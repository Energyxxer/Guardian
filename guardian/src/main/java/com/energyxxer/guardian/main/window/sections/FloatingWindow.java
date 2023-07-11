package com.energyxxer.guardian.main.window.sections;

import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.guardian.ui.ToolbarButton;
import com.energyxxer.guardian.ui.ToolbarSeparator;
import com.energyxxer.guardian.ui.styledcomponents.StyledLabel;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.ComponentResizer;
import com.energyxxer.xswing.DragHandler;
import com.energyxxer.xswing.OverlayBorderPanel;
import com.energyxxer.xswing.ScalableDimension;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class FloatingWindow extends JFrame implements WindowFocusListener, WindowListener, WindowStateListener {

    private ThemeListenerManager tlm = new ThemeListenerManager();

    private OverlayBorderPanel contentPanel = new OverlayBorderPanel(new BorderLayout(), new Insets(8, 8, 8, 8));

    private boolean pinned = false;
    private CompoundBorder border;

    public FloatingWindow(String title, JPanel content, Dimension defaultSize, boolean pinnable) {
        super();
        setup(title, content, defaultSize, pinnable);
    }

    private void setup(String title, JPanel content, Dimension defaultSize, boolean pinnable) {
        this.setUndecorated(true);
        this.setBackground(new Color(0,0,0,1));

        this.setContentPane(contentPanel);
        contentPanel.setOpaque(false);
        JPanel header = new JPanel(new BorderLayout());

        JPanel titleBar = new JPanel(new BorderLayout());
        MouseAdapter dragAdapter = new DragHandler(this);
        titleBar.addMouseListener(dragAdapter);
        titleBar.addMouseMotionListener(dragAdapter);
        titleBar.add(new StyledLabel("    " + title, "FloatingWindow.header", tlm), BorderLayout.WEST);
        JPanel controlsPanel = new JPanel();
        controlsPanel.setOpaque(false);
        titleBar.add(controlsPanel, BorderLayout.EAST);
        header.add(titleBar, BorderLayout.NORTH);
        titleBar.setPreferredSize(new ScalableDimension(1, 35));

        if(pinnable) {
            addPinButton(controlsPanel);
            controlsPanel.add(new ToolbarSeparator(tlm));
        }
        addMinimizeButton(controlsPanel);
        addMaximizeButton(controlsPanel);
        addCloseButton(controlsPanel);

        contentPanel.add(header, BorderLayout.NORTH);
        contentPanel.add(content, BorderLayout.CENTER);

        tlm.addThemeChangeListener(t -> {
            contentPanel.setMinimumSize(new ScalableDimension(400, 150));
            contentPanel.setPreferredSize(defaultSize);
            titleBar.setBackground(t.getColor(new Color(230, 230, 230), "FloatingWindow.header.background"));
            int thickness = Math.max(t.getInteger(1,"FloatingWindow.border.thickness"),0);
            border = new CompoundBorder(new EmptyBorder(ComponentResizer.DIST, ComponentResizer.DIST, ComponentResizer.DIST, ComponentResizer.DIST), BorderFactory.createMatteBorder(thickness, thickness, thickness, thickness, t.getColor(new Color(200, 200, 200), "FloatingWindow.border.color")));
            contentPanel.setBorder(border);
        });

        //this.addMouseListener(this);
        //this.addMouseMotionListener(this);

        this.setSize(defaultSize);

        this.pack();
        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        Dimension size = this.getSize();
        center.x -= size.width/2;
        center.y -= size.height/2;
        this.setLocation(center);

        ComponentResizer resizer = new ComponentResizer(contentPanel, this);
        resizer.setResizable(true, true, true, true);

        this.addWindowListener(this);
        this.addWindowFocusListener(this);
        this.addWindowStateListener(this);

        this.setIconImages(Guardian.window.getWindowIcons());
    }

    private void addCloseButton(JPanel controlsPanel) {
        ToolbarButton button = new ToolbarButton("cross", tlm);
        button.setHintText("Close");
        controlsPanel.add(button);
        button.addActionListener(a -> {
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });
    }

    private void addMaximizeButton(JPanel controlsPanel) {
        ToolbarButton button = new ToolbarButton("maximize", tlm);
        button.setHintText("Maximize");
        controlsPanel.add(button);
        button.addActionListener(a -> {
            if(getExtendedState() == Frame.MAXIMIZED_BOTH) {
                button.setHintText("Maximize");
                this.setExtendedState(Frame.NORMAL);
            } else {
                button.setHintText("Restore");
                this.setExtendedState(Frame.MAXIMIZED_BOTH);
            }
        });
    }

    private void addMinimizeButton(JPanel controlsPanel) {
        ToolbarButton button = new ToolbarButton("toggle", tlm);
        button.setHintText("Minimize");
        controlsPanel.add(button);
        button.addActionListener(a -> {
            this.setExtendedState(Frame.ICONIFIED);
        });
    }

    private void addPinButton(JPanel controlsPanel) {
        ToolbarButton button = new ToolbarButton("pin", tlm);
        button.setHintText("Pin");
        controlsPanel.add(button);
        button.addActionListener(a -> {
            pinned = !pinned;
            this.setAlwaysOnTop(pinned);
            button.setIconName(pinned ? "unpin" : "pin");
            button.setHintText(pinned ? "Unpin" : "Pin");
        });
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
    }

    public void reveal() {
        this.setVisible(true);
        this.requestFocus();
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        dispose();
    }

    @Override
    public void windowClosed(WindowEvent e) {
        dispose();
    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    @Override
    public void dispose() {
        super.dispose();
        if(tlm != null) tlm.dispose();
        tlm = null;
    }

    @Override
    public void windowStateChanged(WindowEvent e) {
        int newState = e.getNewState();
        if(newState == MAXIMIZED_BOTH) {
            contentPanel.setBorder(null);
        } else {
            contentPanel.setBorder(border);
        }
    }
}
