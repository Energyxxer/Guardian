package com.energyxxer.guardian.ui.orderlist;

import com.energyxxer.guardian.ui.explorer.base.StyleProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class ItemLabelAction implements ItemAction {
    protected boolean leftAligned = false;

    private JComponent component;

    protected String label = "";
    protected ImageIcon icon = null;

    public ItemLabelAction(String label) {
        this.label = label;
    }


    public void setLabel(String label) {
        this.label = label;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    public void perform() {}

    @Override
    public void mouseClicked(MouseEvent e, ItemActionHost parent) {
        this.perform();
    }






    private static final int buttonHGap = 6;
    private static final float buttonVSize = 0.8f;

    private int lastRenderedWidth = 0;

    @Override
    public boolean isLeftAligned() {
        return leftAligned;
    }

    public void setLeftAligned(boolean leftAligned) {
        this.leftAligned = leftAligned;
    }

    @Override
    public void render(Graphics g, ItemActionHost host, int x, int y, int w, int h, int mouseState, boolean actionEnabled) {
        this.component = host.getComponent();
        StyleProvider styleProvider = host.getStyleProvider();

        int buttonVGap = (int) ((h * (1-buttonVSize)) / 2);

        FontMetrics fm = g.getFontMetrics(g.getFont());

        int iconMargin = (int) (((h*buttonVSize) - 0.64f*buttonVSize) / 2);

        int buttonWidth = 2 * iconMargin;
        buttonWidth += fm.stringWidth(label);
        if(icon != null) {
            buttonWidth += 16 + iconMargin;
        }

        if(!isLeftAligned()) x -= buttonWidth;

        x += iconMargin;

        if(this.icon != null) {
            g.drawImage(this.icon.getImage(), x + iconMargin, y + buttonVGap + iconMargin, 16, 16, null);
            x += 16;
            x += iconMargin;
        }

        if(mouseState >= 1) {
            g.setColor(styleProvider.getColors().get("item.rollover.foreground"));
        } else {
            g.setColor(styleProvider.getColors().get("item.foreground"));
        }

        g.drawString(label, x, y + fm.getAscent() + ((h - fm.getHeight())/2));

        lastRenderedWidth = buttonWidth;
    }

    @Override
    public int getRenderedWidth() {
        return buttonHGap + lastRenderedWidth;
    }

    @Override
    public String getHint() {
        return null;
    }

    @Override
    public int getHintOffset() {
        return 0;
    }

    @Override
    public boolean intersects(Point p, int w, int h) {
        int buttonVGap = (int) ((h * (1-buttonVSize)) / 2);

        return (p.getY() >= buttonVGap && p.getY() < buttonVGap + (h*buttonVSize)) &&
                (p.getX() >= 0 && p.getX() < lastRenderedWidth);
    }
}
