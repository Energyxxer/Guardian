package com.energyxxer.guardian.main.window.sections.tools.find;

import com.energyxxer.guardian.ui.Tab;
import com.energyxxer.guardian.ui.display.DisplayModule;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.ui.styledcomponents.StyledPopupMenu;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public abstract class QueryResult implements ModuleToken {

    protected int count = 0;

    public abstract Collection<? extends QueryResult> getInnerOccurrences();

    public void insertResult(FileOccurrence occurrence) {
        count++;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String getSubTitle() {
        return "" + count + " occurrences";
    }

    @Override
    public String getIdentifier() {
        return null;
    }

    @Override
    public boolean equals(ModuleToken other) {
        return other.equals((Object)this);
    }

    @Override
    public String getHint() {
        return null;
    }

    @Override
    public Collection<? extends ModuleToken> getSubTokens() {
        return getInnerOccurrences();
    }

    @Override
    public DisplayModule createModule(Tab tab) {
        return null;
    }

    @Override
    public boolean isModuleSource() {
        return false;
    }

    @Override
    public StyledPopupMenu generateMenu(@NotNull ModuleToken.TokenContext context) {
        return null;
    }

    public abstract void collectFileOccurrences(List<FileOccurrence> target);
}
