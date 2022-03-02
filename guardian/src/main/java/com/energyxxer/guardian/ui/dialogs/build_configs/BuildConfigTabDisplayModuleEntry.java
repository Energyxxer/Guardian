package com.energyxxer.guardian.ui.dialogs.build_configs;

import com.energyxxer.guardian.ui.styledcomponents.*;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.ScalableDimension;
import com.energyxxer.xswing.ScalableGraphics2D;
import com.energyxxer.xswing.XFileField;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public interface BuildConfigTabDisplayModuleEntry<T> {

    void create(ThemeListenerManager tlm, JComponent parent, FieldHost<T> fieldHost);

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

        public Label(String text) {
            this.text = text;
        }

        @Override
        public void create(ThemeListenerManager tlm, JComponent parent, FieldHost fieldHost) {
            create(text, Font.PLAIN, tlm, parent);
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
        public void create(ThemeListenerManager tlm, JComponent parent, FieldHost fieldHost) {
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
        public void create(ThemeListenerManager tlm, JComponent parent, FieldHost<S> fieldHost) {

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
                component.setFile(new File(property.get(s)));
            });
            fieldHost.addApplyEvent(s -> {
                property.set(s, component.getFile().getAbsolutePath());
            });
        }
    }

    class TextField<S> extends FieldForProperty<String, S> {

        public String name;
        public String description;

        public TextField() {

        }

        public TextField(String name, String description) {
            this.name = name;
            this.description = description;
        }

        @Override
        public void create(ThemeListenerManager tlm, JComponent parent, FieldHost<S> fieldHost) {

            BuildConfigTabDisplayModuleEntry.nameDescription(tlm, parent, name, description);

            StyledTextField component = new StyledTextField("", "BuildConfigs.content", tlm);
            component.setPreferredSize(new ScalableDimension(300,25));
            component.setMaximumSize(new ScalableDimension(200,25));
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
        public void create(ThemeListenerManager tlm, JComponent parent, FieldHost<S> fieldHost) {
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
