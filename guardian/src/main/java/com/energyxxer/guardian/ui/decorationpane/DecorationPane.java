package com.energyxxer.guardian.ui.decorationpane;

import com.energyxxer.guardian.global.Status;
import com.energyxxer.guardian.main.window.GuardianWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class DecorationPane extends JDialog implements MouseListener, MouseMotionListener, WindowFocusListener {

    private Image image;
    private Dimension size;

    private Status status = new Status("");

    public final List<DecorationObject> objects = new ArrayList<>();

    public DecorationPane(JFrame parent, Dimension size, Image image) {
        super(parent, false);
        setup(size, image);
    }

    public DecorationPane(Dimension size, Image image) {
        setup(size,image);
    }

    private void setup(Dimension size, Image image) {
        this.image = image;
        this.size = size;
        this.setSize(size);

        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        center.x -= size.width/2;
        center.y -= size.height/2;
        this.setLocation(center);

        this.setUndecorated(true);

        this.setContentPane(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                DecorationPane.this.paintComponent(g);
            }
        });

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addWindowFocusListener(this);
    }

    protected void paintComponent(Graphics g) {
        g.drawImage(image, 0,0,this.getWidth(),this.getHeight(), null);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        objects.forEach(o -> o.paint(g));

        g.dispose();
    }

    private DecorationObject getObjectAtPos(Point p) {
        for (int i = objects.size() - 1; i >= 0; i--) {
            DecorationObject o = objects.get(i);
            if(o.contains(p)) return o;
        }
        return null;
    }

    private DecorationObject pressSource = null;
    private DecorationObject previousRollover = null;

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        DecorationObject obj = getObjectAtPos(e.getPoint());
        if(pressSource == null) pressSource = obj;
        if(pressSource != null) pressSource.mousePressed(e);
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(pressSource != null) {
            pressSource.mouseReleased(e);
            if(pressSource.contains(e.getPoint())) pressSource.mouseClicked(e);
        }
        pressSource = null;
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {
        if(previousRollover != null) previousRollover.mouseExited(e);
        this.setCursor(Cursor.getDefaultCursor());
        GuardianWindow.dismissStatus(status);
        previousRollover = null;
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        DecorationObject obj = getObjectAtPos(e.getPoint());
        if(obj != previousRollover) {
            if(obj != null) {
                this.setCursor(obj.getCursor());
                status.setMessage(obj.getToolTipText());
                if(obj.getToolTipText() != null) GuardianWindow.setStatus(status);
                else GuardianWindow.dismissStatus(status);
                obj.mouseEntered(e);
            }
            if(previousRollover != null) previousRollover.mouseExited(e);
        }
        if(obj == null) {
            this.setCursor(Cursor.getDefaultCursor());
            GuardianWindow.dismissStatus(status);
        }
        previousRollover = obj;
        repaint();
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {

    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        this.setVisible(false);
    }
}
