package com.energyxxer.guardian.ui.floatingcanvas;

import java.awt.*;

public abstract class FloatingPanel extends FloatingObject {

    @Override
    public boolean contains(Point p) {
        return getBounds().contains(p);
    }

    @Override
    public void paint(Graphics2D g) {
        Rectangle rect = getBounds();

        Color color = isPressed() ? this.getPressedBackground() : isRollover() ? this.getRolloverBackground() : this.getBackground();

        g.setColor(color);
        g.fillRect(rect.x, rect.y, rect.width, rect.height);

        if(this.children != null) {
            for(FloatingObject obj : this.children) {
                obj.paint(g);
            }
        }
    }

    @Override
    public abstract Rectangle getBounds();

    public static class Aligned extends FloatingPanel {

        private DynamicVector size;
        private Alignment alignment;

        public Aligned(DynamicVector size) {
            this.size = size;
            this.alignment = new Alignment();
        }

        public Aligned(DynamicVector size, float alignmentX, float alignmentY) {
            this.size = size;
            this.alignment = new Alignment(alignmentX, alignmentY);
        }

        public Aligned(DynamicVector size, Alignment alignment) {
            this.size = size;
            this.alignment = alignment;
        }

        @Override
        public Rectangle getBounds() {
            Rectangle parentBounds = getParentBounds();

            int width = size.getAbsoluteX(parentBounds.width, parentBounds.height);
            int height = size.getAbsoluteY(parentBounds.width, parentBounds.height);

            int x = parentBounds.x + alignment.getX(parentBounds.width, width);
            int y = parentBounds.y + alignment.getY(parentBounds.height, height);

            return new Rectangle(x, y, width, height);
        }

        public DynamicVector getSizeVector() {
            return size;
        }

        public Alignment getAlignment() {
            return alignment;
        }
    }

}
