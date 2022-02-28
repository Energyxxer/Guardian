package com.energyxxer.guardian.ui.explorer.base.elements;

import com.energyxxer.guardian.ui.explorer.base.ExplorerMaster;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.xswing.ScalableGraphics2D;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Created by User on 4/8/2017.
 */
public class ExplorerSeparator extends ExplorerElement {

    private String label;

    public ExplorerSeparator(ExplorerMaster master) {
        this(null, master);
    }

    public ExplorerSeparator(String label, ExplorerMaster master) {
        super(master);
        this.label = label;
    }

    @Override
    public void render(Graphics g) {
        master.addToFlatList(this);

        g.setColor((this.rollover || this.selected) ? master.getColors().get("item.rollover.background") : master.getColors().get("item.background"));
        g.fillRect(0, master.getOffsetY(), master.getWidth(), this.getHeight());

        g.setColor(master.getColors().get("item.foreground"));
        int lineStart = (int) (master.getWidth()/ScalableGraphics2D.SCALE_FACTOR) / 10;

        g.setFont(g.getFont().deriveFont(Font.BOLD));
        if(label != null) {
            lineStart = (int) (master.getWidth()/ScalableGraphics2D.SCALE_FACTOR)/20;
            FontMetrics fm = g.getFontMetrics();
            int stringWidth = fm.stringWidth(label);
            g.drawString(label, lineStart, master.getOffsetY() + (this.getHeight()/2) + fm.getAscent()/2 - 1);
            lineStart += stringWidth + lineStart / 2;
        }
        g.fillRect(lineStart, master.getOffsetY() + ((this.getHeight() / 2) - 1), ((int) (master.getWidth()/ScalableGraphics2D.SCALE_FACTOR) * 9 / 10) - lineStart, 2);

        master.renderOffset(this.getHeight());
    }

    @Override
    public int getHeight() {
        return master.getRowHeight();
    }

    @Override
    public ModuleToken getToken() {
        return null;
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
}
