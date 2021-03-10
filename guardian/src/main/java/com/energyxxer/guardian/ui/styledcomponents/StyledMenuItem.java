package com.energyxxer.guardian.ui.styledcomponents;

import com.energyxxer.guardian.global.Commons;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.Disposable;
import com.energyxxer.xswing.menu.XMenuItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Menu item that reacts to window theme changes.
 */
public class StyledMenuItem extends XMenuItem implements Disposable, ActionListener {

    private ThemeListenerManager tlm = new ThemeListenerManager();
    private String icon = null;

    public StyledMenuItem(String text, String icon) {
        if(text != null) setText(text);
        this.icon = icon;
        tlm.addThemeChangeListener(t -> {
            this.setRolloverBackground(t.getColor(new Color(190, 190, 190), "General.menu.selected.background"));
            this.setForeground(t.getColor(Color.BLACK, "General.menu.foreground","General.foreground"));
            this.setFont(t.getFont("General.menu","General"));
            updateIcon();
        });
        this.addActionListener(this);

        this.addMouseWheelListener(StyledMenu.NAVIGATE_WITH_MOUSE_WHEEL);
    }
    public StyledMenuItem(String text) {
        this(text, null);
    }
    public StyledMenuItem() {this(null,null);}

    private void updateIcon() {
        if(this.icon != null) this.setIcon(new ImageIcon(Commons.getIcon(icon).getScaledInstance(16,16, Image.SCALE_SMOOTH)));
    }

    public void setIconName(String icon) {
        this.icon = icon;
        updateIcon();
    }

    public String getIconName() {
        return icon;
    }

    @Override
    public void dispose() {
        tlm.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}