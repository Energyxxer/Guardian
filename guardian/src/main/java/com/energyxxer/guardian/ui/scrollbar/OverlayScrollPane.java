package com.energyxxer.guardian.ui.scrollbar;

import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;

import javax.swing.*;
import java.awt.*;

public class OverlayScrollPane extends JScrollPane {

    private final ThemeListenerManager tlm;

    public OverlayScrollPane(ThemeListenerManager tlm, Component view) {
        super(view);
        this.tlm = tlm;
        setup();
    }

    public OverlayScrollPane(ThemeListenerManager tlm) {
        this.tlm = tlm;
        setup();
    }

    private void setup() {
        this.setOpaque(false);
        this.getViewport().setOpaque(false);
        this.setLayout(new OverlayScrollPaneLayout(this, tlm));
        this.setBorder(BorderFactory.createEmptyBorder());
    }
}
