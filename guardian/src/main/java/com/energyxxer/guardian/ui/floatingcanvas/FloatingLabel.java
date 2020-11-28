package com.energyxxer.guardian.ui.floatingcanvas;

import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.xswing.SystemDefaults;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.function.Supplier;

public abstract class FloatingLabel extends FloatingObject {
    private Font font = SystemDefaults.FONT;
    private Alignment alignment = new Alignment();

    private Rectangle prevBounds = new Rectangle();

    private boolean copyOnDoubleClick = false;

    public FloatingLabel() {
        this(false);
    }

    public FloatingLabel(boolean copyOnDoubleClick) {
        this.copyOnDoubleClick = copyOnDoubleClick;
    }

    @Override
    public boolean contains(Point p) {
        return prevBounds.contains(p);
    }

    @Override
    public void paint(Graphics2D g) {
        g.setColor(this.getForeground());
        g.setFont(font);

        String text = getText();

        FontMetrics fm = g.getFontMetrics(font);
        int width = fm.stringWidth(text);
        int height = fm.getAscent() + fm.getDescent();

        Rectangle parentBounds = getParentBounds();

        int x = parentBounds.x + alignment.getX(parentBounds.width, width);
        int y = parentBounds.y + alignment.getY(parentBounds.height, height);

        g.drawString(text, x, y + fm.getAscent());

        prevBounds.x = x;
        prevBounds.y = y;
        prevBounds.width = width;
        prevBounds.height = height;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    @Override
    public Rectangle getBounds() {
        return prevBounds;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public abstract String getText();

    @Override
    public void mouseClicked(MouseEvent e) {
        if(copyOnDoubleClick && e.getClickCount() % 2 == 0) {
            String text = getText();
            AdvancedEditor.copyToClipboard(text);
            GuardianWindow.setStatus("Copied to clipboard: " + text);
        }
    }

    public static class Fixed extends FloatingLabel {
        private String text;

        public Fixed(String text) {
            this.text = text;
        }

        public Fixed(String text, boolean copyOnDoubleClick) {
            super(copyOnDoubleClick);
            this.text = text;
        }

        @Override
        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class Dynamic extends FloatingLabel {
        private Supplier<String> supplier;

        public Dynamic(Supplier<String> supplier) {
            this.supplier = supplier;
        }

        public Dynamic(Supplier<String> supplier, boolean copyOnDoubleClick) {
            super(copyOnDoubleClick);
            this.supplier = supplier;
        }

        @Override
        public String getText() {
            return supplier.get();
        }
    }
}
