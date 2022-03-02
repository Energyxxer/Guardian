package com.energyxxer.guardian.ui.dialogs.build_configs;

import com.energyxxer.prismarine.util.JsonTraverser;
import com.energyxxer.util.logger.Debug;
import com.google.gson.*;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class BuildConfigTabParser {
    public static void parseUserBuildConfigTabs(File file, ArrayList<BuildConfigTab> tabs) {
        if(file.exists() && file.isFile()) {
            try(FileReader fr = new FileReader(file)) {
                JsonObject root = new Gson().fromJson(fr, JsonObject.class);
                JsonTraverser traverser = new JsonTraverser(root);

                for(Map.Entry<String, JsonElement> rawTabEntry : traverser.get("tabs").iterateAsObject()) {
                    JsonObject rawTabObj = traverser.reset(rawTabEntry.getValue()).asJsonObject();
                    if(rawTabObj == null) continue;
                    traverser.reset(rawTabObj);
                    tabs.add(parseTab(traverser, rawTabEntry.getKey(), rawTabObj));
                }

            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
    }
    private static BuildConfigTab parseTab(JsonTraverser traverser, String title, JsonObject rawTabObj) {
        BuildConfigTab tab = new BuildConfigTab(title);
        for(JsonElement rawEntry : traverser.reset(rawTabObj).get("fields").iterateAsArray()) {
            parseEntry(traverser, rawEntry, tab);
        }
        return tab;
    }

    private static void parseEntry(JsonTraverser traverser, JsonElement rawEntry, BuildConfigTab tab) {
        if(rawEntry.isJsonObject()) {
            String type = traverser.reset(rawEntry).get("type").asString();
            if(type != null) {
                switch(type) {
                    case "label": {
                        parseLabel(traverser, rawEntry.getAsJsonObject(), tab);
                        break;
                    }
                    default: {
                        parseField(traverser, rawEntry.getAsJsonObject(), type, tab);
                        break;
                    }
                }
            }
        } else if(rawEntry.isJsonPrimitive()) {
            JsonPrimitive rawPrimitive = rawEntry.getAsJsonPrimitive();
            if(rawPrimitive.isNumber()) {
                tab.addEntry(new BuildConfigTabDisplayModuleEntry.Spacing(rawPrimitive.getAsInt()));
            } else if(rawPrimitive.isString()) {
                tab.addEntry(new BuildConfigTabDisplayModuleEntry.Label(rawPrimitive.getAsString()));
            }
        }
    }

    private static void parseLabel(JsonTraverser traverser, JsonObject entryObj, BuildConfigTab tab) {
        String text = traverser.reset(entryObj).get("text").asString();
        if(text == null) return;
        BuildConfigTabDisplayModuleEntry.Label label = new BuildConfigTabDisplayModuleEntry.Label(text);
        if(traverser.reset(entryObj).get("bold").asBoolean(false)) label.style |= Font.BOLD;
        if(traverser.reset(entryObj).get("italic").asBoolean(false)) label.style |= Font.ITALIC;

        tab.addEntry(label);
    }

    private static void parseField(JsonTraverser traverser, JsonObject entryObj, String type, BuildConfigTab tab) {
        String path = traverser.reset(entryObj).get("path").asString();
        if(path == null) return;

        String name = traverser.reset(entryObj).get("name").asString();
        String description = traverser.reset(entryObj).get("description").asString();

        switch(type) {
            case "boolean": {
                JsonProperty<Boolean> property = new JsonProperty.JsonBooleanProperty(
                        path,
                        traverser.reset(entryObj).get("default-value").asBoolean(false)
                );

                BuildConfigTabDisplayModuleEntry.CheckboxField<JsonTraverser> field = new BuildConfigTabDisplayModuleEntry.CheckboxField<>(name, description);
                field.setProperty(property);

                tab.addEntry(field);

                break;
            }
            case "string": {
                JsonProperty<String> property = new JsonProperty.JsonStringProperty(
                        path,
                        traverser.reset(entryObj).get("default-value").asString()
                );

                BuildConfigTabDisplayModuleEntry.TextField<JsonTraverser> field = new BuildConfigTabDisplayModuleEntry.TextField<>(name, description);
                field.setProperty(property);
                field.width = traverser.reset(entryObj).get("width").asInt(field.width);

                tab.addEntry(field);

                break;
            }
            case "int": {
                JsonProperty<Integer> property = new JsonProperty.JsonIntProperty(
                        path,
                        traverser.reset(entryObj).get("default-value").asInt(0)
                );

                BuildConfigTabDisplayModuleEntry.IntField<JsonTraverser> field = new BuildConfigTabDisplayModuleEntry.IntField<>(name, description);
                field.setProperty(property);
                field.width = traverser.reset(entryObj).get("width").asInt(field.width);

                tab.addEntry(field);

                break;
            }
            case "real": {
                JsonProperty<Double> property = new JsonProperty.JsonDoubleProperty(
                        path,
                        traverser.reset(entryObj).get("default-value").asDouble(0)
                );

                BuildConfigTabDisplayModuleEntry.DoubleField<JsonTraverser> field = new BuildConfigTabDisplayModuleEntry.DoubleField<>(name, description);
                field.setProperty(property);
                field.width = traverser.reset(entryObj).get("width").asInt(field.width);

                tab.addEntry(field);

                break;
            }
            case "version": {
                JsonProperty<JsonArray> property = new JsonProperty.JsonArrayProperty(
                        path,
                        traverser.reset(entryObj).get("default-value").asJsonArray()
                );

                BuildConfigTabDisplayModuleEntry.VersionField<JsonTraverser> field = new BuildConfigTabDisplayModuleEntry.VersionField<>(name, description);
                field.setProperty(property);
                field.width = traverser.reset(entryObj).get("width").asInt(field.width);

                tab.addEntry(field);

                break;
            }
            case "file": {
                JsonProperty<String> property = new JsonProperty.JsonStringProperty(
                        path,
                        traverser.reset(entryObj).get("default-value").asString()
                );

                BuildConfigTabDisplayModuleEntry.FileField<JsonTraverser> field = new BuildConfigTabDisplayModuleEntry.FileField<>(
                        name,
                        description,
                        traverser.reset(entryObj).get("dialog-title").asNonEmptyString()
                );
                field.setProperty(property);

                tab.addEntry(field);

                break;
            }
            default: {
                Debug.log("Unhandled custom field type '" + type + "'");
            }
        }
    }
}
