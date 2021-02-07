package com.energyxxer.xswing;

import javax.swing.*;
import java.awt.*;

public class XCheckBox extends JCheckBox {

    protected Color borderColor = new Color(150,150,150);
    protected int borderThickness = 1;
    protected Color rolloverColor = new Color(240,240,240);
    protected Color pressedColor = new Color(175,175,175);

    protected int checkBoxSize = 12;

    protected int realIconTextGap = 5;

    protected Image checkMarkIcon = null;

    protected boolean labelOnLeft = false;

    {
        setFocusPainted(false);
        setOpaque(false);
        setContentAreaFilled(false);
        setBackground(new Color(225,225,225));
        this.setBorderPainted(false);
    }

    public XCheckBox() {
    }

    public XCheckBox(Icon icon) {
        super(icon);
    }

    public XCheckBox(Icon icon, boolean selected) {
        super(icon, selected);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Rectangle checkBoxRect = new Rectangle();
        checkBoxRect.x = 0;
        checkBoxRect.y = (this.getHeight() - checkBoxSize) / 2;
        checkBoxRect.width = checkBoxSize;
        checkBoxRect.height = checkBoxSize;
        int stringX = checkBoxRect.x + checkBoxRect.width + realIconTextGap;
        FontMetrics metrics = g.getFontMetrics(this.getFont());

        if(labelOnLeft) {
            stringX = 0;
            checkBoxRect.x = metrics.stringWidth(this.getText()) + realIconTextGap;
        }

        g.setColor(this.getBorderColor());
        g.fillRect(checkBoxRect.x, checkBoxRect.y, checkBoxRect.width, checkBoxRect.height);
        if(this.getModel().isPressed()) {
            g.setColor(this.getPressedColor());
        } else if(this.getModel().isRollover()) {
            g.setColor(this.getRolloverColor());
        } else {
            g.setColor(this.getBackground());
        }
        g.fillRect(checkBoxRect.x+borderThickness, checkBoxRect.y+borderThickness, checkBoxRect.width-2*borderThickness, checkBoxRect.height-2*borderThickness);
        g.setColor(this.getForeground());
        g.setFont(this.getFont());
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if(this.isSelected() && this.getCheckMarkIcon() != null) {
            g.drawImage(this.getCheckMarkIcon(), checkBoxRect.x, checkBoxRect.y, checkBoxRect.width, checkBoxRect.height, null);
        }


        g.drawString(this.getText(), stringX, this.getHeight()/2 - metrics.getHeight()/2 + metrics.getAscent());

        //super.paintComponent(g);
    }

    public void setBorder(Color c, int thickness) {
        if(c != null) borderColor = c;
        this.borderThickness = thickness;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public int getBorderThickness() {
        return borderThickness;
    }

    public Color getRolloverColor() {
        return rolloverColor;
    }

    public Color getPressedColor() {
        return pressedColor;
    }

    public void setRolloverColor(Color c) {
        this.rolloverColor = c;
    }

    public void setPressedColor(Color c) {
        this.pressedColor = c;
    }

    public Image getCheckMarkIcon() {
        return checkMarkIcon;
    }

    public void setCheckMarkIcon(Image checkMarkIcon) {
        this.checkMarkIcon = checkMarkIcon;
    }

    public int getCheckBoxSize() {
        return checkBoxSize;
    }

    public void setCheckBoxSize(int checkBoxSize) {
        this.checkBoxSize = checkBoxSize;
    }

    @Override
    public void setIconTextGap(int iconTextGap) {
        realIconTextGap = iconTextGap;
        super.setIconTextGap(realIconTextGap + checkBoxSize - 12);
    }

    public boolean isLabelOnLeft() {
        return labelOnLeft;
    }

    public void setLabelOnLeft(boolean labelOnLeft) {
        this.labelOnLeft = labelOnLeft;
    }
}
