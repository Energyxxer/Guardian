package com.energyxxer.guardian.main.window.sections;

import com.energyxxer.guardian.global.Commons;
import com.energyxxer.guardian.global.TabManager;
import com.energyxxer.guardian.global.temp.projects.BuildConfiguration;
import com.energyxxer.guardian.global.temp.projects.Project;
import com.energyxxer.guardian.global.temp.projects.ProjectManager;
import com.energyxxer.guardian.langinterface.ProjectType;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.main.window.actions.ActionManager;
import com.energyxxer.guardian.main.window.actions.ProgramAction;
import com.energyxxer.guardian.main.window.sections.toolbar.PathViewToken;
import com.energyxxer.guardian.ui.ToolbarButton;
import com.energyxxer.guardian.ui.ToolbarSeparator;
import com.energyxxer.guardian.ui.styledcomponents.StyledDropdownMenu;
import com.energyxxer.guardian.ui.tablist.TabListMaster;
import com.energyxxer.guardian.ui.tablist.TabSeparator;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.xswing.Padding;
import com.energyxxer.xswing.ScalableDimension;
import com.energyxxer.xswing.hints.TextHint;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by User on 12/15/2016.
 */
public class Toolbar extends JPanel {

    public TextHint hint = GuardianWindow.hintManager.createTextHint("");
    private TabSeparator sharedSeparator = null;

    public ThemeListenerManager tlm;

    private File lastActiveFile = null;
    private Path lastActiveProjectRoot = null;

    public void updateActiveFile() {
        File activeFile = lastActiveFile;
        lastActiveFile = null;
        lastActiveProjectRoot = null;
        setActiveFile(activeFile);
    }

    public void setActiveFile(File file) {
        Project activeProject = Commons.getActiveProject();
        Path activeProjectRoot = activeProject != null ? activeProject.getRootDirectory().toPath() : null;
        if(Objects.equals(lastActiveFile, file) && Objects.equals(lastActiveProjectRoot, activeProjectRoot)) {
            return;
        }
        lastActiveFile = file;
        pathIndicatorTabManager.closeAllTabs(true);
        pathIndicatorTabManager.getTabList().removeAllTabs();
        if(file != null) {
            Project<?> associatedProject = ProjectManager.getAssociatedProject(file);

            Path projectRoot = associatedProject != null ? associatedProject.getRootDirectory().toPath() : null;

            Path shownPath = file.toPath();

            if(projectRoot != null) {
                shownPath = projectRoot.relativize(shownPath);
            }
            for(int i = 0; i <= shownPath.getNameCount(); i++) {
                File fileToShow;
                if(i == 0) {
                    if(projectRoot != null) fileToShow = projectRoot.toFile();
                    else fileToShow = shownPath.getRoot().toFile();
                } else {
                    if(projectRoot != null) fileToShow = projectRoot.resolve(shownPath.subpath(0, i)).toFile();
                    else fileToShow = shownPath.getRoot().resolve(shownPath.subpath(0, i)).toFile();
                }
                pathIndicatorTabManager.openTab(new PathViewToken(fileToShow, associatedProject == activeProject));
                if(i != shownPath.getNameCount()) {
                    pathIndicatorTabManager.getTabList().addTab(sharedSeparator);
                }
            }
        }
        {
            Path projectRoot = null;
            if(activeProject == null || !Objects.equals(lastActiveProjectRoot, projectRoot = activeProject.getRootDirectory().toPath())) {
                lastActiveProjectRoot = projectRoot;
                updateBuildConfigs(activeProject);
            }
        }
    }

    private final TabManager pathIndicatorTabManager;
    private final StyledDropdownMenu<BuildConfigDropdownItem> buildConfigDropdown;
    
