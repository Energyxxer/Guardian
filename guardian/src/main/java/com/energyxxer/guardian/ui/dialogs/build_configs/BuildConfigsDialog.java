package com.energyxxer.guardian.ui.dialogs.build_configs;

import com.energyxxer.guardian.global.TabManager;
import com.energyxxer.guardian.global.temp.projects.BuildConfiguration;
import com.energyxxer.guardian.global.temp.projects.Project;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.Tab;
import com.energyxxer.guardian.ui.ToolbarButton;
import com.energyxxer.guardian.ui.dialogs.PromptDialog;
import com.energyxxer.guardian.ui.orderlist.OrderListElement;
import com.energyxxer.guardian.ui.orderlist.OrderListMaster;
import com.energyxxer.guardian.ui.orderlist.StandardOrderListItem;
import com.energyxxer.guardian.ui.scrollbar.InvisibleScrollPaneLayout;
import com.energyxxer.guardian.ui.scrollbar.OverlayScrollPane;
import com.energyxxer.guardian.ui.styledcomponents.StyledButton;
import com.energyxxer.guardian.ui.styledcomponents.StyledMenuItem;
import com.energyxxer.guardian.ui.styledcomponents.StyledPopupMenu;
import com.energyxxer.guardian.ui.tablist.TabListMaster;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.guardian.ui.user_configs.ConfigTab;
import com.energyxxer.guardian.ui.user_configs.ConfigTabDisplayModule;
import com.energyxxer.util.ImageManager;
import com.energyxxer.xswing.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class BuildConfigsDialog {

    private static JDialog dialog = new JDialog(GuardianWindow.jframe);
    //static Theme t;

    private static OrderListMaster configList;
    private static TabListMaster tabList;
    private static TabManager tabManager;
    private static JComponent currentView;

    private static JPanel mainContent;
    private static JPanel switcher;

    private static BuildConfigToken selectedConfig;

    private static ArrayList<File> filesToDelete = new ArrayList<>();
    private static Project<?> project;

    private static ArrayList<BuildConfigToken> templates = new ArrayList<>();

    private static boolean applied = false;

    private static ThemeListenerManager tlm = new ThemeListenerManager();

    static {
        JPanel pane = new JPanel(new OverlayBorderLayout());
        pane.setPreferredSize(new ScalableDimension(900,600));
        tlm.addThemeChangeListener(t ->
                pane.setBackground(t.getColor(new Color(235, 235, 235), "BuildConfigs.background"))
        );

        {
            JPanel sidebar = new OverlayBorderPanel(new BorderLayout(), new Insets(0, 0, 0, ComponentResizer.DIST));
            sidebar.setOpaque(false);

            ComponentResizer sidebarResizer = new ComponentResizer(sidebar);
            sidebar.setMinimumSize(new ScalableDimension(25, 1));
            sidebar.setMaximumSize(new ScalableDimension(400, 1));
            sidebarResizer.setResizable(false, false, false, true);
            sidebar.setPreferredSize(new ScalableDimension(200,1));
            tlm.addThemeChangeListener(t ->
                    sidebar.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, ComponentResizer.DIST), BorderFactory.createMatteBorder(0, 0, 0, Math.max(t.getInteger(1,"BuildConfigs.content.border.thickness"),0), t.getColor(new Color(200, 200, 200), "BuildConfigs.content.border.color"))))
            );

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
            buttons.setOpaque(false);
            buttons.setPreferredSize(new ScalableDimension(0,35));

            {
                ToolbarButton btn = new ToolbarButton("add", tlm);
                btn.setHintText("Add a build configuration");
                btn.addActionListener(a -> {
                    StyledPopupMenu menu = new StyledPopupMenu("-");

                    {
                        StyledMenuItem item = new StyledMenuItem("New Configuration");

                        item.addActionListener(aa -> {
                            String newName = new PromptDialog("Add build configuration", "Enter a name for the new build configuration", "Unnamed Configuration").result;
                            if(newName != null) {
                                if(newName.isEmpty()) newName = "Unnamed Configuration";
                                StandardOrderListItem element = new StandardOrderListItem(configList, new BuildConfigToken(newName, project.getBuildDirectory(), new JsonObject()));
                                configList.addItem(element);
                                configList.selectElement(element);
                            }
                        });

                        menu.add(item);
                    }

                    for(BuildConfigToken template : templates) {
                        StyledMenuItem item = new StyledMenuItem(template.getTitle(), "package");

                        item.addActionListener(aa -> {
                            StandardOrderListItem element = new StandardOrderListItem(configList, new BuildConfigToken(template.name, project.getBuildDirectory(), template.root.deepCopy()));
                            configList.addItem(element);
                            configList.selectElement(element);
                        });

                        menu.add(item);
                    }

                    menu.show(btn, 0, btn.getHeight());
                });
                buttons.add(btn);
            }

            {
                ToolbarButton btn = new ToolbarButton("toggle", tlm);
                btn.setHintText("Remove");
                btn.addActionListener(a -> {
                    if(selectedConfig == null) return;
                    OrderListElement elementToDelete = null;
                    for(OrderListElement element : configList.getAllElements()) {
                        if(((StandardOrderListItem) element).getToken() == selectedConfig) {
                            elementToDelete = element;
                            break;
                        }
                    }
                    configList.removeElement(elementToDelete);
                });
                buttons.add(btn);
            }

            {
                ToolbarButton btn = new ToolbarButton("rename", tlm);
                btn.setHintText("Rename");
                btn.addActionListener(a -> {
                    if(selectedConfig == null) return;
                    String newName = new PromptDialog("Rename build configuration", "Enter a new name for the build configuration", selectedConfig.name).result;
                    if(newName != null && !newName.isEmpty()) {
                        selectedConfig.name = newName;

                        for(OrderListElement element : configList.getAllElements()) {
                            if(((StandardOrderListItem) element).getToken() == selectedConfig) {
                                ((StandardOrderListItem) element).updateName();
                                configList.repaint();
                                break;
                            }
                        }
                    }
                });
                buttons.add(btn);
            }

            {
                ToolbarButton btn = new ToolbarButton("copy", tlm);
                btn.setHintText("Duplicate");
                btn.addActionListener(a -> {
                    if(selectedConfig == null) return;
                    StandardOrderListItem element = new StandardOrderListItem(configList, new BuildConfigToken("Copy of " + selectedConfig.name, project.getBuildDirectory(), selectedConfig.root.deepCopy()));
                    configList.addItem(element);
                    configList.selectElement(element);
                });
                buttons.add(btn);
            }

            sidebar.add(buttons, BorderLayout.NORTH);

            configList = new OrderListMaster();
            OverlayScrollPane scrollPane = new OverlayScrollPane(tlm, configList);
            configList.addSelectionListener((o, n) -> {
                setSelected(n != null ? ((BuildConfigToken) ((StandardOrderListItem) n).getToken()) : null);
            });
            sidebar.add(scrollPane, BorderLayout.CENTER);

            pane.add(sidebar, BorderLayout.WEST);
        }

        tlm.addThemeChangeListener(t ->
                pane.setBackground(t.getColor(new Color(235, 235, 235), "BuildConfigs.background"))
        );

