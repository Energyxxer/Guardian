package com.energyxxer.guardian.ui.floatingcanvas;

import com.energyxxer.guardian.ui.floatingcanvas.styles.ColorStyleProperty;
import com.energyxxer.guardian.ui.floatingcanvas.styles.StyleProperty;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public abstract class FloatingComponent implements MouseListener, MouseMotionListener, MouseWheelListener {

    FloatingCanvas rootCanvas;
    FloatingComponent parent;
    protected List<FloatingComponent> children = null;

    @NotNull
    private Alignment alignment = new Alignment();

    public final StyleProperty<Color> background = new ColorStyleProperty(Color.WHITE);
    public final StyleProperty<Color> foreground = new ColorStyleProperty(Color.BLACK);

    public FloatingComponent(FloatingCanvas rootCanvas) {
        this.rootCanvas = rootCanvas;
    }

    public FloatingComponent() {
    }

    public boolean isRollover() {
        if(rootCanvas == null) return false;
        return rootCanvas.isRollover(this);
    }

    public boolean isPressed() {
        if(rootCanvas == null) return false;
        return rootCanvas.isPressed(this);
    }

    public FloatingComponent getObjectAtMousePos(Point p) {
        if(children != null) {
            for(FloatingComponent obj : children) {
                if(obj.contains(p)) {
                    return obj.getObjectAtMousePos(p);
                }
            }
        }
        return this;
    }

    public void add(FloatingComponent obj) {
        if(this.children == null) this.children = new ArrayList<>();
        obj.parent = this;
        if(this.rootCanvas != null) obj.setRootCanvas(this.rootCanvas);
        this.children.add(obj);
    }

    private void setRootCanvas(FloatingCanvas rootCanvas) {
        if(this.rootCanvas == rootCanvas) return;
        this.rootCanvas = rootCanvas;
        if(this.children != null) {
            for(FloatingComponent child : children) {
                child.setRootCanvas(rootCanvas);
            }
        }
    }

    public FloatingCanvas getRootCanvas() {
        return rootCanvas;
    }

    public FloatingComponent getParent() {
        return parent;
    }

    public Rectangle getParentBounds() {
        if(parent != null) return parent.getBounds();
        if(rootCanvas != null) return new Rectangle(0, 0, rootCanvas.getWidth(), rootCanvas.getHeight());
        throw new IllegalStateException("Orphan FloatingComponent");
    }


    public abstract boolean contains(Point p);
    public abstract void paint(Graphics2D g);
    public abstract Rectangle getBounds();
    public Cursor getCursor() {
        return Cursor.getDefaultCursor();
    }

    public Alignment getAlignment() {
        return alignment;
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