    {
        this.tlm = new ThemeListenerManager();
        this.hint.setOutDelay(1);

        int defaultHeight = 29;

        this.setPreferredSize(new ScalableDimension(1, defaultHeight));
        this.setLayout(new BorderLayout());

        JPanel projectIndicator = new JPanel(new GridBagLayout());
        //projectIndicator.setOpaque(false);

        //this.add(projectIndicator, BorderLayout.WEST);

        projectIndicator.add(new Padding(10));

        TabListMaster pathIndicatorList = new TabListMaster("Toolbar.pathIndicator");
        this.pathIndicatorTabManager = new TabManager(pathIndicatorList, c -> {});
        pathIndicatorTabManager.setChangeWindowInfo(false);
        pathIndicatorTabManager.setOverrideTabLimit(-1);
        sharedSeparator = new TabSeparator(pathIndicatorList);
        pathIndicatorList.setMayRearrange(false);

        projectIndicator.add(new Padding(10));
        this.add(pathIndicatorList, BorderLayout.WEST);
        //projectIndicator.add(pathIndicatorList);


        JPanel buttonBar = new JPanel(new GridBagLayout());
        buttonBar.setOpaque(false);
        this.add(buttonBar, BorderLayout.EAST);

        {
            buttonBar.add(buildConfigDropdown = new StyledDropdownMenu<>("Toolbar"));
            buildConfigDropdown.addChoiceListener(this::updateBuildConfig);
            buttonBar.add(new Padding(10));
        }

        {
            buttonBar.add(createButtonForAction("COMPILE"));
        }

        buttonBar.add(new ToolbarSeparator(tlm));

        {
            buttonBar.add(createButtonForAction("SAVE"));
        }

        {
            buttonBar.add(createButtonForAction("SAVE_ALL"));
        }

        buttonBar.add(new ToolbarSeparator(tlm));

        buttonBar.add(createButtonForAction("DOCUMENTATION"));

        buttonBar.add(new ToolbarSeparator(tlm));

        for(ProjectType type : ProjectType.values()) {
            buttonBar.add(createButtonForAction("NEW_PROJECT_" + type.getCode()));
        }

        {
            buttonBar.add(createButtonForAction("PROJECT_PROPERTIES"));
        }

        {
            buttonBar.add(createButtonForAction("SEARCH_EVERYWHERE"));
        }

        buttonBar.add(new Padding(10));

        tlm.addThemeChangeListener(t -> {
            this.setBackground(t.getColor(new Color(235, 235, 235), "Toolbar.background"));
            this.setBorder(BorderFactory.createMatteBorder(0, 0, Math.max(t.getInteger(1,"Toolbar.border.thickness"),0), 0, t.getColor(new Color(200, 200, 200), "Toolbar.border.color")));

            int height = t.getInteger(29, "Toolbar.height");

            this.setPreferredSize(new ScalableDimension(1, height));
        });
    }

    private void updateBuildConfigs(Project<?> associatedProject) {
        if(associatedProject != null) {
            ArrayList<? extends BuildConfiguration<?>> allConfigs = associatedProject.getAllBuildConfigs();

            BuildConfigDropdownItem[] options = new BuildConfigDropdownItem[allConfigs.size()+1];
            options[0] = new BuildConfigDropdownItem(); //Edit Configurations

            for(int i = 0; i < allConfigs.size(); i++) {
                BuildConfigDropdownItem configItem = new BuildConfigDropdownItem(allConfigs.get(i));
                options[i+1] = configItem;
            }
            buildConfigDropdown.setOptions(options);

            //Set cog icons
            for(int i = 1; i < options.length; i++) {
                buildConfigDropdown.setIcon(i, Commons.getIcon("cog"));
            }

        } else {
            buildConfigDropdown.setOptions(new BuildConfigDropdownItem[] {new BuildConfigDropdownItem()});
        }
        buildConfigDropdown.setFallbackIcon(Commons.getIcon("cog_dropdown"));

        updateBuildConfigSelected(associatedProject);
    }

    private void updateBuildConfigSelected(Project project) {
        if(project != null) {
            BuildConfiguration<?> activeConfig = project.getBuildConfig();
            ArrayList<BuildConfigDropdownItem> options = buildConfigDropdown.getOptions();
            boolean anyConfigs = false;
            if(!activeConfig.isFallback()) {
                for(BuildConfigDropdownItem option : options) {
                    if(option.config != null) anyConfigs = true;
                    if(option.config == activeConfig) {
                        buildConfigDropdown.setValue(option);
                        break;
                    }
                }
            }
            if(!anyConfigs) {
                buildConfigDropdown.setText("[ No Configurations ]");
            }
        } else {
            buildConfigDropdown.setText("[ No Project Selected ]");
        }
    }

    private void updateBuildConfig(BuildConfigDropdownItem buildConfiguration) {
        if(buildConfiguration.config == null) {
            Debug.log("EDIT");
            updateBuildConfigSelected(ProjectManager.getAssociatedProject(lastActiveProjectRoot.toFile()));
            return;
        }
        if(lastActiveProjectRoot != null) {
            Project project = ProjectManager.getAssociatedProject(lastActiveProjectRoot.toFile());
            if(project != null) {
                //noinspection unchecked
                project.setActiveBuildConfig(buildConfiguration.config);
            }
        }
    }

    private ToolbarButton createButtonForAction(String actionKey) {
        ProgramAction action = ActionManager.getAction(actionKey);
        ToolbarButton button = new ToolbarButton(action.getIconKey(),tlm);
        String title = action.getTitle();
        if(action.getShortcut() != null && action.getShortcut().getFirstMapping() != null) {
            title += " (" + action.getShortcut().getFirstMapping().getHumanReadableName() + ")";
        }
        button.setHintText(title);
        button.addActionListener(e -> action.perform());
        return button;
    }

    private static class BuildConfigDropdownItem {
        public BuildConfiguration<?> config;

        public BuildConfigDropdownItem() {

        }

        public BuildConfigDropdownItem(BuildConfiguration<?> config) {
            this.config = config;
        }

        @Override
        public String toString() {
            return config != null ? config.name : "Edit Configurations...";
        }
    }
}
