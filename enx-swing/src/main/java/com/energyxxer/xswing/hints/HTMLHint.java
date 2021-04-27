package com.energyxxer.xswing.hints;

import com.energyxxer.xswing.Padding;

import javax.swing.*;
import java.awt.*;

public class HTMLHint extends Hint {
    private final JLabel label;

    private final Padding paddingNorth;
    private final Padding paddingSouth;
    private final Padding paddingEast;
    private final Padding paddingWest;

    public HTMLHint(JFrame owner) {
        super(owner);
        label = new JLabel();
        label.setOpaque(false);
        label.setBackground(new Color(0,0,0,0));
        label.setForeground(new Color(187, 187, 187));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(label, BorderLayout.CENTER);

        panel.add(paddingNorth = new Padding(3), BorderLayout.NORTH);
        panel.add(paddingSouth = new Padding(3), BorderLayout.SOUTH);
        panel.add(paddingEast = new Padding(3), BorderLayout.EAST);
        panel.add(paddingWest = new Padding(3), BorderLayout.WEST);

        this.setContent(panel);
    }

    public HTMLHint(JFrame owner, String text) {
        this(owner);
        setText(text);
    }

    public void setText(String text) {
        label.setText("<html>" + text + "</html>");
        label.invalidate();
        label.validate();
        this.update();
    }

    public void setPadding(int horizontal, int vertical) {
        paddingNorth.setPadding(vertical);
        paddingSouth.setPadding(vertical);
        paddingEast.setPadding(horizontal);
        paddingWest.setPadding(horizontal);
    }

    public void setPadding(int px) {
        paddingNorth.setPadding(px);
        paddingSouth.setPadding(px);
        paddingEast.setPadding(px);
        paddingWest.setPadding(px);
    }

    public String getText() {
        return label.getText();
    }

    public Color getForeground() {
        return label.getForeground();
    }

    public void setForeground(Color fg) {
        label.setForeground(fg);
    }

    public Font getFont() {
        return label.getFont();
    }

    public void setFont(Font font) {
        label.setFont(font);
    }
}
