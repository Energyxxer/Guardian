package com.energyxxer.guardian.global.temp.projects;

import com.energyxxer.guardian.global.Preferences;
import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.prismarine.util.JsonTraverser;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BuildConfiguration<T> {
    public File file;
    public JsonObject root;

    public BuildConfiguration() {
    }

    public BuildConfiguration(File file) {
        this(file, null);
    }

    public BuildConfiguration(File file, JsonObject root) {
        this.file = file;
        this.root = root;

        populateMetadata();
    }

    public String name;
    public int sortIndex;

    private T config;

    public ArrayList<String> preActions;
    public ArrayList<String> postActions;
    public ArrayList<String> postSuccessActions;
    public ArrayList<String> postFailureActions;

    public T get(Project<T> project) {
        if(config == null) populate(project);
        return config;
    }

    private void populate(Project<T> project) {
        try {
            config = project.parseBuildConfig(root, file);
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    public void populateMetadata() {
        if(root == null) {
            try(JsonReader jsonReader = new JsonReader(new InputStreamReader(new FileInputStream(file), Guardian.DEFAULT_CHARSET))) {
                root = new Gson().fromJson(jsonReader, JsonObject.class);
            } catch (Exception x) {
                x.printStackTrace();
            }
        }

        JsonTraverser traverser = new JsonTraverser(root);

        name = traverser.reset().get("guardian").get("name").asNonEmptyString("Unnamed Configuration");
        sortIndex = traverser.reset().get("guardian").get("sort-index").asInt(0);

        populateActions(preActions = new ArrayList<>(), traverser.reset().get("guardian").get("actions").get("pre"));
        populateActions(postActions = new ArrayList<>(), traverser.reset().get("guardian").get("actions").get("post"));
        populateActions(postSuccessActions = new ArrayList<>(), traverser.reset().get("guardian").get("actions").get("post-success"));
        populateActions(postFailureActions = new ArrayList<>(), traverser.reset().get("guardian").get("actions").get("post-failure"));
    }

    private void populateActions(ArrayList<String> actions, JsonTraverser traverser) {
        for(JsonElement rawCommand : traverser.iterateAsArray()) {
            if(rawCommand.isJsonPrimitive() && rawCommand.getAsJsonPrimitive().isString()) {
                String command = rawCommand.getAsString();
                if(!command.isEmpty()) {
                    actions.add(command);
                }
            }
        }
    }

    public boolean isFallback() {
        return file == null;
    }

    @Override
    public String toString() {
        return name;
    }

    private static final HashMap<File, String> activeBuildConfigs = new HashMap<>();

    static {
        String raw = Preferences.get("active_build_configs", null);
        try {
            if(raw != null) {
                String[] parts = raw.split(File.pathSeparator);
                for(int i = 0; i < parts.length; i += 2) {
                    File projectRoot = new File(parts[i]);
                    if(projectRoot.exists() && projectRoot.isDirectory()) {
                        activeBuildConfigs.put(projectRoot, parts[i+1]);
                    } else {
                        break;
                    }
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public static void saveActiveBuildConfigs() {
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<File, String> entry : activeBuildConfigs.entrySet()) {
            sb.append(entry.getKey());
            sb.append(File.pathSeparatorChar);
            sb.append(entry.getValue());
            sb.append(File.pathSeparatorChar);
        }
        Preferences.put("active_build_configs", sb.toString());
    }

    public static String getActiveBuildConfig(File projectRoot) {
        return activeBuildConfigs.get(projectRoot);
    }

    public static void setActiveBuildConfig(File projectRoot, String configName) {
        activeBuildConfigs.put(projectRoot, configName);
        saveActiveBuildConfigs();
    }
}
