package com.energyxxer.guardian;

import com.energyxxer.guardian.global.temp.projects.Project;
import com.energyxxer.guardian.global.temp.projects.ProjectManager;
import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.guardian.ui.common.ProgramUpdateProcess;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class GuardianCore {
    protected Class resourceRegistry = GuardianCore.class;
    protected String programName = "Guardian";
    protected Path mainDirectory = new File(System.getProperty("user.home") + File.separator + "Guardian").toPath();

    public String getProgramName() {
        return programName;
    }

    public String getDisplayedVersion() {
        return Guardian.VERSION.toString();
    }

    public String getLicense() {
        return Guardian.LICENSE;
    }

    public Class getResourceRegistry() {
        return resourceRegistry;
    }

    public Path getMainDirectory() {
        return mainDirectory;
    }

    public File getResourceInfoFile() {
        return mainDirectory.resolve("resources.json").toFile();
    }

    public File getDefaultWorkspace() {
        return mainDirectory.resolve("workspace").toFile();
    }

    public Path getThemeDir() {
        return mainDirectory.resolve("resources").resolve("themes");
    }

    public ProgramUpdateProcess.ProgramVersionInfo checkForUpdates() throws IOException {
        return null;
    }

    public Collection<JComponent> createWelcomePaneButtons(ThemeListenerManager tlm) {
        return null;
    }

    public File getLogFile() {
        return getMainDirectory().resolve("latest.log").toFile();
    }

    public File getDefinitionPacksDir() {
        return getMainDirectory().resolve("resources").resolve("defpacks").toFile();
    }

    public File getFeatureMapsDir() {
        return getMainDirectory().resolve("resources").resolve("featmaps").toFile();
    }

    public File getTypeMapDir() {
        return getMainDirectory().resolve("resources").resolve("typemaps").toFile();
    }

    public File getProjectTemplatesDir() {
        return getMainDirectory().resolve("resources").resolve("project_templates").toFile();
    }

    private final ArrayList<File> templateRootsReturn = new ArrayList<>();
    public ArrayList<File> getProjectTemplateRoots() {
        templateRootsReturn.clear();
        File mainTemplatesDir = getProjectTemplatesDir();
        if(mainTemplatesDir != null && mainTemplatesDir.isDirectory()) {
            //noinspection ConstantConditions
            Collections.addAll(templateRootsReturn, mainTemplatesDir.listFiles());
        }
        for(Project project : ProjectManager.getLoadedProjects()) {
            File subTemplatesDir = project.getRootDirectory().toPath().resolve(".guardian").resolve("project_templates").toFile();
            if(subTemplatesDir.exists() && subTemplatesDir.isDirectory()) {
                Collections.addAll(templateRootsReturn, subTemplatesDir.listFiles());
            }
        }
        return templateRootsReturn;
    }

    public File getGlobalLibrariesDir() {
        return getMainDirectory().resolve("libraries").toFile();
    }

    public Point getSplashVersionCoords() {
        return new Point(512, 320);
    }

    public URI getDocumentationURI() throws URISyntaxException {
        return null;
    }
    public boolean useJavaImageCoordinates() {
        return false;
    }
}
