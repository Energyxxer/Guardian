package com.energyxxer.guardian.ui.floatingcanvas;

import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.floatingcanvas.styles.ColorStyleProperty;
import com.energyxxer.guardian.ui.floatingcanvas.styles.FloatStyleProperty;
import com.energyxxer.guardian.ui.floatingcanvas.styles.IntStyleProperty;
import com.energyxxer.guardian.ui.floatingcanvas.styles.StyleProperty;
import com.energyxxer.guardian.ui.theme.Theme;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static com.energyxxer.guardian.ui.floatingcanvas.DynamicVector.Unit.RELATIVE;
import static com.energyxxer.guardian.ui.floatingcanvas.DynamicVector.Unit.RELATIVE_MIN;

public class FloatingPanel extends FloatingComponent {

    private DynamicVector size;
    @NotNull
    public StyleProperty<Integer> borderThickness = new IntStyleProperty(0);
    @NotNull
    public StyleProperty<Color> borderColor = new ColorStyleProperty(null);
    @NotNull
    public StyleProperty<Float> cornerRadius = new FloatStyleProperty(0f);

    public FloatingPanel(DynamicVector size) {
        this.size = size;
    }

    @Override
    public boolean contains(Point p) {
        return getBounds().contains(p);
    }

    @Override
    public void paint(Graphics2D g) {
        Rectangle rect = getBounds();

        int cornerRadius = RELATIVE_MIN.convert(this.cornerRadius.getCurrent(this), rect.width, rect.height);

        g.setColor(borderColor.getCurrent(this));
        int borderThickness = this.borderThickness.getCurrent(this);
        if(borderThickness > 0) {
            g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, cornerRadius, cornerRadius);
        }

        g.setColor(this.background.getCurrent(this));
        g.fillRoundRect(rect.x+borderThickness, rect.y+borderThickness, rect.width-borderThickness*2, rect.height-borderThickness*2, cornerRadius, cornerRadius);

        if(this.children != null) {
            for(FloatingComponent obj : this.children) {
                obj.paint(g);
            }
        }
    }

    @Override
    public Rectangle getBounds() {
        Rectangle parentBounds = getParentBounds();

        int width = size.getAbsoluteX(parentBounds.width, parentBounds.height);
        int height = size.getAbsoluteY(parentBounds.width, parentBounds.height);

        int x = parentBounds.x + getAlignment().getX(parentBounds.width, width, parentBounds.height, height);
        int y = parentBounds.y + getAlignment().getY(parentBounds.height, height, parentBounds.width, width);

        return new Rectangle(x, y, width, height);
    }

    public DynamicVector getSizeVector() {
        return size;
    }

    @Override
    public void themeUpdated(Theme t) {
        super.themeUpdated(t);
        borderColor.themeUpdated(t);
        borderThickness.themeUpdated(t);
        cornerRadius.themeUpdated(t);
    }


    public void batchSetKeys(String... keys) {
        batchSetKeysNoUpdate(keys);
        themeUpdated(GuardianWindow.getTheme());
    }

    private void batchSetKeysNoUpdate(String... keys) {
        String[] backgroundKeys = new String[keys.length];
        String[] foregroundKeys = new String[keys.length];
        String[] borderThicknessKeys = new String[keys.length];
        String[] borderColorKeys = new String[keys.length];
        String[] cornerRadiusKeys = new String[keys.length];

        for(int i = 0; i < keys.length; i++) {
            String prefix = keys[i];
            backgroundKeys[i] = prefix + ".*.background";
            foregroundKeys[i] = prefix + ".*.foreground";
            borderThicknessKeys[i] = prefix + ".*.border.thickness";
            borderColorKeys[i] = prefix + ".*.border.color";
            cornerRadiusKeys[i] = prefix + ".*.cornerRadius";
        }

        background.setKeys(backgroundKeys);
        foreground.setKeys(foregroundKeys);
        borderThickness.setKeys(borderThicknessKeys);
        borderColor.setKeys(borderColorKeys);
        cornerRadius.setKeys(cornerRadiusKeys);
    }

    public static class ContentSized extends FloatingPanel {

        public ContentSized() {
            super(new DynamicVector(1f, RELATIVE, 1f, RELATIVE));
        }

        public ContentSized(DynamicVector size) {
            super(size);
        }

        @Override
        public Rectangle getBounds() {
            Rectangle parentBounds = getParentBounds();

            DynamicVector size = getSizeVector();

            Rectangle childUnion = null;
            if(this.children != null) {
                for(FloatingComponent child : this.children) {
                    if(childUnion == null) childUnion = child.getBounds();
                    else childUnion = childUnion.union(child.getBounds());
                }
            }
            if(childUnion == null) {
                childUnion = new Rectangle(0, 0, 0, 0);
            }

            int width = size.getAbsoluteX(childUnion.width, childUnion.height);
            int height = size.getAbsoluteY(childUnion.width, childUnion.height);

            int x = parentBounds.x + getAlignment().getX(parentBounds.width, width, parentBounds.height, height);
            int y = parentBounds.y + getAlignment().getY(parentBounds.height, height, parentBounds.width, width);

            return new Rectangle(x, y, width, height);
        }
    }
}
