package com.energyxxer.xswing;

import javax.swing.*;
import java.awt.*;

/**
 * Created by User on 2/11/2017.
 */
public class Padding extends JPanel {

    public Padding() {
        this.setOpaque(false);
    }

    public Padding(int size) {
        this(size, size);
        setPadding(size);
    }

    public Padding(int width, int height) {
        setPadding(width, height);
        this.setOpaque(false);
    }

    public void setPadding(int size) {
        setPadding(size, size);
    }

    public void setPadding(int width, int height) {
        Dimension dim = new ScalableDimension(width, height);
        this.setPreferredSize(dim);
        this.setMaximumSize(dim);
    }
}
