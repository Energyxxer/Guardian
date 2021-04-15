package com.energyxxer.guardian.main.window.sections.tools.find;

import com.energyxxer.guardian.ui.explorer.base.ExplorerMaster;
import com.energyxxer.guardian.ui.explorer.base.StandardExplorerItem;
import com.energyxxer.guardian.ui.explorer.base.elements.ExplorerElement;
import com.energyxxer.guardian.ui.modules.ModuleToken;

import java.awt.*;
import java.util.function.Predicate;

public class FindResultExplorerItem extends StandardExplorerItem {

    private Predicate<FindExplorerFilter> filter;

    public FindResultExplorerItem(ModuleToken token, StandardExplorerItem parent, Predicate<FindExplorerFilter> filter) {
        super(token, parent, null);
        this.filter = filter;
    }

    public FindResultExplorerItem(ModuleToken token, ExplorerMaster master, Predicate<FindExplorerFilter> filter) {
        super(token, master, null);
        this.filter = filter;
    }

    @Override
    public void render(Graphics g) {
        if(!(this.master instanceof FindExplorerFilter) || filter.test(((FindExplorerFilter) this.master))) {
            super.render(g);
        } else {
            if(!expanded) {
                this.expand(null);
            }
            for(ExplorerElement element : children) {
                element.render(g);
            }
        }
    }
}
