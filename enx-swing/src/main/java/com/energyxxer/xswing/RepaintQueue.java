package com.energyxxer.xswing;

import com.energyxxer.util.Disposable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class RepaintQueue implements Disposable, ActionListener {

    private ArrayList<JComponent> queuedComponents = new ArrayList<>();
    private Timer timer;

    public RepaintQueue() {
        timer = new Timer(1000/60, this);
        timer.setRepeats(false);
    }
    @Override
    public void dispose() {
        timer.stop();
        queuedComponents.clear();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        timer.stop();
        try {
            for (JComponent component : queuedComponents) {
                if (component.isDisplayable()) {
                    component.repaint();
                }
            }
        } finally {
            queuedComponents.clear();
        }
    }

    public void queueRepaint(JComponent component) {
        if(queuedComponents.contains(component)) return;
        queuedComponents.add(component);
        if(!timer.isRunning()) {
            timer.start();
        }
    }
}
