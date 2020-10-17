package com.energyxxer.guardian.main.window.sections;

import com.energyxxer.guardian.global.keystrokes.SimpleMapping;
import com.energyxxer.guardian.ui.common.MenuItems;
import com.energyxxer.guardian.ui.styledcomponents.StyledMenuItem;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.guardian.main.window.actions.ActionManager;
import com.energyxxer.guardian.main.window.actions.ProgramAction;
import com.energyxxer.guardian.ui.styledcomponents.StyledMenu;
import com.energyxxer.xswing.ScalableDimension;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Created by User on 12/15/2016.
 */
public class MenuBar extends JMenuBar {

    private ThemeListenerManager tlm = new ThemeListenerManager();

    {
        tlm.addThemeChangeListener(t -> {
            this.setBackground(t.getColor(new Color(215, 215, 215), "MenuBar.background"));
            this.setBorder(BorderFactory.createMatteBorder(0, 0, Math.max(t.getInteger(1,"MenuBar.border.thickness"),0), 0, t.getColor(new Color(150, 150, 150), "MenuBar.border.color")));
            this.setPreferredSize(new ScalableDimension(0, 20));
        });


        {
            StyledMenu menu = new StyledMenu(" File ");

            menu.setMnemonic(KeyEvent.VK_F);

            // --------------------------------------------------

            menu.add(MenuItems.newMenu("New"));
            menu.add(createItemForAction("CHANGE_WORKSPACE"));
            menu.add(createItemForAction("RELOAD_WORKSPACE"));

            menu.addSeparator();

            menu.add(createItemForAction("SAVE"));
            menu.add(createItemForAction("SAVE_ALL"));

            menu.addSeparator();

            menu.add(createItemForAction("SETTINGS"));
            menu.add(createItemForAction("PROJECT_PROPERTIES"));

            menu.addSeparator();

            menu.add(createItemForAction("COMPILE"));

            menu.addSeparator();

            menu.add(createItemForAction("EXIT"));

            this.add(menu);
        }

        {
            StyledMenu menu = new StyledMenu(" Edit ");
            menu.setMnemonic(KeyEvent.VK_E);

            // --------------------------------------------------

            {
                menu.add(createItemForAction("UNDO"));
            }

            // --------------------------------------------------

            {
                menu.add(createItemForAction("REDO"));
            }

            // --------------------------------------------------

            menu.addSeparator();

            // --------------------------------------------------

            {
                menu.add(createItemForAction("COPY"));
            }

            // --------------------------------------------------

            {
                menu.add(createItemForAction("CUT"));
            }

            // --------------------------------------------------

            {
                menu.add(createItemForAction("PASTE"));
            }

            // --------------------------------------------------

            menu.addSeparator();

            // --------------------------------------------------

            {
                menu.add(createItemForAction("DELETE"));
            }

            // --------------------------------------------------

            this.add(menu);
        }

        {
            StyledMenu menu = new StyledMenu(" Navigate ");
            menu.setMnemonic(KeyEvent.VK_W);

            menu.add(createItemForAction("CLOSE_TAB"));
            menu.add(createItemForAction("CLOSE_ALL_TABS"));
            menu.add(createItemForAction("CLOSE_ALL_TABS_FOR_PROJECT"));
            menu.addSeparator();
            menu.add(createItemForAction("EDITOR_FIND"));
            menu.add(createItemForAction("FIND_IN_PATH"));
            menu.add(createItemForAction("SEARCH_EVERYWHERE"));
            menu.addSeparator();
            menu.add(createItemForAction("TOGGLE_TOOL_BOARD"));
            menu.add(createItemForAction("OPEN_TODO"));
            menu.add(createItemForAction("OPEN_NOTICE_BOARD"));
            menu.add(createItemForAction("OPEN_CONSOLE"));
            menu.add(createItemForAction("OPEN_SEARCH_RESULTS"));
            menu.add(createItemForAction("OPEN_PROCESSES"));

            this.add(menu);
        }

        {
            StyledMenu menu = new StyledMenu("Help");
            menu.setMnemonic(KeyEvent.VK_H);

            menu.add(createItemForAction("DOCUMENTATION"));
            menu.addSeparator();
            menu.add(createItemForAction("CHECK_FOR_UPDATES"));
            menu.add(createItemForAction("ABOUT"));

            this.add(menu);
        }
    }

    private static StyledMenuItem createItemForAction(String actionKey) {
        ProgramAction action = ActionManager.getAction(actionKey);
        StyledMenuItem item = new StyledMenuItem(action.getTitle(), action.getIconKey());
        if(action.getShortcut() != null && action.getShortcut().getFirstMapping() instanceof SimpleMapping) item.setAccelerator(((SimpleMapping) action.getShortcut().getFirstMapping()).getKeyStroke());
        item.addActionListener(e -> action.perform());
        return item;
    }
}
