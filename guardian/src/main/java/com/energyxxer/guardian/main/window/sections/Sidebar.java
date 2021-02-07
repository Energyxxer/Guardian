package com.energyxxer.guardian.main.window.sections;

import com.energyxxer.guardian.global.Preferences;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.main.window.actions.ActionManager;
import com.energyxxer.guardian.ui.ToolbarButton;
import com.energyxxer.guardian.ui.explorer.ProjectExplorerMaster;
import com.energyxxer.guardian.ui.scrollbar.OverlayScrollPaneLayout;
import com.energyxxer.guardian.ui.styledcomponents.Padding;
import com.energyxxer.guardian.ui.styledcomponents.StyledLabel;
import com.energyxxer.guardian.ui.styledcomponents.StyledMenuItem;
import com.energyxxer.guardian.ui.styledcomponents.StyledPopupMenu;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.ComponentResizer;
import com.energyxxer.xswing.OverlayBorderPanel;
import com.energyxxer.xswing.ScalableDimension;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created by User on 12/15/2016.
 */
public class Sidebar extends OverlayBorderPanel {

    private ThemeListenerManager tlm = new ThemeListenerManager();

    private JPanel expanded = new JPanel(new BorderLayout());
    private JPanel collapsed = new JPanel(new BorderLayout());

    public Sidebar() {
        super(new BorderLayout(), new Insets(0, 0, 0, ComponentResizer.DIST));
        this.setOpaque(false);
    }