//        HashMap<String, JPanel> sectionPanes = new LinkedHashMap<>();
//
//        BuildConfigsBehavior contentBehavior = new BuildConfigsBehavior();
//        sectionPanes.put("Behavior", contentBehavior);
//        sectionPanes.put("Appearance", new BuildConfigsAppearance());
//        sectionPanes.put("Editor", new BuildConfigsEditor());
//        sectionPanes.put("Snippets", new BuildConfigsSnippets());
//        sectionPanes.put("Keymap", new BuildConfigsKeymap());
//        Guardian.core.setupBuildConfigsSections(sectionPanes);
//        Set<String> keySet = sectionPanes.keySet();
//
//        {
//            JPanel sidebar = new OverlayBorderPanel(new BorderLayout(), new Insets(0, 0, 0, ComponentResizer.DIST));
//
//            ComponentResizer sidebarResizer = new ComponentResizer(sidebar);
//            sidebar.setMinimumSize(new ScalableDimension(25, 1));
//            sidebar.setMaximumSize(new ScalableDimension(400, 1));
//            sidebarResizer.setResizable(false, false, false, true);
//
//            String[] sections = keySet.toArray(new String[0]);
//
//            StyledList<String> navigator = new StyledList<>(sections, "BuildConfigs");
//            sidebar.setBackground(navigator.getBackground());
//            tlm.addThemeChangeListener(t ->
//                    sidebar.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, ComponentResizer.DIST), BorderFactory.createMatteBorder(0, 0, 0, Math.max(t.getInteger(1,"BuildConfigs.content.border.thickness"),0), t.getColor(new Color(200, 200, 200), "BuildConfigs.content.border.color"))))
//            );
//            sidebar.setOpaque(false);
//            navigator.setPreferredSize(new ScalableDimension(200,500));
//
//            navigator.addListSelectionListener(o -> {
//                contentPane.remove(currentSection);
//                currentSection = sectionPanes.get(sections[o.getFirstIndex()]);
//                contentPane.add(currentSection, BorderLayout.CENTER);
//                contentPane.repaint();
//            });
//
//            sidebar.add(navigator, BorderLayout.CENTER);
//
//            pane.add(sidebar, BorderLayout.WEST);
//        }
//
//        tlm.addThemeChangeListener(t ->
//                contentPane.setBackground(t.getColor(new Color(235, 235, 235), "BuildConfigs.content.background"))
//        );
//        pane.add(contentPane, BorderLayout.CENTER);
//
//        contentPane.add(contentBehavior, BorderLayout.CENTER);
//        currentSection = contentBehavior;
//
        {
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttons.setPreferredSize(new ScalableDimension(0,60));
            tlm.addThemeChangeListener(t -> buttons.setBackground(pane.getBackground()));

            {
                StyledButton okay = new StyledButton("OK", "BuildConfigs.okButton", tlm);
                tlm.addThemeChangeListener(t -> okay.setPreferredSize(new ScalableDimension(Math.max(t.getInteger(75,"BuildConfigs.okButton.width"),10), Math.max(t.getInteger(25,"BuildConfigs.okButton.height"),10))));
                buttons.add(okay);

                okay.addActionListener(e -> {
                    dialog.setVisible(false);
                    apply();
                    close();
                });
            }

            {
                StyledButton cancel = new StyledButton("Cancel", "BuildConfigs.cancelButton", tlm);
                tlm.addThemeChangeListener(t -> cancel.setPreferredSize(new ScalableDimension(Math.max(t.getInteger(75,"BuildConfigs.cancelButton.width"),10), Math.max(t.getInteger(25,"BuildConfigs.cancelButton.height"),10))));
                buttons.add(cancel);

                pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
                pane.getActionMap().put("cancel", new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        cancel();
                    }
                });

                cancel.addActionListener(e -> {cancel();});
            }

            {
                StyledButton apply = new StyledButton("Apply", "BuildConfigs.applyButton", tlm);
                tlm.addThemeChangeListener(t -> apply.setPreferredSize(new ScalableDimension(Math.max(t.getInteger(75,"BuildConfigs.applyButton.width"),10), Math.max(t.getInteger(25,"BuildConfigs.applyButton.height"),10))));
                buttons.add(apply);

                apply.addActionListener(e -> {
                    apply();
                });
            }
            buttons.add(new Padding(25));

            pane.add(buttons, BorderLayout.SOUTH);
        }

        {
            mainContent = new JPanel(new BorderLayout());

            tlm.addThemeChangeListener(t ->
                    mainContent.setBackground(t.getColor(new Color(235, 235, 235), "BuildConfigs.content.background"))
            );

            pane.add(mainContent, BorderLayout.CENTER);

            switcher = new JPanel(new BorderLayout());
            switcher.setOpaque(false);

            tabList = new TabListMaster();
            tabList.setMayRearrange(false);
            tabManager = new TabManager(tabList, c -> {
                if(currentView != null) {
                    switcher.remove(currentView);
                }
                currentView = c;
                if(c != null) {
                    switcher.add(currentView);
                }

                switcher.revalidate();
                switcher.repaint();
            });


            JScrollPane tabSP = new JScrollPane(tabList);
            tabSP.setBorder(BorderFactory.createEmptyBorder());
            tabSP.setLayout(new InvisibleScrollPaneLayout(tabSP));
            tabSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

            switcher.add(tabSP, BorderLayout.NORTH);
            currentView = new JPanel();
            switcher.add(currentView);
        }
        dialog.setContentPane(pane);
        dialog.pack();

        dialog.setTitle("Build Configurations");
        dialog.setIconImage(ImageManager.load("/assets/icons/ui/settings.png").getScaledInstance(16, 16, Image.SCALE_SMOOTH));

        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        center.x -= dialog.getWidth()/2;
        center.y -= dialog.getHeight()/2;

        dialog.setLocation(center);

        dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
    }

    private static void cancel() {
        dialog.setVisible(false);
        close();
    }

    public static void show(Project<?> project) {
        configList.requestFocus();
        configList.removeAllElements();
        BuildConfigsDialog.project = project;

        filesToDelete.clear();
        applied = false;

        StandardOrderListItem startSelected = null;
        BuildConfiguration<?> activeBuildConfig = project.getBuildConfig();

        for(BuildConfiguration<?> config : project.getAllBuildConfigs()) {
            StandardOrderListItem element = new StandardOrderListItem(configList, new BuildConfigToken(config));
            if(config == activeBuildConfig) {
                startSelected = element;
            }
            configList.addItem(element);
            filesToDelete.add(config.file);
        }

        tabManager.closeAllTabs(true);

        for(ConfigTab tab : project.getBuildConfigTabs()) {
            tabManager.openTab(tab);
        }
        if(tabManager.openTabs.size() > 0) tabManager.setSelectedTab(tabManager.openTabs.get(0));

        if(startSelected != null) configList.selectElement(startSelected);

        //list templates
        templates.clear();
        findTemplates(project);
        for(Project<?> dependency : project.getLoadedDependencies(new ArrayList<>(), true)) {
            findTemplates(dependency);
        }
        templates.sort(Comparator.comparingInt(c -> c.originalSortIndex));

        dialog.setTitle("Build Configurations for project \"" + project.getName() + "\"");
        switcher.revalidate();
        dialog.setVisible(true);
    }

    private static void findTemplates(Project<?> project) {
        File dir = project.getBuildTemplateDirectory();
        if(dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if(files != null) {
                for(File file : files) {
                    if(file.isFile() && (file.getName().endsWith(".build") || file.getName().endsWith(".build.guardiantemplate"))) {
                        try {
                            templates.add(new BuildConfigToken(file, project.getRootDirectory(), BuildConfigsDialog.project.getRootDirectory()));
                        } catch (IOException | JsonSyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private static void apply() {
        if(selectedConfig != null) {
            for(Tab tab : tabManager.openTabs) {
                ((ConfigTabDisplayModule) tab.module).apply(selectedConfig.traverser);
                ((ConfigTabDisplayModule) tab.module).open(selectedConfig.traverser);
            }
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        int i = 0;
        for(OrderListElement element : configList.getAllElements()) {
            BuildConfigToken token = ((BuildConfigToken) ((StandardOrderListItem) element).getToken());
            token.setSortIndex(i);
            token.writeToFile(gson);
            filesToDelete.remove(token.file);
            i++;
        }

        for(File file : filesToDelete) {
            file.delete();
        }

        //Regenerate the list of created files in case this is an apply-apply case
        filesToDelete.clear();
        for(OrderListElement element : configList.getAllElements()) {
            BuildConfigToken token = ((BuildConfigToken) ((StandardOrderListItem) element).getToken());
            filesToDelete.add(token.file);
        }

        applied = true;
    }

    private static void close() {

        selectedConfig = null;
        mainContent.remove(switcher);
        configList.removeAllElements();
        templates.clear();
        filesToDelete.clear();
        tabManager.closeAllTabs(true);
        if(applied) project.refreshBuildConfigs();
        project = null;

    }

    public static void setSelected(BuildConfigToken newSelectedConfig) {
        if(selectedConfig != null) {
            for(Tab tab : tabManager.openTabs) {
                ((ConfigTabDisplayModule) tab.module).apply(selectedConfig.traverser);
            }
            if(newSelectedConfig == null) {
                mainContent.remove(switcher);
                dialog.getContentPane().revalidate();
                dialog.getContentPane().repaint();
            }
        }
        if(newSelectedConfig != null) {
            if(selectedConfig == null) {
                mainContent.add(switcher, BorderLayout.CENTER);
                dialog.getContentPane().revalidate();
                dialog.getContentPane().repaint();
            }
            for(Tab tab : tabManager.openTabs) {
                ((ConfigTabDisplayModule) tab.module).open(newSelectedConfig.traverser);
            }
        }
        selectedConfig = newSelectedConfig;
    }
}
