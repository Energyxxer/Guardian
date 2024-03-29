package com.energyxxer.guardian.ui.dialogs.settings;

import com.energyxxer.guardian.global.TabManager;
import com.energyxxer.guardian.ui.commodoreresources.DefinitionUpdateProcess;
import com.energyxxer.guardian.ui.common.ProgramUpdateProcess;
import com.energyxxer.guardian.ui.explorer.ProjectExplorerMaster;
import com.energyxxer.guardian.ui.explorer.base.StandardExplorerItem;
import com.energyxxer.guardian.ui.modules.FileModuleToken;
import com.energyxxer.guardian.ui.scrollbar.OverlayScrollPane;
import com.energyxxer.guardian.ui.styledcomponents.StyledCheckBox;
import com.energyxxer.guardian.ui.styledcomponents.StyledLabel;
import com.energyxxer.guardian.ui.styledcomponents.StyledTextField;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.Padding;
import com.energyxxer.xswing.ScalableDimension;

import javax.swing.*;
import java.awt.*;

class SettingsBehavior extends JPanel {

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

            StyledLabel label = new StyledLabel("Behavior","Settings.content.header", tlm);
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
                JPanel padding = new JPanel();
                padding.setOpaque(false);
                padding.setMinimumSize(new ScalableDimension(1,20));
                padding.setMaximumSize(new ScalableDimension(1,20));
                content.add(padding);
            }

            {
                StyledLabel label = new StyledLabel("Updates","Settings.content", tlm);
                label.setStyle(Font.BOLD);
                content.add(label);
            }
            {
                StyledCheckBox checkProgramUpdates = new StyledCheckBox("Check for program updates on startup","Settings.content");
                checkProgramUpdates.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> checkProgramUpdates.setSelected(ProgramUpdateProcess.CHECK_FOR_PROGRAM_UPDATES_STARTUP.get()));
                Settings.addApplyEvent(() -> ProgramUpdateProcess.CHECK_FOR_PROGRAM_UPDATES_STARTUP.set(checkProgramUpdates.isSelected()));

                content.add(checkProgramUpdates);
            }
            {
                StyledCheckBox checkDefinitionUpdates = new StyledCheckBox("Check for definition updates on startup","Settings.content");
                checkDefinitionUpdates.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> checkDefinitionUpdates.setSelected(DefinitionUpdateProcess.CHECK_FOR_DEF_UPDATES_STARTUP.get()));
                Settings.addApplyEvent(() -> DefinitionUpdateProcess.CHECK_FOR_DEF_UPDATES_STARTUP.set(checkDefinitionUpdates.isSelected()));

                content.add(checkDefinitionUpdates);
            }

            {
                content.add(new Padding(20));
            }

            {
                StyledLabel label = new StyledLabel("Persistence","Settings.content", tlm);
                label.setStyle(Font.BOLD);
                content.add(label);
            }
            //region Save Open
            {
                StyledCheckBox saveOpenTabs = new StyledCheckBox("Save open tabs","Settings.content");
                saveOpenTabs.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> saveOpenTabs.setSelected(TabManager.SAVE_OPEN_TABS.get()));
                Settings.addApplyEvent(() -> TabManager.SAVE_OPEN_TABS.set(saveOpenTabs.isSelected()));

                content.add(saveOpenTabs);
            }
            {
                StyledCheckBox saveExplorerTree = new StyledCheckBox("Save open project tree","Settings.content");
                saveExplorerTree.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> saveExplorerTree.setSelected(ProjectExplorerMaster.SAVE_EXPLORER_TREE.get()));
                Settings.addApplyEvent(() -> ProjectExplorerMaster.SAVE_EXPLORER_TREE.set(saveExplorerTree.isSelected()));

                content.add(saveExplorerTree);
            }

            {
                content.add(new Padding(20));
            }

            {
                StyledLabel label = new StyledLabel("File Presentation","Settings.content", tlm);
                label.setStyle(Font.BOLD);
                content.add(label);
            }
            {
                StyledCheckBox loadPNGs = new StyledCheckBox("Load images for .png file icons","Settings.content");
                loadPNGs.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> loadPNGs.setSelected(FileModuleToken.LOAD_PNGS.get()));
                Settings.addApplyEvent(() -> FileModuleToken.LOAD_PNGS.set(loadPNGs.isSelected()));

                content.add(loadPNGs);
            }
            {
                StyledCheckBox showExtensions = new StyledCheckBox("Show file extension on project explorer","Settings.content");
                showExtensions.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> showExtensions.setSelected(FileModuleToken.SHOW_EXTENSIONS_EXPLORER.get()));
                Settings.addApplyEvent(() -> FileModuleToken.SHOW_EXTENSIONS_EXPLORER.set(showExtensions.isSelected()));

                content.add(showExtensions);
            }
            {
                StyledCheckBox showExtensions = new StyledCheckBox("Show file extension on tabs","Settings.content");
                showExtensions.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> showExtensions.setSelected(FileModuleToken.SHOW_EXTENSIONS_TAB.get()));
                Settings.addApplyEvent(() -> FileModuleToken.SHOW_EXTENSIONS_TAB.set(showExtensions.isSelected()));

                content.add(showExtensions);
            }
            if(FileModuleToken.MOVE_TO_TRASH_VERSION_AVAILABLE) {
                {
                    content.add(new Padding(15));
                }
                {
                    StyledCheckBox showExtensions = new StyledCheckBox("Move to Trash on file delete","Settings.content");
                    showExtensions.setAlignmentX(Component.LEFT_ALIGNMENT);
                    Settings.addOpenEvent(() -> showExtensions.setSelected(FileModuleToken.DELETE_MOVES_TO_TRASH.get()));
                    Settings.addApplyEvent(() -> FileModuleToken.DELETE_MOVES_TO_TRASH.set(showExtensions.isSelected()));

                    content.add(showExtensions);
                }
            }
            {
                {
                    content.add(new Padding(15));
                }
                {
                    StyledCheckBox singleClickInteract = new StyledCheckBox("Single-click explorer interact","Settings.content");
                    singleClickInteract.setAlignmentX(Component.LEFT_ALIGNMENT);
                    Settings.addOpenEvent(() -> singleClickInteract.setSelected(StandardExplorerItem.ONE_CLICK_OPEN.get()));
                    Settings.addApplyEvent(() -> StandardExplorerItem.ONE_CLICK_OPEN.set(singleClickInteract.isSelected()));

                    content.add(singleClickInteract);
                }

                {
                    StyledLabel label = new StyledLabel("If checked, files will open and folders will expand with a single click.","Settings.content", tlm);
                    label.setStyle(Font.ITALIC);
                    content.add(label);
                }
                {
                    StyledLabel label = new StyledLabel("Also applies to other explorer elements such as code suggestions.","Settings.content", tlm);
                    label.setStyle(Font.ITALIC);
                    content.add(label);
                }
            }

            {
                content.add(new Padding(30));
            }




            {
                StyledLabel label = new StyledLabel("Tab Limit:","Settings.content", tlm);
                label.setStyle(Font.BOLD);
                content.add(label);
            }
            {
                StyledTextField tabLimitField = new StyledTextField("","Settings.content", tlm);
                tabLimitField.setMaximumSize(new ScalableDimension(300,25));
                tabLimitField.setAlignmentX(Component.LEFT_ALIGNMENT);
                Settings.addOpenEvent(() -> tabLimitField.setText("" + TabManager.TAB_LIMIT.get()));
                Settings.addApplyEvent(() -> {
                    try {
                        int limit = Integer.parseInt(tabLimitField.getText());
                        if(limit >= 0) {
                            TabManager.TAB_LIMIT.set(limit);
                        }
                    } catch(NumberFormatException ignore) {}
                });
                content.add(tabLimitField);
            }
            {
                StyledLabel label = new StyledLabel("When the number of tabs exceed the limit, the oldest unopened","Settings.content", tlm);
                label.setStyle(Font.ITALIC);
                content.add(label);
            }
            {
                StyledLabel label = new StyledLabel("tabs will be closed. This will never close unsaved tabs.","Settings.content", tlm);
                label.setStyle(Font.ITALIC);
                content.add(label);
            }
            {
                StyledLabel label = new StyledLabel("Enter 0 for no limit.","Settings.content", tlm);
                content.add(label);
            }
            //endregion

        }
    }

    public SettingsBehavior() {
        super(new BorderLayout());
    }
}
