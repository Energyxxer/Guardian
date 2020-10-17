package com.energyxxer.guardian.ui.modules;

import java.awt.datatransfer.DataFlavor;

public interface DraggableExplorerModuleToken extends ModuleToken {
    DataFlavor getDataFlavor();

    Object getTransferData();
}
