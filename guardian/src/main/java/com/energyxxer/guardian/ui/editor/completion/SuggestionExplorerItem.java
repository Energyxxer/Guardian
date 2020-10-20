package com.energyxxer.guardian.ui.editor.completion;

import com.energyxxer.guardian.ui.explorer.base.ExplorerMaster;
import com.energyxxer.guardian.ui.explorer.base.StandardExplorerItem;

import java.awt.*;

public class SuggestionExplorerItem extends StandardExplorerItem {

    ExpandableSuggestionToken token;

    public SuggestionExplorerItem(ExpandableSuggestionToken token, ExplorerMaster master) {
        super(token, master, null);
        this.token = token;
    }

    @Override
    public void render(Graphics g) {
        if(token.isEnabled()) {
            super.render(g);
        }
    }
}
