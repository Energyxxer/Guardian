package com.energyxxer.guardian.ui.audio;

import com.energyxxer.guardian.global.Commons;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.floatingcanvas.DrawMultipliedComposite;
import com.energyxxer.guardian.ui.floatingcanvas.DynamicVector;
import com.energyxxer.guardian.ui.floatingcanvas.FloatingComponent;
import com.energyxxer.guardian.ui.floatingcanvas.FloatingPanel;
import com.energyxxer.guardian.ui.floatingcanvas.styles.ColorStyleProperty;
import com.energyxxer.guardian.ui.floatingcanvas.styles.FloatStyleProperty;
import com.energyxxer.guardian.ui.floatingcanvas.styles.IntStyleProperty;
import com.energyxxer.guardian.ui.theme.Theme;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class Button extends FloatingPanel {

    private FloatStyleProperty size = new FloatStyleProperty(0.2f);
    private IntStyleProperty borderThickness = new IntStyleProperty(2);
    private ColorStyleProperty borderColor = new ColorStyleProperty(Color.BLACK);

    private ArrayList<Runnable> clickEvents = new ArrayList<>();

    private String iconName;

    public Button(ThemeListenerManager tlm, String... keys) {
        super(new DynamicVector(0.2f, DynamicVector.Unit.RELATIVE_MIN, 0.2f, DynamicVector.Unit.RELATIVE_MIN));

        setKeysNoUpdate(keys);
        tlm.addThemeChangeListener(this::themeChanged);
    }

    public void setKeys(String... keys) {
        setKeysNoUpdate(keys);
        themeChanged(GuardianWindow.getTheme());
    }

    private void setKeysNoUpdate(String... keys) {
        String[] backgroundKeys = new String[keys.length];
        String[] foregroundKeys = new String[keys.length];
        String[] sizeKeys = new String[keys.length];
        String[] borderThicknessKeys = new String[keys.length];
        String[] borderColorKeys = new String[keys.length];

        for(int i = 0; i < keys.length; i++) {
            String prefix = keys[i];
            backgroundKeys[i] = prefix + ".*.background";
            foregroundKeys[i] = prefix + ".*.foreground";
            sizeKeys[i] = prefix + ".*.size";
            borderThicknessKeys[i] = prefix + ".*.border.thickness";
            borderColorKeys[i] = prefix + ".*.border.color";
        }

        background.setKeys(backgroundKeys);
        foreground.setKeys(foregroundKeys);
        size.setKeys(sizeKeys);
        borderThickness.setKeys(borderThicknessKeys);
        borderColor.setKeys(borderColorKeys);
    }

    private void themeChanged(Theme t) {
        background.themeUpdated(t);
        foreground.themeUpdated(t);
        size.themeUpdated(t);
        borderThickness.themeUpdated(t);
        borderColor.themeUpdated(t);
        updateSize();
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    @Override
    public void paint(Graphics2D g) {
        Rectangle bounds = getBounds();

        g.setColor(this.borderColor.getCurrent(this));
        g.fillOval(bounds.x, bounds.y, bounds.width, bounds.height);

        int borderThickness = this.borderThickness.getCurrent(this);
        g.setColor(this.background.getCurrent(this));
        g.fillOval(bounds.x + borderThickness, bounds.y + borderThickness, bounds.width - borderThickness*2, bounds.height - borderThickness*2);

        g.setColor(this.foreground.getCurrent(this));
        drawButtonContents(g, bounds);

        if(this.children != null) {
            for(FloatingComponent obj : this.children) {
                obj.paint(g);
            }
        }
    }

    protected void drawButtonContents(Graphics2D g, Rectangle bounds) {
        if(iconName != null) {
            Image icon = Commons.getIcon(iconName);

            Composite oldComposite = g.getComposite();

            g.setComposite(new DrawMultipliedComposite(g.getColor()));
            g.drawImage(icon.getScaledInstance((int) Math.ceil(bounds.width*0.6), (int) Math.ceil(bounds.height*0.6), Image.SCALE_SMOOTH), (int) (bounds.x + bounds.width*0.2), (int) (bounds.y + bounds.width*0.2), null);

            g.setComposite(oldComposite);
        }
    }

    public boolean addClickEvent(Runnable runnable) {
        return clickEvents.add(runnable);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        updateSize();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        updateSize();
        if(isRollover()) {
            clickEvents.forEach(Runnable::run);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        updateSize();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        updateSize();
    }

    private void updateSize() {
        this.getSizeVector().x = size.getCurrent(this);
        this.getSizeVector().y = size.getCurrent(this);
    }
}
