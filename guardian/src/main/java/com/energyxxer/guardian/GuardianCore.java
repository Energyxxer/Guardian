package com.energyxxer.guardian;

import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.guardian.ui.common.ProgramUpdateProcess;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

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

    public void startupComplete() {
    }

    public boolean usesJavaEditionDefinitions() {
        return false;
    }
    public boolean usesBedrockEditionDefinitions() {
        return false;
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

    public File getGlobalLibrariesDir() {
        return getMainDirectory().resolve("libraries").toFile();
    }

    public void populateResources() {

    }

    public Point getSplashVersionCoords() {
        return new Point(512, 320);
    }

    public void workspaceLoaded(JsonObject config) {

    }
}
