package com.energyxxer.guardian.ui.editor.inspector;

import com.energyxxer.enxlex.lexical_analysis.inspections.InspectionAction;
import com.energyxxer.enxlex.lexical_analysis.inspections.ReplacementInspectionAction;
import com.energyxxer.enxlex.lexical_analysis.inspections.SuggestionInspection;
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
    private SuggestionInspection inspection;

    public ExecutableInspectionToken(InspectorDialog dialog, SuggestionInspection inspection) {
        this.dialog = dialog;
        this.inspection = inspection;
    }

    @Override
    public String getTitle(TokenContext context) {
        return inspection.getDescription();
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
        for(InspectionAction action : inspection.getActions()) {
            if(action instanceof ReplacementInspectionAction) {
                dialog.submit((ReplacementInspectionAction) action);
            }
        }
    }
}
