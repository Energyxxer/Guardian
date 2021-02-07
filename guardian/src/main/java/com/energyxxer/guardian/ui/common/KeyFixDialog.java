package com.energyxxer.guardian.ui.common;

import com.energyxxer.guardian.main.window.GuardianWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;

public abstract class KeyFixDialog extends JDialog implements KeyListener {

    public KeyFixDialog() {
    }

    public KeyFixDialog(Frame owner) {
        super(owner);
    }

    public KeyFixDialog(Frame owner, boolean modal) {
        super(owner, modal);
    }

    public KeyFixDialog(Frame owner, String title) {
        super(owner, title);
    }

    public KeyFixDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    public KeyFixDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
    }

    public KeyFixDialog(Dialog owner) {
        super(owner);
    }

    public KeyFixDialog(Dialog owner, boolean modal) {
        super(owner, modal);
    }

    public KeyFixDialog(Dialog owner, String title) {
        super(owner, title);
    }

    public KeyFixDialog(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    public KeyFixDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
    }

    public KeyFixDialog(Window owner) {
        super(owner);
    }

    public KeyFixDialog(Window owner, ModalityType modalityType) {
        super(owner, modalityType);
    }

    public KeyFixDialog(Window owner, String title) {
        super(owner, title);
    }

    public KeyFixDialog(Window owner, String title, ModalityType modalityType) {
        super(owner, title, modalityType);
    }

    public KeyFixDialog(Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc) {
        super(owner, title, modalityType, gc);
    }

    @Override
    public void setVisible(boolean b) {
        if(b) {
            GuardianWindow.dialogShown(this);
        }
        super.setVisible(b);
    }
}
