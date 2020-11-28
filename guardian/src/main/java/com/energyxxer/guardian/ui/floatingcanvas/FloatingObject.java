package com.energyxxer.guardian.ui.floatingcanvas;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public abstract class FloatingObject implements MouseListener, MouseMotionListener, MouseWheelListener {

    FloatingCanvas rootCanvas;
    FloatingObject parent;
    protected List<FloatingObject> children = null;
    private Color background = Color.WHITE;
    private Color rolloverBackground = null;
    private Color pressedBackground = null;

    private Color foreground = Color.BLACK;
    private Color rolloverForeground = null;
    private Color pressedForeground = null;

    public FloatingObject(FloatingCanvas rootCanvas) {
        this.rootCanvas = rootCanvas;
    }

    public FloatingObject() {
    }

    public boolean isRollover() {
        if(rootCanvas == null) throw new IllegalStateException("Orphan FloatingObject");
        return rootCanvas.isRollover(this);
    }

    public boolean isPressed() {
        if(rootCanvas == null) throw new IllegalStateException("Orphan FloatingObject");
        return rootCanvas.isPressed(this);
    }

    public FloatingObject getObjectAtMousePos(Point p) {
        if(children != null) {
            for(FloatingObject obj : children) {
                if(obj.contains(p)) {
                    return obj.getObjectAtMousePos(p);
                }
            }
        }
        return this;
    }

    public void add(FloatingObject obj) {
        if(this.children == null) this.children = new ArrayList<>();
        obj.parent = this;
        obj.rootCanvas = this.rootCanvas;
        this.children.add(obj);
    }

    public FloatingCanvas getRootCanvas() {
        return rootCanvas;
    }

    public FloatingObject getParent() {
        return parent;
    }

    public Rectangle getParentBounds() {
        if(parent != null) return parent.getBounds();
        if(rootCanvas != null) return new Rectangle(0, 0, rootCanvas.getWidth(), rootCanvas.getHeight());
        throw new IllegalStateException("Orphan FloatingObject");
    }


    public abstract boolean contains(Point p);
    public abstract void paint(Graphics2D g);
    public abstract Rectangle getBounds();
    public Cursor getCursor() {
        return Cursor.getDefaultCursor();
    }

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    public Color getForeground() {
        return foreground;
    }

    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }

    public Color getRolloverBackground() {
        return rolloverBackground != null ? rolloverBackground : getBackground();
    }

    public void setRolloverBackground(Color rolloverBackground) {
        this.rolloverBackground = rolloverBackground;
    }

    public Color getPressedBackground() {
        return pressedBackground != null ? pressedBackground : getRolloverBackground();
    }

    public void setPressedBackground(Color pressedBackground) {
        this.pressedBackground = pressedBackground;
    }

    public Color getRolloverForeground() {
        return rolloverForeground != null ? rolloverForeground : getForeground();
    }

    public void setRolloverForeground(Color rolloverForeground) {
        this.rolloverForeground = rolloverForeground;
    }

    public Color getPressedForeground() {
        return pressedForeground != null ? pressedForeground : getRolloverForeground();
    }

    public void setPressedForeground(Color pressedForeground) {
        this.pressedForeground = pressedForeground;
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

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

    }
}
