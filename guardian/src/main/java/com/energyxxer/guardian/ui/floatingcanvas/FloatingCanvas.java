package com.energyxxer.guardian.ui.floatingcanvas;

import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class FloatingCanvas extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
    private Dimension size = new Dimension(0,0);
    public List<FloatingComponent> objects = new ArrayList<>();

    private FloatingComponent rollover = null;
    private FloatingComponent pressed = null;

    protected final ThemeListenerManager tlm = new ThemeListenerManager();

    public FloatingCanvas() {
        setup();
    }

    private void setup() {
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);

        tlm.addThemeChangeListener(t -> {
            for(FloatingComponent child : objects) {
                child.themeUpdated(t);
            }
        });
    }

    public void add(FloatingComponent obj) {
        objects.add(obj);
        obj.parent = null;
        obj.setRootCanvas(this);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(this.getBackground());
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);


        Rectangle union = new Rectangle(0,0,0,0);
        for(FloatingComponent o : objects) {
            o.paint(g2d);
            union = union.union(o.getBounds());
        }
        union.width -= union.x;
        union.height -= union.y;
        Dimension newSize = union.getSize();
        if(!newSize.equals(size)) {
            size = newSize;
            this.setPreferredSize(newSize);
        }

        g.dispose();
    }

    private FloatingComponent getObjectAtMousePos(Point p) {
        for(int i = objects.size()-1; i >= 0; i--) {
            FloatingComponent o = objects.get(i);
            if(o.contains(p)) {
                return o.getObjectAtMousePos(p);
            }
        }
        return null;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        FloatingComponent obj = getObjectAtMousePos(e.getPoint());
        if(obj != null) obj.mouseClicked(e);
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        pressed = getObjectAtMousePos(e.getPoint());
        if(pressed != null) pressed.mousePressed(e);
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        FloatingComponent oldPressed = pressed;
        pressed = null;
        mouseMoved(e);
        if(oldPressed != null) oldPressed.mouseReleased(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        FloatingComponent oldRollover = rollover;
        rollover = null;
        if(oldRollover != null) oldRollover.mouseExited(e);
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(pressed != null) pressed.mouseDragged(e);
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        FloatingComponent previousRollover = rollover;
        rollover = getObjectAtMousePos(e.getPoint());
        if(previousRollover != rollover) {
            if(previousRollover != null) previousRollover.mouseExited(e);
            if(rollover != null) {
                rollover.mouseEntered(e);
                this.setCursor(rollover.getCursor());
            }
        } else if(rollover != null) {
            rollover.mouseMoved(e);
        }

        if(rollover == null) {
            this.setCursor(Cursor.getDefaultCursor());
        }

        repaint();
    }

    public void updateCursor() {
        if(rollover != null) {
            this.setCursor(rollover.getCursor());
        } else {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if(rollover != null) {
            rollover.mouseWheelMoved(e);
            repaint();
        }
    }

    public boolean isRollover(FloatingComponent obj) {
        return rollover == obj;
    }

    public boolean isPressed(FloatingComponent obj) {
        return pressed == obj;
    }
}
