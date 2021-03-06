package com.energyxxer.guardian.ui.dialogs.settings;

import com.energyxxer.guardian.global.temp.Lang;
import com.energyxxer.guardian.ui.editor.EditorModule;
import com.energyxxer.guardian.ui.editor.completion.snippets.Snippet;
import com.energyxxer.guardian.ui.editor.completion.snippets.SnippetContext;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.ui.orderlist.CompoundActionModuleToken;
import com.energyxxer.guardian.ui.orderlist.ItemAction;
import com.energyxxer.guardian.ui.orderlist.ItemCheckboxAction;
import com.energyxxer.guardian.ui.styledcomponents.*;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.Disposable;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.xswing.ComponentResizer;
import com.energyxxer.xswing.ScalableDimension;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashSet;

public class SnippetModuleToken implements CompoundActionModuleToken, Disposable {
    private final ThemeListenerManager tlm = new ThemeListenerManager();

    private final SettingsSnippets parent;
    private final StyledTextField shorthandField;
    private Snippet snippet;

    private JPanel settingsPanel;
    private EditorModule editor;

    public SnippetModuleToken(Snippet snippet, SettingsSnippets parent) {
        this.snippet = snippet;
        this.parent = parent;
        settingsPanel = new JPanel(new BorderLayout());
        settingsPanel.setMinimumSize(new ScalableDimension(1, 100));
        settingsPanel.setPreferredSize(new ScalableDimension(1, 200));
        ComponentResizer resizer = new ComponentResizer(settingsPanel);
        resizer.setEnabled(true);
        resizer.setResizable(true, false, false, false);


        tlm.addThemeChangeListener(t -> {
            settingsPanel.setBackground(t.getColor(new Color(235, 235, 235), "Settings.content.background"));
        });

        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.X_AXIS));
        summaryPanel.setOpaque(false);
        summaryPanel.setPreferredSize(new ScalableDimension(1, 40));
        summaryPanel.add(new Padding(30));
        summaryPanel.add(new StyledLabel("Shorthand: ", tlm));
        summaryPanel.add(shorthandField = new StyledTextField(snippet.getShorthand(), tlm) {
            {
                this.setMaximumSize(new ScalableDimension(Integer.MAX_VALUE, 25));
                this.getDocument().addUndoableEditListener(e -> {
                    Debug.log(this.getText());
                    snippet.setShorthand(this.getText());
                    parent.repaint();
                });
            }
        });
        summaryPanel.add(new Padding(30));
        summaryPanel.add(new StyledLabel("Description: ", tlm));
        summaryPanel.add(new StyledTextField(snippet.getDescription(), tlm) {
            {
                this.setMaximumSize(new ScalableDimension(Integer.MAX_VALUE, 25));
                this.getDocument().addUndoableEditListener(e -> {
                    snippet.setDescription(this.getText());
                    parent.repaint();
                });
            }
        });
        summaryPanel.add(new Padding(30));
        settingsPanel.add(summaryPanel, BorderLayout.NORTH);

        JPanel advancedPanel = new JPanel(new BorderLayout());
        advancedPanel.setOpaque(false);
        advancedPanel.add(new JPanel(new BorderLayout()) {
            {
                this.setOpaque(false);
                this.add(new Padding(30), BorderLayout.WEST);
                this.add(new StyledLabel("Expands into:", tlm));
            }
        }, BorderLayout.NORTH);
        editor = new EditorModule(null, null);
        editor.setForcedLanguage(Lang.SNIPPET);
        editor.updateSyntax();
        editor.setText(snippet.getText());
        editor.editorComponent.getDocument().addUndoableEditListener(e -> {
            if(!"style change".equals(e.getEdit().getPresentationName())) {
                snippet.setText(editor.getText());
            }
        });
        advancedPanel.add(editor, BorderLayout.CENTER);
        advancedPanel.add(new JPanel(new BorderLayout()) {
            StyledLabel contextLabel;
            {
                JPanel contextPanel = this;
                this.setPreferredSize(new ScalableDimension(200, 1));
                this.setMinimumSize(new ScalableDimension(200, 1));
                ComponentResizer contextResizer = new ComponentResizer(contextPanel);
                contextResizer.setEnabled(true);
                contextResizer.setResizable(false, true, false, false);
                this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                this.setOpaque(false);
                this.add(contextLabel = new StyledLabel(getApplicableLanguagesLabel(), tlm));
                //contextLabel.setBackgroundPainted(false);
                //contextLabel.setEditable(false);
                contextPanel.setOpaque(false);
                contextPanel.setBackground(new Color(0,0,0,0));
                this.add(new Padding(15));
                this.add(new StyledButton("Change", tlm) {
                    private int remainingHeight;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showContextMenu();
                    }

                    public void showContextMenu() {
                        StyledPopupMenu menu = new StyledPopupMenu("");
                        remainingHeight = contextPanel.getHeight()-2;
                        for(SnippetContext context : SnippetContext.values()) {
                            menu.add(new StyledMenuItem(context.getFriendlyName()) {
                                {
                                    this.setIconName(snippet.isContextEnabled(context) ? "checkmark" : "blank");
                                    this.setPreferredSize(new ScalableDimension(contextPanel.getWidth()-2, this.getPreferredSize().height));
                                    remainingHeight -= this.getPreferredSize().height;
                                }

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    snippet.setContextEnabled(context, !snippet.isContextEnabled(context));
                                    updateContextLabel();
                                    showContextMenu();
                                }
                            });
                        }
                        if(remainingHeight >= 1) {
                            menu.add(new JMenuItem() {
                                {
                                    this.setPreferredSize(new ScalableDimension(contextPanel.getWidth()-2, remainingHeight));
                                    this.setOpaque(false);
                                    this.setContentAreaFilled(false);
                                    this.setBorderPainted(false);
                                }
                            });
                        }
                        menu.show(contextPanel, 0, 0);
                    }
                });

                updateContextLabel();
            }

            private void updateContextLabel() {
                if(snippet.isEnabledEverywhere()) {
                    contextLabel.setText("<html>" + getApplicableLanguagesLabel() + "<pre>\n  Everywhere</pre></html>");
                } else {
                    StringBuilder sb = new StringBuilder("<html>" + getApplicableLanguagesLabel() + "<pre>");

                    boolean any = false;
                    for(SnippetContext context : snippet.getContexts()) {
                        any = true;
                        sb.append("\n  ");
                        sb.append(context.getFriendlyName());
                    }
                    if(!any) {
                        sb.setLength(0);
                        sb.append("<html>Applicable nowhere<pre>");
                    }
                    sb.append("</pre></html>");
                    contextLabel.setText(sb.toString());
                }
            }
        }, BorderLayout.EAST);
        settingsPanel.add(advancedPanel, BorderLayout.CENTER);
    }

    private String getApplicableLanguagesLabel() {
        StringBuilder sb = new StringBuilder("Applicable ");
        HashSet<Lang> languagesWithSnippets = SnippetContext.getLanguagesWithSnippets();
        if(languagesWithSnippets.isEmpty()) {
            sb.append("nowhere");
        } else {
            sb.append("in ");
            int i = 0;
            for(Lang lang : languagesWithSnippets) {
                if(i > 0) {
                    if(i < languagesWithSnippets.size()-1) {
                        sb.append(", ");
                    } else {
                        sb.append(" and ");
                    }
                }
                sb.append(lang.getFriendlyName());
                i++;
            }
        }
        sb.append(": ");
        return sb.toString();
    }

    @Override
    public java.util.@NotNull List<ItemAction> getActions() {
        return Collections.singletonList(new ItemCheckboxAction() {
            {
                leftAligned = true;
                setValue(snippet.isEnabled());
            }

            @Override
            public String getDescription() {
                return "Enabled";
            }

            @Override
            public void onChange(boolean newValue) {
                snippet.setEnabled(newValue);
            }
        });
    }

    @Override
    public String getTitle(TokenContext context) {
        return snippet.getShorthand();
    }

    @Override
    public Image getIcon() {
        return null;
    }

    @Override
    public StyledPopupMenu generateMenu(@NotNull ModuleToken.TokenContext context) {
        return null;
    }

    @Override
    public boolean equals(ModuleToken other) {
        return this == other;
    }

    @Override
    public String getSubTitle() {
        String desc = snippet.getDescription();
        return (desc != null && !desc.isEmpty()) ? "(" + desc + ")" : null;
    }

    @Override
    public void onInteract() {
        parent.setContent(settingsPanel);
        shorthandField.requestFocus();
        shorthandField.setSelectionStart(0);
        shorthandField.setSelectionEnd(shorthandField.getText().length());
    }

    public Snippet getSnippet() {
        return snippet;
    }

    @Override
    public void dispose() {
        editor.dispose();
        tlm.dispose();
    }
}
