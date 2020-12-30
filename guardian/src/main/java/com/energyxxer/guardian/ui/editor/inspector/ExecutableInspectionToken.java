package com.energyxxer.guardian.ui.editor.inspector;

import com.energyxxer.enxlex.lexical_analysis.inspections.CodeAction;
import com.energyxxer.guardian.global.Commons;
import com.energyxxer.guardian.ui.Tab;
import com.energyxxer.guardian.ui.display.DisplayModule;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.ui.styledcomponents.StyledPopupMenu;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Collection;

public class ExecutableInspectionToken implements ModuleToken {
    private InspectorDialog dialog;
    private CodeAction action;

    public ExecutableInspectionToken(InspectorDialog dialog, CodeAction action) {
        this.dialog = dialog;
        this.action = action;
    }

    @Override
    public String getTitle(TokenContext context) {
        return action.getDescription();
    }

    @Override
    public Image getIcon() {
        return Commons.getIcon("rename"); //TODO
    }

    @Override
    public String getHint() {
        return "";
    }

    @Override
    public Collection<? extends ModuleToken> getSubTokens() {
        return null;
    }

    @Override
    public boolean isExpandable() {
        return false;
    }

    @Override
    public boolean isModuleSource() {
        return false;
    }

    @Override
    public DisplayModule createModule(Tab tab) {
        return null;
    }

    @Override
    public StyledPopupMenu generateMenu(@NotNull TokenContext context) {
        return null;
    }

    @Override
    public String getIdentifier() {
        return null;
    }

    @Override
    public boolean equals(ModuleToken other) {
        return other == this;
    }

    public int getDefaultXOffset() {
        return -25;
    }

    @Override
    public void onInteract() {
        dialog.submit(action);
    }
}
