package com.energyxxer.guardian.ui.floatingcanvas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

public class FloatingCanvas extends JPanel implements MouseListener, MouseMotionListener {
    private Dimension size = new Dimension(0,0);
    public List<FloatingObject> objects = new ArrayList<>();

    private FloatingObject rollover = null;
    private FloatingObject pressed = null;

    public FloatingCanvas() {
        setup();
    }

    private void setup() {
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    public void add(FloatingObject obj) {
        objects.add(obj);
        obj.parent = null;
        obj.rootCanvas = this;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(this.getBackground());
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        Rectangle union = new Rectangle(0,0,0,0);
        for(FloatingObject o : objects) {
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

    private FloatingObject getObjectAtMousePos(Point p) {
        for(FloatingObject o : objects) {
            if(o.contains(p)) {
                return o.getObjectAtMousePos(p);
            }
        }
        return null;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        FloatingObject obj = getObjectAtMousePos(e.getPoint());
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
        if(pressed != null) pressed.mouseReleased(e);
        pressed = null;
        mouseMoved(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if(rollover != null) rollover.mouseExited(e);
        rollover = null;
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(pressed != null) pressed.mouseDragged(e);
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        FloatingObject previousRollover = rollover;
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

    public boolean isRollover(FloatingObject obj) {
        return rollover == obj;
    }

    public boolean isPressed(FloatingObject obj) {
        return pressed == obj;
    }
}
