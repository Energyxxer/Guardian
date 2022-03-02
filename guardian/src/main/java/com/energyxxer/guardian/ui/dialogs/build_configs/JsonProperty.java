package com.energyxxer.guardian.ui.dialogs.build_configs;

import com.energyxxer.prismarine.util.JsonTraverser;
import com.google.gson.JsonArray;

public abstract class JsonProperty<T> implements Property<T, JsonTraverser> {
    private String[] pathParts;
    protected T defaultValue;

    public JsonProperty(String path, T defaultValue) {
        this.pathParts = path.split("\\.");
        this.defaultValue = defaultValue;
    }

    protected JsonTraverser traverseGet(JsonTraverser traverser) {
        traverser.reset();
        for(String part : pathParts) {
            traverser.get(part);
        }
        return traverser;
    }

    protected JsonTraverser traverseSet(JsonTraverser traverser) {
        traverser.reset().createOnTraversal();
        for(String part : pathParts) {
            traverser.get(part);
        }
        return traverser;
    }

    public static class JsonBooleanProperty extends JsonProperty<Boolean> {

        public JsonBooleanProperty(String path, boolean defaultValue) {
            super(path, defaultValue);
        }

        @Override
        public Boolean get(JsonTraverser subject) {
            return traverseGet(subject).asBoolean(defaultValue);
        }

        @Override
        public void set(JsonTraverser subject, Boolean value) {
            traverseSet(subject).set(value);
        }
    }

    public static class JsonStringProperty extends JsonProperty<String> {

        public JsonStringProperty(String path, String defaultValue) {
            super(path, defaultValue);
        }

        @Override
        public String get(JsonTraverser subject) {
            return traverseGet(subject).asString(defaultValue);
        }

        @Override
        public void set(JsonTraverser subject, String value) {
            traverseSet(subject).set(value);
        }
    }

    public static class JsonIntProperty extends JsonProperty<Integer> {

        public JsonIntProperty(String path, int defaultValue) {
            super(path, defaultValue);
        }

        @Override
        public Integer get(JsonTraverser subject) {
            return traverseGet(subject).asInt(defaultValue);
        }

        @Override
        public void set(JsonTraverser subject, Integer value) {
            traverseSet(subject).set(value);
        }
    }

    public static class JsonDoubleProperty extends JsonProperty<Double> {

        public JsonDoubleProperty(String path, double defaultValue) {
            super(path, defaultValue);
        }

        @Override
        public Double get(JsonTraverser subject) {
            return traverseGet(subject).asDouble(defaultValue);
        }

        @Override
        public void set(JsonTraverser subject, Double value) {
            traverseSet(subject).set(value);
        }
    }

    public static class JsonArrayProperty extends JsonProperty<JsonArray> {

        public JsonArrayProperty(String path, JsonArray defaultValue) {
            super(path, defaultValue);
        }

        @Override
        public JsonArray get(JsonTraverser subject) {
            JsonArray value = traverseGet(subject).asJsonArray();
            if(value == null) value = defaultValue;
            return value;
        }

        @Override
        public void set(JsonTraverser subject, JsonArray value) {
            traverseSet(subject).set(value);
        }
    }
}
