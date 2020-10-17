package com.energyxxer.guardian.ui.modules;

public interface DropTargetExplorerModuleToken extends ModuleToken {
    boolean canAcceptMove(DraggableExplorerModuleToken[] draggable);

    boolean canAcceptCopy(DraggableExplorerModuleToken[] draggables);
}
