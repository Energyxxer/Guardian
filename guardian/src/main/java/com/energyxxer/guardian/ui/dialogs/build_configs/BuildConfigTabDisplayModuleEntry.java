package com.energyxxer.guardian.ui.dialogs.build_configs;

import com.energyxxer.commodore.versioning.ThreeNumberVersion;
import com.energyxxer.guardian.global.temp.projects.Project;
import com.energyxxer.guardian.ui.styledcomponents.*;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.prismarine.PrismarineCompiler;
import com.energyxxer.prismarine.util.JsonTraverser;
import com.energyxxer.xswing.ScalableDimension;
import com.energyxxer.xswing.ScalableGraphics2D;
import com.energyxxer.xswing.XFileField;
import com.google.gson.JsonArray;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface BuildConfigTabDisplayModuleEntry<T> {

    void create(ThemeListenerManager tlm, JComponent parent, FieldHost<T> fieldHost, Project project);

    static void nameDescription(ThemeListenerManager tlm, JComponent parent, String name, String description) {
        if(name != null) {
            Label.create(name, Font.BOLD, tlm, parent);
        }

        if(description != null) {
            Label.create(description, Font.PLAIN, tlm, parent);
        }
    }

    abstract class FieldForProperty<T, S> implements BuildConfigTabDisplayModuleEntry<S> {
        protected Property<T, S> property;

        public FieldForProperty<T, S> setProperty(Property<T, S> property) {
            this.property = property;
            return this;
        }
    }

    class Label implements BuildConfigTabDisplayModuleEntry {
        public String text;
        public int style = Font.PLAIN;

        public Label(String text) {
            this.text = text;
        }

        @Override
        public void create(ThemeListenerManager tlm, JComponent parent, FieldHost fieldHost, Project project) {
            create(text, style, tlm, parent);
        }

        public static void create(String text, int style, ThemeListenerManager tlm, JComponent parent) {
            if(text.contains("\n")) {
                String[] lines = text.split("\n");
                for(String line : lines) {
                    StyledLabel label = new StyledLabel(line, tlm);
                    label.setStyle(style);
                    label.setAlignmentX(Component.LEFT_ALIGNMENT);
                    parent.add(label);
                }
            } else {
                StyledLabel label = new StyledLabel(text, tlm);
                label.setStyle(style);
                label.setAlignmentX(Component.LEFT_ALIGNMENT);
                parent.add(label);
            }
        }
    }

    class Spacing implements BuildConfigTabDisplayModuleEntry {
        public int size;

        public Spacing(int size) {
            this.size = size;
        }

        @Override
        public void create(ThemeListenerManager tlm, JComponent parent, FieldHost fieldHost, Project project) {
            Padding component = new Padding(size);
            component.setAlignmentX(Component.LEFT_ALIGNMENT);
            parent.add(component);
        }
    }

    class FileField<S> extends FieldForProperty<String, S> {

        public String name;
        public String description;
        public String dialogTitle;

        public FileField() {

        }

        public FileField(String name, String description) {
            this(name, description, "Select File...");
        }

        public FileField(String name, String description, String dialogTitle) {
            this.name = name;
            this.description = description;
            this.dialogTitle = dialogTitle;
        }

        @Override
        public void create(ThemeListenerManager tlm, JComponent parent, FieldHost<S> fieldHost, Project project) {

            BuildConfigTabDisplayModuleEntry.nameDescription(tlm, parent, name, description);

            StyledFileField component = new StyledFileField(null, "BuildConfigs.content") {
                @Override
                public Dimension getMaximumSize() {
                    return new Dimension(parent.getWidth() - 5, (int) (25* ScalableGraphics2D.SCALE_FACTOR));
                }
                @Override
                public Dimension getPreferredSize() {
                    return getMaximumSize();
                }
            };
            component.setDialogTitle(dialogTitle);
            component.setOperation(XFileField.SAVE);
            component.setMaximumSize(new ScalableDimension(component.getMaximumSize().width,25));
            component.setAlignmentX(Component.LEFT_ALIGNMENT);
            parent.add(component);

            fieldHost.addOpenEvent(s -> {
                component.setFile(PrismarineCompiler.newFileObject(property.get(s), project.getRootDirectory()));
            });
            fieldHost.addApplyEvent(s -> {
                property.set(s, PrismarineCompiler.fileObjectToString(component.getFile(), project.getRootDirectory()));
            });
        }
    }

    class TextField<S> extends FieldForProperty<String, S> {

        public String name;
        public String description;
        public int width = 200;

        public TextField() {

        }

        public TextField(String name, String description) {
            this.name = name;
            this.description = description;
        }

        @Override
        public void create(ThemeListenerManager tlm, JComponent parent, FieldHost<S> fieldHost, Project project) {

            BuildConfigTabDisplayModuleEntry.nameDescription(tlm, parent, name, description);

            StyledTextField component = new StyledTextField("", "BuildConfigs.content", tlm);
            component.setMaximumSize(new ScalableDimension(width,25));
            component.setAlignmentX(Component.LEFT_ALIGNMENT);
            parent.add(component);

            fieldHost.addOpenEvent(s -> {
                component.setText(property.get(s));
            });
            fieldHost.addApplyEvent(s -> {
                property.set(s, component.getText());
            });
        }
    }

    class IntField<S> extends FieldForProperty<Integer, S> {

        public String name;
        public String description;
        public int width = 200;

        public IntField() {

        }

        public IntField(String name, String description) {
            this.name = name;
            this.description = description;
        }

        @Override
        public void create(ThemeListenerManager tlm, JComponent parent, FieldHost<S> fieldHost, Project project) {

            BuildConfigTabDisplayModuleEntry.nameDescription(tlm, parent, name, description);

            StyledTextField component = new StyledTextField("", "BuildConfigs.content", tlm);
            component.setMaximumSize(new ScalableDimension(width,25));
            component.setAlignmentX(Component.LEFT_ALIGNMENT);
            parent.add(component);

            fieldHost.addOpenEvent(s -> {
                component.setText(Integer.toString(property.get(s)));
            });
            fieldHost.addApplyEvent(s -> {
                try {
                    property.set(s, Integer.parseInt(component.getText()));
                } catch(NumberFormatException ignore) {}
            });
        }
    }

    class DoubleField<S> extends FieldForProperty<Double, S> {

        public String name;
        public String description;
        public int width = 200;

        public DoubleField() {

        }

        public DoubleField(String name, String description) {
            this.name = name;
            this.description = description;
        }

        @Override
        public void create(ThemeListenerManager tlm, JComponent parent, FieldHost<S> fieldHost, Project project) {

            BuildConfigTabDisplayModuleEntry.nameDescription(tlm, parent, name, description);

            StyledTextField component = new StyledTextField("", "BuildConfigs.content", tlm);
            component.setMaximumSize(new ScalableDimension(width,25));
            component.setAlignmentX(Component.LEFT_ALIGNMENT);
            parent.add(component);

            fieldHost.addOpenEvent(s -> {
                component.setText(Double.toString(property.get(s)));
            });
            fieldHost.addApplyEvent(s -> {
                try {
                    property.set(s, Double.parseDouble(component.getText()));
                } catch(NumberFormatException ignore) {}
            });
        }
    }

    class VersionField<S> extends FieldForProperty<JsonArray, S> {

        public String name;
        public String description;
        public int width = 200;

        private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))");

        public VersionField() {

        }

        public VersionField(String name, String description) {
            this.name = name;
            this.description = description;
        }

        @Override
        public void create(ThemeListenerManager tlm, JComponent parent, FieldHost<S> fieldHost, Project project) {

            BuildConfigTabDisplayModuleEntry.nameDescription(tlm, parent, name, description);

            StyledTextField component = new StyledTextField("", "BuildConfigs.content", tlm);
            component.setMaximumSize(new ScalableDimension(width,25));
            component.setAlignmentX(Component.LEFT_ALIGNMENT);
            parent.add(component);

            fieldHost.addOpenEvent(s -> {
                JsonArray arr = property.get(s);
                ThreeNumberVersion version;
                if(arr != null) {
                    version = new ThreeNumberVersion(
                            JsonTraverser.getThreadInstance().reset(arr).get(0).asInt(),
                            JsonTraverser.getThreadInstance().reset(arr).get(1).asInt(),
                            JsonTraverser.getThreadInstance().reset(arr).get(2).asInt()
                    );
                } else {
                    version = new ThreeNumberVersion(1, 0, 0);
                }
                component.setText(version.getVersionString());
            });
            fieldHost.addApplyEvent(s -> {
                Matcher matcher = VERSION_PATTERN.matcher(component.getText());
                try {
                    if(matcher.matches()) {
                        int major = Integer.parseInt(matcher.group(1));
                        int minor = Integer.parseInt(matcher.group(2));
                        int patch = matcher.groupCount() >= 3 ? Integer.parseInt(matcher.group(3)) : 0;
                        JsonArray arr = new JsonArray(3);
                        arr.add(major);
                        arr.add(minor);
                        arr.add(patch);
                        property.set(s, arr);
                    }
                } catch(NumberFormatException ignore) {}
            });
        }
    }

    class CheckboxField<S> extends FieldForProperty<Boolean, S> {

        public String name;
        public String description;

        public CheckboxField() {

        }

        public CheckboxField(String name, String description) {
            this.name = name;
            this.description = description;
        }

        @Override
        public void create(ThemeListenerManager tlm, JComponent parent, FieldHost<S> fieldHost, Project project) {
            StyledCheckBox checkbox = new StyledCheckBox(name,"BuildConfigs.content");
            checkbox.setAlignmentX(Component.LEFT_ALIGNMENT);
            parent.add(checkbox);
            if(description != null) {
                BuildConfigTabDisplayModuleEntry.nameDescription(tlm, parent, null, description);
            }

            fieldHost.addOpenEvent(s -> {
                checkbox.setSelected(property.get(s));
            });
            fieldHost.addApplyEvent(s -> {
                property.set(s, checkbox.isSelected());
            });
        }
    }
}
