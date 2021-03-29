package com.energyxxer.guardian.ui.floatingcanvas;

import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.floatingcanvas.styles.StyleProperty;
import com.energyxxer.guardian.ui.theme.Theme;
import com.energyxxer.xswing.SystemDefaults;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.function.Supplier;

public abstract class FloatingLabel extends FloatingComponent {
    public StyleProperty<Font> font = new StyleProperty<Font>(SystemDefaults.FONT) {public void themeUpdated(Theme t) {}};

    private Rectangle prevBounds = new Rectangle();

    private boolean interactive = false;
    private boolean copyOnDoubleClick = false;

    public FloatingLabel() {
        this(false);
    }

    public FloatingLabel(boolean copyOnDoubleClick) {
        this.copyOnDoubleClick = copyOnDoubleClick;
        this.interactive = copyOnDoubleClick;
    }

    @Override
    public boolean contains(Point p) {
        return interactive && prevBounds.contains(p);
    }

    @Override
    public void paint(Graphics2D g) {
        g.setFont(font.getCurrent(this));

        String text = getText();
        Image icon = getIcon();

        FontMetrics fm = g.getFontMetrics(font.getCurrent(this));
        int width = fm.stringWidth(text);
        int height = fm.getAscent() + fm.getDescent();

        int iconPadding = fm.getDescent();
        int textXOffset = 0;

        Rectangle parentBounds = getParentBounds();

        if(icon != null) {
            height = Math.max(height, icon.getHeight(null));
            textXOffset = icon.getWidth(null) + iconPadding;
            width += textXOffset;
        }

        int x = parentBounds.x + getAlignment().getX(parentBounds.width, width, parentBounds.height, height);
        int y = parentBounds.y + getAlignment().getY(parentBounds.height, height, parentBounds.width, width);

        if(icon != null) {
            g.drawImage(icon, x, y + height/2 - icon.getHeight(null)/2, null);
        }

        g.setColor(this.foreground.getCurrent(this));
        g.drawString(text, x + textXOffset, y + fm.getAscent());

        prevBounds.x = x;
        prevBounds.y = y;
        prevBounds.width = width;
        prevBounds.height = height;
    }

    @Override
    public void themeUpdated(Theme t) {
        super.themeUpdated(t);
        font.themeUpdated(t);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(prevBounds);
    }

    public void setFont(Font font) {
        this.font = new StyleProperty<Font>(font) {public void themeUpdated(Theme t) {}};
    }

    public abstract String getText();

    public Image getIcon() {
        return null;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(copyOnDoubleClick && e.getClickCount() % 2 == 0) {
            String text = getText();
            AdvancedEditor.copyToClipboard(text);
            GuardianWindow.setStatus("Copied to clipboard: " + text);
        }
    }

    public boolean isInteractive() {
        return interactive;
    }

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
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
