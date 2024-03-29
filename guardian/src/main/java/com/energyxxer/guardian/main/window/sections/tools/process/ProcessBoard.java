package com.energyxxer.guardian.main.window.sections.tools.process;

import com.energyxxer.guardian.main.window.sections.quick_find.StyledExplorerMaster;
import com.energyxxer.guardian.ui.explorer.base.StandardExplorerItem;
import com.energyxxer.guardian.ui.scrollbar.OverlayScrollPane;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.guardian.main.window.sections.tools.ToolBoard;
import com.energyxxer.guardian.main.window.sections.tools.ToolBoardMaster;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.util.processes.AbstractProcess;
import com.energyxxer.xswing.ScalableDimension;

import java.awt.*;
import java.util.ConcurrentModificationException;

public class ProcessBoard extends ToolBoard {

    private OverlayScrollPane scrollPane = new OverlayScrollPane(new ThemeListenerManager());
    private StyledExplorerMaster explorer = new StyledExplorerMaster();

    public ProcessBoard(ToolBoardMaster parent) {
        super(parent);
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new ScalableDimension(10, 200));
        scrollPane.setViewportView(explorer);
        this.add(scrollPane);
    }

    public void addProcess(AbstractProcess process) {
        StandardExplorerItem item = new StandardExplorerItem(new ProcessToken(process), explorer, null);
        item.setDetailed(true);
        explorer.addElement(item);
        explorer.repaint();
    }

    public void removeProcess(AbstractProcess process) {
        while(true) {
            try {
                explorer.removeElementIf(e -> e instanceof StandardExplorerItem && e.getToken() instanceof ProcessToken && ((ProcessToken) e.getToken()).getProcess() == process);
                break;
            } catch(ConcurrentModificationException ignore) {
                Debug.log("Concurrent modification exception, try again");
            }
        }
        explorer.repaint();
    }

    @Override
    public String getName() {
        return "Processes";
    }

    @Override
    public String getIconName() {
        return "process";
    }
}
