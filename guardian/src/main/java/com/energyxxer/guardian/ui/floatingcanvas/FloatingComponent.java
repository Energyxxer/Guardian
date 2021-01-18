package com.energyxxer.guardian.ui.floatingcanvas;

import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.floatingcanvas.styles.ColorStyleProperty;
import com.energyxxer.guardian.ui.floatingcanvas.styles.StyleProperty;
import com.energyxxer.guardian.ui.theme.Theme;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public abstract class FloatingComponent implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

    FloatingCanvas rootCanvas;
    FloatingComponent parent;
    protected List<FloatingComponent> children = null;

    @NotNull
    private Alignment alignment = new Alignment();

    @NotNull
    public StyleProperty<Color> background = new ColorStyleProperty(Color.WHITE);
    @NotNull
    public StyleProperty<Color> foreground = new ColorStyleProperty(Color.BLACK);

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
            for(int i = children.size()-1; i >= 0; i--) {
                FloatingComponent obj = children.get(i);
                if(obj.contains(p)) {
                    return obj.getObjectAtMousePos(p);
                }
            }
        }
        return this;
    }

    public void add(FloatingComponent obj) {
        if(this.children == null) this.children = new ArrayList<>();
        if(obj.parent != null) obj.parent.remove(obj);
        obj.parent = this;
        if(this.rootCanvas != null) obj.setRootCanvas(this.rootCanvas);
        this.children.add(obj);
    }

    public void remove(FloatingComponent obj) {
        if(obj.parent == this) {
            obj.parent = null;
            if(this.children != null) {
                this.children.remove(obj);
            }
        }
    }

    void setRootCanvas(FloatingCanvas rootCanvas) {
        if(this.rootCanvas == rootCanvas) return;
        this.rootCanvas = rootCanvas;
        if(this.children != null) {
            for(FloatingComponent child : children) {
                child.setRootCanvas(rootCanvas);
            }
        }
        themeUpdated(GuardianWindow.getTheme());
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

    @NotNull
    public Alignment getAlignment() {
        return alignment;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(parent != null) parent.mouseClicked(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(parent != null) parent.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(parent != null) parent.mouseReleased(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (this.parent != null) {
            this.parent.mouseDragged(e);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (this.parent != null) {
            this.parent.mouseMoved(e);
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if(parent != null) parent.mouseWheelMoved(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(this.children != null) {
            for(FloatingComponent component : children) {
                component.keyPressed(e);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(this.children != null) {
            for(FloatingComponent component : children) {
                component.keyReleased(e);
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if(this.children != null) {
            for(FloatingComponent component : children) {
                component.keyTyped(e);
            }
        }
    }

    public void canvasResized() {
        if(this.children != null) {
            for(FloatingComponent component : children) {
                component.canvasResized();
            }
        }
    }

    public void themeUpdated(Theme t) {
        background.themeUpdated(t);
        foreground.themeUpdated(t);
        if(children != null) {
            for(FloatingComponent child : children) {
                child.themeUpdated(t);
            }
        }
    }
}
