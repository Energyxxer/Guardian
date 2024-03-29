package com.energyxxer.guardian.ui.scrollbar;

import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;

import javax.swing.*;
import java.awt.*;

/**
 * Created by User on 12/13/2016.
 */
public class OverlayScrollPaneLayout extends ScrollPaneLayout {

    private final JScrollPane sp;
    private final OverlayScrollBarUI verticalUI;
    private final OverlayScrollBarUI horizontalUI;

    public OverlayScrollPaneLayout(JScrollPane sp, ThemeListenerManager tlm) {
        this.sp = sp;

        sp.getVerticalScrollBar().setUI(verticalUI = new OverlayScrollBarUI(sp, tlm));
        sp.getHorizontalScrollBar().setUI(horizontalUI = new OverlayScrollBarUI(sp, tlm));
        sp.getVerticalScrollBar().setUnitIncrement(20);
        sp.getHorizontalScrollBar().setUnitIncrement(20);
        sp.getVerticalScrollBar().setOpaque(false);
        sp.getHorizontalScrollBar().setOpaque(false);

        sp.setComponentZOrder(sp.getVerticalScrollBar(), 0);
        sp.setComponentZOrder(sp.getHorizontalScrollBar(), 1);
        sp.setComponentZOrder(sp.getViewport(), 2);
    }

    @Override
    public void layoutContainer(Container parent) {

//        try {
            super.layoutContainer(parent);
//        } catch (NullPointerException x) {
//            if(new ConfirmDialog("Crash", "An unexpected error has occurred. Save all open tabs?").result) {
//                ActionManager.getAction("SAVE_ALL").perform();
//            }
//        }

        Rectangle availR = parent.getBounds();
        if(this.rowHead != null) this.rowHead.setSize(this.rowHead.getWidth(),availR.height);
        availR.x = availR.y = 0;

        // viewport
        Insets insets = parent.getInsets();
        availR.x = insets.left + ((rowHead != null) ? rowHead.getWidth() : 0);
        availR.y = insets.top;
        availR.width -= insets.left + insets.right + ((rowHead != null) ? rowHead.getWidth() : 0);
        availR.height -= insets.top + insets.bottom;
        if (viewport != null) {
            viewport.setBounds(availR);
        }

        boolean vsbNeeded = isVerticalScrollBarNecessary();
        boolean hsbNeeded = isHorizontalScrollBarNecessary();

        if (vsb != null) {
            // vertical scroll bar
            Rectangle vsbR = new Rectangle();
            vsbR.width = verticalUI.getThumbSize();
            vsbR.height = availR.height - (hsbNeeded ? vsbR.width : 0);
            vsbR.x = availR.x + availR.width - vsbR.width;
            vsbR.y = availR.y;
            vsb.setBounds(vsbR);
        }

        if (hsb != null) {
            // horizontal scroll bar
            Rectangle hsbR = new Rectangle();
            hsbR.height = horizontalUI.getThumbSize();
            hsbR.width = availR.width - (vsbNeeded ? hsbR.height : 0);
            hsbR.x = availR.x;
            hsbR.y = availR.y + availR.height - hsbR.height;
            hsb.setBounds(hsbR);
        }
    }

    private boolean isVerticalScrollBarNecessary() {
        Rectangle viewRect = viewport.getViewRect();
        Dimension viewSize = viewport.getViewSize();
        return viewSize.getHeight() > viewRect.getHeight();
    }

    private boolean isHorizontalScrollBarNecessary() {
        Rectangle viewRect = viewport.getViewRect();
        Dimension viewSize = viewport.getViewSize();
        return viewSize.getWidth() > viewRect.getWidth();
    }
}
