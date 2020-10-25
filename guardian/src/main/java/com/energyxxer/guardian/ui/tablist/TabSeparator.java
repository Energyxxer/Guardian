package com.energyxxer.guardian.ui.tablist;

import com.energyxxer.guardian.ui.theme.Theme;
import com.energyxxer.xswing.ScalableGraphics2D;

import java.awt.*;
import java.awt.event.MouseEvent;

public class TabSeparator extends TabListElement {

    public TabSeparator(TabListMaster master) {
        super(master);
    }

    @Override
    public void render(Graphics g) {
        this.lastRecordedOffset = master.getOffsetX();
        int h = (int)Math.floor(master.getHeight() / ScalableGraphics2D.SCALE_FACTOR);
        g.setColor(master.getColors().get("tab.foreground"));

        int x = master.getOffsetX() - 6;
        int w = 5;

        g.drawLine(x, h / 4, x+w, h/2);
        g.drawLine(x, 3*h / 4, x+w, h/2);
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public boolean select(MouseEvent e) {
        return false;
    }

    @Override
    public String getToolTipText() {
        return null;
    }

    @Override
    public void themeChanged(Theme t) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
