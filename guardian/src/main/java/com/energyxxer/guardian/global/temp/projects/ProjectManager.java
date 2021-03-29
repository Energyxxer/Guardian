package com.energyxxer.guardian.global.temp.projects;

import com.energyxxer.guardian.langinterface.ProjectType;
import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

public class ProjectManager {
	public static final String WORKSPACE_CONFIG_FILE_NAME = ".guardianworkspace";

	private static ArrayList<Project> loadedProjects = new ArrayList<>();
	private static String workspaceDir = null;

	public static void loadWorkspace() {
		if(workspaceDir == null) throw new IllegalStateException("Workspace directory not specified.");

		loadedProjects.clear();
		
		File workspace = new File(workspaceDir);

		File[] fileList = workspace.listFiles();
		if (fileList == null) {
			return;
		}

		for(File file : fileList) {
			if(file.isDirectory()) {
				ProjectType projectType = ProjectType.getProjectTypeForRoot(file);

				if(projectType != null) {
					try {
						loadedProjects.add(projectType.createProjectFromRoot(new File(file.getAbsolutePath())));
					} catch (RuntimeException x) {
						x.printStackTrace();
					}
				}
			}
		}

		GuardianWindow.projectExplorer.refresh();

		JsonObject workspaceConfigObj = null;

		File workspaceConfigFile = workspace.toPath().resolve(WORKSPACE_CONFIG_FILE_NAME).toFile();
		if(workspaceConfigFile.exists()) {

			try(FileReader fr = new FileReader(workspaceConfigFile)) {
				workspaceConfigObj = new Gson().fromJson(fr, JsonObject.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Guardian.core.workspaceLoaded(workspaceConfigObj);
	}
	
	public static Project getAssociatedProject(File file) {
		if(file == null) return null;
		for(Project project : loadedProjects) {
			if((file.getPath() + File.separator).startsWith((project.getRootDirectory().getPath() + File.separator))) {
				return project;
			}
		}
		return null;
	}

	public static void create(String name, ProjectType type) {
		Project p = type.createNew(Paths.get(ProjectManager.getWorkspaceDir()).resolve(name));
		loadedProjects.add(p);
	}
	
	public static boolean renameFile(File file, String newName) {
		String path = file.getAbsolutePath();
		String name = file.getName();
		String pathToParent = path.substring(0, path.lastIndexOf(name));
		
		File newFile = new File(pathToParent + newName);
		
		boolean renamed = file.renameTo(newFile);

		return renamed;
	}

	public static String getWorkspaceDir() {
		return workspaceDir;
	}

	public static void setWorkspaceDir(String workspaceDir) {
		ProjectManager.workspaceDir = workspaceDir;
	}

	public static Collection<Project> getLoadedProjects() {
		return loadedProjects;
	}

	public static boolean isLoadedProjectRoot(File file) {
		for(Project project : loadedProjects) {
			if(project.getRootDirectory().equals(file)) return true;
		}
		return false;
	}
}
