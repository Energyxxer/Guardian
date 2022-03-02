package com.energyxxer.guardian.global.temp.projects;

import com.energyxxer.commodore.versioning.Version;
import com.energyxxer.enxlex.lexical_analysis.summary.ProjectSummarizer;
import com.energyxxer.enxlex.lexical_analysis.summary.ProjectSummary;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.guardian.langinterface.ProjectType;
import com.energyxxer.guardian.ui.dialogs.build_configs.BuildConfigTab;
import com.energyxxer.guardian.ui.dialogs.build_configs.BuildConfigTabParser;
import com.energyxxer.prismarine.PrismarineCompiler;
import com.energyxxer.prismarine.summaries.PrismarineProjectSummary;
import com.google.gson.JsonObject;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public interface Project<T> {
	ProjectType getProjectType();

	TokenPatternMatch getFileStructure();
	PrismarineProjectSummary getSummary();
	File getRootDirectory();
	default File getBuildDirectory() {
		return getRootDirectory().toPath().resolve(".build").toFile();
	}
	String getName();
	File getServerDataRoot();
	File getClientDataRoot();
	void updateSummary(ProjectSummary summary);
	void updateConfig();
    Version getTargetVersion();
    Image getIconForFile(File file);

    default BuildConfiguration<T> getBuildConfig() {
		ArrayList<BuildConfiguration<T>> buildConfigs = getAllBuildConfigs();
		if(buildConfigs == null || buildConfigs.isEmpty()) return getDefaultBuildConfig();

		String activeName = BuildConfiguration.getActiveBuildConfig(getRootDirectory());
		if(activeName != null) {
			for(BuildConfiguration<T> config : buildConfigs) {
				if(config.file.getName().equals(activeName)) return config;
			}
		}

		//selected build config removed or not set, set to first
		activeName = buildConfigs.get(0).file.getName();
		BuildConfiguration.setActiveBuildConfig(getRootDirectory(), activeName);
		return buildConfigs.get(0);
	}

	BuildConfiguration<T> getDefaultBuildConfig();

	T parseBuildConfig(JsonObject root, File file) throws IOException;

	PrismarineCompiler createProjectCompiler();
	ProjectSummarizer createProjectSummarizer();

	long getInstantiationTime();

	void clearPersistentCache();

	default ArrayList<BuildConfiguration<T>> listAllConfigs() {
		File configDir = getRootDirectory().toPath().resolve(".build").toFile();

		ArrayList<BuildConfiguration<T>> list = new ArrayList<>();

		if(configDir.exists() && configDir.isDirectory()) {
			File[] configFiles = configDir.listFiles();
			if(configFiles != null) {
				for(File configFile : configFiles) {
					if(configFile.getName().endsWith(".build")) {
						list.add(new BuildConfiguration<>(configFile));
					}
				}
			}
		}

		list.sort(Comparator.comparingInt(c -> c.sortIndex));

		return list;
	}

	ArrayList<BuildConfiguration<T>> getAllBuildConfigs();

	default void setActiveBuildConfig(BuildConfiguration<T> config) {
		if(!getBuildDirectory().equals(config.file.getParentFile())) throw new IllegalArgumentException("Build config does not belong to project");

		BuildConfiguration.setActiveBuildConfig(getRootDirectory(), config.file.getName());
		buildConfigUpdated(config);
	}

	Iterable<? extends BuildConfigTab> getBuildConfigTabs();

    default void parseUserBuildConfigTabs(ArrayList<BuildConfigTab> tabs, boolean recursively) {
        parseUserBuildConfigTabs(tabs);
        if(recursively) {
        	for(Project dependency : getLoadedDependencies(new ArrayList<>(), true)) {
        		dependency.parseUserBuildConfigTabs(tabs);
			}
		}
    }

    default void parseUserBuildConfigTabs(ArrayList<BuildConfigTab> tabs) {
	    File file = getRootDirectory().toPath().resolve(".guardian").resolve("build_config_fields.json").toFile();
        BuildConfigTabParser.parseUserBuildConfigTabs(file, tabs);
    }

	default void buildConfigUpdated(BuildConfiguration<T> config) {}

    ArrayList<Project> getLoadedDependencies(ArrayList<Project> list, boolean recursively);

	void refreshBuildConfigs();
}