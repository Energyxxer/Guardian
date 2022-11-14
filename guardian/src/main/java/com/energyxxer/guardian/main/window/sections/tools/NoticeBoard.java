package com.energyxxer.guardian.main.window.sections.tools;

import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.explorer.NoticeExplorerMaster;
import com.energyxxer.guardian.ui.scrollbar.OverlayScrollPaneLayout;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.ScalableDimension;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created by User on 5/16/2017.
 */
public class NoticeBoard extends ToolBoard {

    private static final int BOARD_HEIGHT = 250;

    private ThemeListenerManager tlm = new ThemeListenerManager();

    public NoticeBoard(ToolBoardMaster parent) {
        super(parent);
        this.setLayout(new BorderLayout());

        JScrollPane sp = new JScrollPane(GuardianWindow.noticeExplorer = new NoticeExplorerMaster());
        sp.setBorder(new EmptyBorder(0,0,0,0));
        sp.setLayout(new OverlayScrollPaneLayout(sp, tlm));

        this.add(sp, BorderLayout.CENTER);

        //clear.addActionListener(e -> GuardianWindow.noticeExplorer.clear());
        this.setPreferredSize(new ScalableDimension(0, BOARD_HEIGHT));
    }

    public void expand() {
        this.setPreferredSize(new ScalableDimension(0, BOARD_HEIGHT));
        this.revalidate();
        this.repaint();
    }

    public void collapse() {
        this.setPreferredSize(new ScalableDimension(0, 25));
        this.revalidate();
        this.repaint();
    }

    @Override
    public String getName() {
        return "Notice Board";
    }

    @Override
    public String getIconName() {
        return "notices";
    }
}