    {

        expanded.setPreferredSize(new ScalableDimension(350, 5));

        tlm.addThemeChangeListener(t -> {
            expanded.setMinimumSize(new ScalableDimension(50, 0));
            expanded.setMaximumSize(new ScalableDimension(700, 0));
            expanded.setBackground(t.getColor(Color.WHITE, "Explorer.background"));
            expanded.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, ComponentResizer.DIST), BorderFactory.createMatteBorder(0, 0, 0, Math.max(t.getInteger(1, "Explorer.border.thickness"), 0), t.getColor(new Color(200, 200, 200), "Explorer.border.color"))));
            expanded.setOpaque(false);
        });

        JPanel header = new JPanel(new BorderLayout());

        StyledLabel label = new StyledLabel("Project Explorer", "Explorer.header", tlm);
        label.setFontSize(14);
        label.setPreferredSize(new ScalableDimension(500, 25));
        header.add(new Padding(15, tlm, "Explorer.header.indent"), BorderLayout.WEST);
        header.add(label, BorderLayout.CENTER);

        tlm.addThemeChangeListener(t -> {
            header.setBackground(t.getColor(this.getBackground(), "Explorer.header.background"));
            header.setPreferredSize(new ScalableDimension(500, t.getInteger(25, "Explorer.header.height")));
            label.setPreferredSize(new ScalableDimension(500, t.getInteger(25, "Explorer.header.height")));
        });

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);

        //((FlowLayout) buttonPanel.getLayout()).setHgap(2);

        header.add(buttonPanel, BorderLayout.EAST);

        {
            ToolbarButton refresh = new ToolbarButton("reload", tlm);
            refresh.setHintText("Refresh Explorer");

            refresh.addActionListener(e -> ActionManager.getAction("RELOAD_WORKSPACE").perform());

            buttonPanel.add(refresh);
        }

        {
            ToolbarButton configure = new ToolbarButton("cog_dropdown", tlm);
            configure.setHintText("Configure");

            configure.addActionListener(e -> {
                StyledPopupMenu menu = new StyledPopupMenu("What is supposed to go here?");/*

                {
                    StyledMenuItem item = new StyledMenuItem("Flatten Empty Packages", "checkmark");
                    item.setIconName(GuardianWindow.projectExplorer.getFlag(ProjectExplorerMaster.FLATTEN_EMPTY_PACKAGES) ? "checkmark" : "blank");

                    item.addActionListener(aa -> {
                        GuardianWindow.projectExplorer.toggleFlag(ProjectExplorerMaster.FLATTEN_EMPTY_PACKAGES);
                        GuardianWindow.projectExplorer.refresh();

                        Preferences.put("explorer.flatten_empty_packages",Boolean.toString(GuardianWindow.projectExplorer.getFlag(ProjectExplorerMaster.FLATTEN_EMPTY_PACKAGES)));
                    });

                    menu.add(item);
                }*/

                {
                    StyledMenuItem item = new StyledMenuItem("Show Project Files", "checkmark");
                    item.setIconName(GuardianWindow.projectExplorer.getFlag(ProjectExplorerMaster.SHOW_PROJECT_FILES) ? "checkmark" : "blank");

                    item.addActionListener(aa -> {
                        GuardianWindow.projectExplorer.toggleFlag(ProjectExplorerMaster.SHOW_PROJECT_FILES);
                        GuardianWindow.projectExplorer.refresh();

                        Preferences.put("explorer.show_project_files",Boolean.toString(GuardianWindow.projectExplorer.getFlag(ProjectExplorerMaster.SHOW_PROJECT_FILES)));
                    });
                    menu.add(item);
                }

                /*{
                    StyledMenuItem item = new StyledMenuItem("Debug Width", "checkmark");
                    item.setIconName(GuardianWindow.projectExplorer.getFlag(ExplorerFlag.DEBUG_WIDTH) ? "checkmark" : "blank");

                    item.addActionListener(aa -> {
                        GuardianWindow.projectExplorer.toggleFlag(ExplorerFlag.DEBUG_WIDTH);

                        Preferences.put("explorer.debug_width",Boolean.toString(GuardianWindow.projectExplorer.getFlag(ExplorerFlag.DEBUG_WIDTH)));
                    });
                    menu.add(item);
                }*/

                menu.show(configure, configure.getWidth()/2, configure.getHeight());

                GuardianWindow.projectExplorer.refresh();
            });

            buttonPanel.add(configure);
        }

        {
            ToolbarButton collapse = new ToolbarButton("arrow_left", tlm);
            collapse.setHintText("Collapse Explorer");

            collapse.addActionListener(e -> collapse());

            buttonPanel.add(collapse);
        }

        expanded.add(header, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(GuardianWindow.projectExplorer = new ProjectExplorerMaster());
        sp.setBorder(new EmptyBorder(0, 0, 0, 0));
        sp.setLayout(new OverlayScrollPaneLayout(sp, tlm));

        expanded.add(sp, BorderLayout.CENTER);
    }
    {

        tlm.addThemeChangeListener(t -> {
            collapsed.setPreferredSize(new ScalableDimension(29, 50));
            collapsed.setBackground(t.getColor(Color.WHITE, "Explorer.background"));
            collapsed.setBorder(BorderFactory.createMatteBorder(0, 0, 0, Math.max(t.getInteger(1, "Explorer.border.thickness"), 0), t.getColor(new Color(200, 200, 200), "Explorer.border.color")));
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);

        collapsed.add(buttonPanel, BorderLayout.NORTH);

        {
            ToolbarButton expand = new ToolbarButton("arrow_right", tlm);
            expand.setHintText("Expand Explorer");

            expand.addActionListener(e -> expand());

            buttonPanel.add(expand);
        }

        if(Preferences.get("explorer.expanded", "true").equals("true")) {
            expand();
        } else {
            collapse();
        }

        ComponentResizer sidebarResizer = new ComponentResizer(expanded);
        sidebarResizer.setResizable(false, false, false, true);
    }

    public void expand() {
        this.removeAll();
        this.add(expanded, BorderLayout.CENTER);
        this.getOverlayInsets().right = ComponentResizer.DIST;
        update();


        Preferences.put("explorer.expanded", "true");
    }

    public void collapse() {
        this.removeAll();
        this.add(collapsed, BorderLayout.CENTER);
        this.getOverlayInsets().right = 0;
        update();


        Preferences.put("explorer.expanded", "false");
    }

    private void update() {
        this.revalidate();
        this.repaint();

        if(GuardianWindow.welcomePane != null) {
            GuardianWindow.welcomePane.revalidate();
            GuardianWindow.welcomePane.repaint();
        }
    }
}
