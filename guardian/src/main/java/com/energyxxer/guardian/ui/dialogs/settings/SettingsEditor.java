package com.energyxxer.guardian.ui.dialogs.settings;

import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.editor.EditorComponent;
import com.energyxxer.guardian.ui.editor.EditorModule;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.editor.behavior.caret.Dot;
import com.energyxxer.guardian.ui.scrollbar.OverlayScrollPane;
import com.energyxxer.guardian.ui.styledcomponents.StyledCheckBox;
import com.energyxxer.guardian.ui.styledcomponents.StyledLabel;
import com.energyxxer.guardian.ui.styledcomponents.StyledTextField;
import com.energyxxer.guardian.ui.theme.change.ThemeChangeListener;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.Padding;
import com.energyxxer.xswing.ScalableDimension;

import javax.swing.*;
import java.awt.*;

public class SettingsEditor extends JPanel {

    private ThemeListenerManager tlm = new ThemeListenerManager();

    {
        {
            JPanel header = new JPanel(new BorderLayout());
            header.setPreferredSize(new ScalableDimension(0,40));
            this.add(header, BorderLayout.NORTH);

            {
                JPanel padding = new JPanel();
                padding.setOpaque(false);
                padding.setPreferredSize(new ScalableDimension(25,25));
                header.add(padding, BorderLayout.WEST);
            }

            StyledLabel label = new StyledLabel("Editor","Settings.content.header", tlm);
            header.add(label, BorderLayout.CENTER);

            tlm.addThemeChangeListener(t -> {
                setBackground(t.getColor(new Color(235, 235, 235), "Settings.content.background"));
                header.setBackground(t.getColor(new Color(235, 235, 235), "Settings.content.header.background"));
                header.setBorder(BorderFactory.createMatteBorder(0, 0, Math.max(t.getInteger(1,"Settings.content.header.border.thickness"),0), 0, t.getColor(new Color(200, 200, 200), "Settings.content.header.border.color")));
            });
        }

        {
            JPanel paddingLeft = new JPanel();
            paddingLeft.setOpaque(false);
            paddingLeft.setPreferredSize(new ScalableDimension(50,25));
            this.add(paddingLeft, BorderLayout.WEST);
        }
        {
            JPanel paddingRight = new JPanel();
            paddingRight.setOpaque(false);
            paddingRight.setPreferredSize(new ScalableDimension(50,25));
            this.add(paddingRight, BorderLayout.EAST);
        }

        {

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setOpaque(false);
            this.add(new OverlayScrollPane(tlm, content), BorderLayout.CENTER);


            {
                content.add(new Padding(20));
            }


            {
                StyledLabel label = new StyledLabel("Font Family:","Settings.content", tlm);
                label.setStyle(Font.BOLD);
                content.add(label);
            }
            {
                StyledTextField fontField = new StyledTextField("","Settings.content", tlm);
                fontField.setMaximumSize(new ScalableDimension(300,25));
                fontField.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> fontField.setText(AdvancedEditor.FONT.get()));
                Settings.addApplyEvent(() -> {
                    EditorComponent.FONT.set(fontField.getText());
                    ThemeChangeListener.dispatchThemeChange(GuardianWindow.getTheme());
                });
                content.add(fontField);
            }


            {
                StyledLabel label = new StyledLabel("Line Spacing:","Settings.content", tlm);
                label.setStyle(Font.BOLD);
                content.add(label);
            }
            {
                StyledTextField lineSpacingField = new StyledTextField("","Settings.content", tlm);
                lineSpacingField.setMaximumSize(new ScalableDimension(300,25));
                lineSpacingField.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> lineSpacingField.setText("" + AdvancedEditor.LINE_SPACING.get()));
                Settings.addApplyEvent(() -> {
                    try {
                        float spacing = Float.parseFloat(lineSpacingField.getText());
                        if(spacing >= 0.1) {
                            EditorComponent.LINE_SPACING.set(spacing);
                            ThemeChangeListener.dispatchThemeChange(GuardianWindow.getTheme());
                        }
                    } catch(NumberFormatException ignore) {}
                });
                content.add(lineSpacingField);
            }


