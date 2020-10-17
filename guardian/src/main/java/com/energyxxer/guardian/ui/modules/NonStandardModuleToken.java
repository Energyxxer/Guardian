package com.energyxxer.guardian.ui.modules;

import com.energyxxer.guardian.ui.explorer.base.ExplorerMaster;
import com.energyxxer.guardian.ui.explorer.base.StandardExplorerItem;
import com.energyxxer.guardian.ui.explorer.base.elements.ExplorerElement;

public interface NonStandardModuleToken {
    ExplorerElement createElement(StandardExplorerItem parent);
    ExplorerElement createElement(ExplorerMaster parent);
}
