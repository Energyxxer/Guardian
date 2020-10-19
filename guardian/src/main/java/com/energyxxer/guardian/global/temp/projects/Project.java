package com.energyxxer.guardian.global.temp.projects;

import com.energyxxer.commodore.versioning.Version;
import com.energyxxer.enxlex.lexical_analysis.summary.ProjectSummarizer;
import com.energyxxer.enxlex.lexical_analysis.summary.ProjectSummary;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.guardian.langinterface.ProjectType;
import com.energyxxer.prismarine.PrismarineCompiler;

import java.awt.*;
import java.io.File;

public interface Project {
	ProjectType getProjectType();

	TokenPatternMatch getFileStructure();
	ProjectSummary getSummary();
	File getRootDirectory();
	String getName();
	File getServerDataRoot();
	File getClientDataRoot();
	void updateSummary(ProjectSummary summary);
	void updateConfig();
    Version getTargetVersion();
    Image getIconForFile(File file);

	Iterable<String> getPreActions();
	Iterable<String> getPostActions();

	PrismarineCompiler createProjectCompiler();
	ProjectSummarizer createProjectSummarizer();

	long getInstantiationTime();

	void clearPersistentCache();
}