            {
                content.add(new Padding(20));
            }


            {
                StyledLabel label = new StyledLabel("Auto-reparse delay (ms):","Settings.content", tlm);
                label.setStyle(Font.BOLD);
                content.add(label);
            }
            {
                StyledTextField autoreparseDelayField = new StyledTextField("","Settings.content", tlm);
                autoreparseDelayField.setMaximumSize(new ScalableDimension(300,25));
                autoreparseDelayField.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> autoreparseDelayField.setText("" + EditorComponent.AUTOREPARSE_DELAY.get()));
                Settings.addApplyEvent(() -> {
                    try {
                        int delay = Integer.parseInt(autoreparseDelayField.getText());
                        if(delay >= 0) {
                            EditorComponent.AUTOREPARSE_DELAY.set(delay);
                        }
                    } catch(NumberFormatException ignore) {}
                });
                content.add(autoreparseDelayField);
            }

            {
                StyledCheckBox showSuggestions = new StyledCheckBox("Show suggestions as you type","Settings.content");
                showSuggestions.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> showSuggestions.setSelected(EditorComponent.SHOW_SUGGESTIONS.get()));
                Settings.addApplyEvent(() -> EditorComponent.SHOW_SUGGESTIONS.set(showSuggestions.isSelected()));

                content.add(showSuggestions);
            }
            {
                StyledLabel label = new StyledLabel("Suggestions are tied to the auto-reparse delay","Settings.content", tlm);
                label.setStyle(Font.ITALIC);
                content.add(label);
            }


            {
                content.add(new Padding(20));
            }

            {
                StyledCheckBox insertTrailingNewline = new StyledCheckBox("Insert trailing newline","Settings.content");
                insertTrailingNewline.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> insertTrailingNewline.setSelected(EditorModule.INSERT_TRAILING_NEWLINE.get()));
                Settings.addApplyEvent(() -> EditorModule.INSERT_TRAILING_NEWLINE.set(insertTrailingNewline.isSelected()));

                content.add(insertTrailingNewline);
            }


            {
                content.add(new Padding(20));
            }

            {
                StyledLabel label = new StyledLabel("Smart Keys:","Settings.content", tlm);
                label.setStyle(Font.BOLD);
                content.add(label);
            }

            {
                StyledCheckBox smartKeysHome = new StyledCheckBox("Home","Settings.content");
                smartKeysHome.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> smartKeysHome.setSelected(Dot.SMART_KEYS_HOME.get()));
                Settings.addApplyEvent(() -> Dot.SMART_KEYS_HOME.set(smartKeysHome.isSelected()));

                content.add(smartKeysHome);
            }
            {
                StyledCheckBox smartKeysIndent = new StyledCheckBox("Smart Indent","Settings.content");
                smartKeysIndent.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> smartKeysIndent.setSelected(Dot.SMART_KEYS_INDENT.get()));
                Settings.addApplyEvent(() -> Dot.SMART_KEYS_INDENT.set(smartKeysIndent.isSelected()));

                content.add(smartKeysIndent);
            }
            {
                StyledCheckBox smartKeysBraces = new StyledCheckBox("Smart Braces","Settings.content");
                smartKeysBraces.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> smartKeysBraces.setSelected(Dot.SMART_KEYS_BRACES.get()));
                Settings.addApplyEvent(() -> Dot.SMART_KEYS_BRACES.set(smartKeysBraces.isSelected()));

                content.add(smartKeysBraces);
            }
            {
                StyledCheckBox smartKeysQuotes = new StyledCheckBox("Smart Quotes","Settings.content");
                smartKeysQuotes.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> smartKeysQuotes.setSelected(Dot.SMART_KEYS_QUOTES.get()));
                Settings.addApplyEvent(() -> Dot.SMART_KEYS_QUOTES.set(smartKeysQuotes.isSelected()));

                content.add(smartKeysQuotes);
            }
        }
    }

    public SettingsEditor() {
        super(new BorderLayout());
    }
}