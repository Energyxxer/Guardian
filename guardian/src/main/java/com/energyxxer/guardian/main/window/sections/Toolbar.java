package com.energyxxer.guardian.main.window.sections;

import com.energyxxer.guardian.global.TabManager;
import com.energyxxer.guardian.global.temp.projects.Project;
import com.energyxxer.guardian.global.temp.projects.ProjectManager;
import com.energyxxer.guardian.langinterface.ProjectType;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.main.window.actions.ActionManager;
import com.energyxxer.guardian.main.window.actions.ProgramAction;
import com.energyxxer.guardian.main.window.sections.toolbar.PathViewToken;
import com.energyxxer.guardian.ui.ToolbarButton;
import com.energyxxer.guardian.ui.ToolbarSeparator;
import com.energyxxer.guardian.ui.tablist.TabListMaster;
import com.energyxxer.guardian.ui.tablist.TabSeparator;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.Padding;
import com.energyxxer.xswing.ScalableDimension;
import com.energyxxer.xswing.hints.TextHint;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;

/**
 * Created by User on 12/15/2016.
 */
public class Toolbar extends JPanel {

    public TextHint hint = GuardianWindow.hintManager.createTextHint("");
    private TabSeparator sharedSeparator = null;

    public ThemeListenerManager tlm;

    public void setActiveFile(File file) {
        pathIndicatorTabManager.closeAllTabs(true);
        pathIndicatorTabManager.getTabList().removeAllTabs();
        if(file != null) {
            Project associatedProject = ProjectManager.getAssociatedProject(file);

            Path root = associatedProject != null ? associatedProject.getRootDirectory().toPath() : null;

            Path shownPath = file.toPath();

            if(root != null) {
                shownPath = root.relativize(shownPath);
            }
            for(int i = 0; i <= shownPath.getNameCount(); i++) {
                File fileToShow;
                if(i == 0) {
                    if(root != null) fileToShow = root.toFile();
                    else fileToShow = shownPath.getRoot().toFile();
                } else {
                    if(root != null) fileToShow = root.resolve(shownPath.subpath(0, i)).toFile();
                    else fileToShow = shownPath.getRoot().resolve(shownPath.subpath(0, i)).toFile();
                }
                pathIndicatorTabManager.openTab(new PathViewToken(fileToShow));
                if(i != shownPath.getNameCount()) {
                    pathIndicatorTabManager.getTabList().addTab(sharedSeparator);
                }
            }
        }
    }

    private final TabManager pathIndicatorTabManager;
    
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
        tlm.addThemeChangeListener(t -> {
            this.setBackground(t.getColor(new Color(235, 235, 235), "Toolbar.background"));
            this.setBorder(BorderFactory.createMatteBorder(0, 0, Math.max(t.getInteger(1,"Toolbar.border.thickness"),0), 0, t.getColor(new Color(200, 200, 200), "Toolbar.border.color")));

            int height = t.getInteger(29, "Toolbar.height");

            this.setPreferredSize(new ScalableDimension(1, height));
        });
        buttonBar.setOpaque(false);
        this.add(buttonBar, BorderLayout.EAST);

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


}
