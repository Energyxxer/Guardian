package com.energyxxer.guardian.global;

import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.util.processes.AbstractProcess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class ProcessManager {
    private static final List<AbstractProcess> activeProcesses = Collections.synchronizedList(new ArrayList<>());

    public static void queueProcess(AbstractProcess process) {
        if(activeProcesses.contains(process)) return;
        process.addProgressListener((p) -> {
            updateStatusBar();
        });
        process.addCompletionListener((p, s) -> {
            activeProcesses.remove(process);
            GuardianWindow.processBoard.removeProcess(process);
            updateStatusBar();
        });
        activeProcesses.add(process);
        GuardianWindow.processBoard.addProcess(process);

        process.start();
    }

    public static boolean any(Predicate<AbstractProcess> p) {
        for(AbstractProcess process : activeProcesses) {
            if(p.test(process)) return true;
        }
        return false;
    }

    private static void updateStatusBar() {
        if(!activeProcesses.isEmpty()) {
            if(activeProcesses.size() == 1) {
                AbstractProcess process = activeProcesses.get(0);
                GuardianWindow.statusBar.setStatus(new Status(Status.INFO, process.getStatus(), process.getProgress()));
            } else {
                GuardianWindow.statusBar.setStatus(new Status(Status.INFO, activeProcesses.size() + " processes running...", null));
            }
        } else {
            GuardianWindow.statusBar.setProgress(null);
        }
        GuardianWindow.processBoard.repaint();
    }

    public static int getCount() {
        return activeProcesses.size();
    }
}
