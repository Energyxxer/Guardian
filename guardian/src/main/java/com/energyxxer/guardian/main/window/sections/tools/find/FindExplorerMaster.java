package com.energyxxer.guardian.main.window.sections.tools.find;

import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.main.window.sections.quick_find.StyledExplorerMaster;

public class FindExplorerMaster extends StyledExplorerMaster implements FindExplorerFilter {
    @Override
    public boolean groupByProject() {
        return GuardianWindow.findBoard.groupByProject();
    }

    @Override
    public boolean groupBySubProject() {
        return GuardianWindow.findBoard.groupBySubProject();
    }

    @Override
    public boolean groupByPath() {
        return GuardianWindow.findBoard.groupByPath();
    }

    @Override
    public boolean groupByFile() {
        return GuardianWindow.findBoard.groupByFile();
    }
}
