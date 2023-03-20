package com.energyxxer.guardian;

import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;

public interface GuardianBinding {
    void setup();
    default void setupActions() {}
    default void populateResources() {}

    default void startupComplete() {}
    default void workspaceLoaded(JsonObject config) {}

    default boolean usesJavaEditionDefinitions() {
        return false;
    }
    default boolean usesBedrockEditionDefinitions() {
        return false;
    }

    default String getTemplateVariable(String s, File destination, File templateRoot) {
        return null;
    }

    default void setupSettingsSections(HashMap<String, JPanel> sectionPanes) {}

    default Image getIconForFile(File file) {
        return null;
    }
}
