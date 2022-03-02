package com.energyxxer.guardian.ui.dialogs.build_configs;

import com.energyxxer.guardian.ui.Tab;
import com.energyxxer.guardian.ui.display.DisplayModule;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.ui.styledcomponents.StyledPopupMenu;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class BuildConfigTab implements ModuleToken {
    private String title;
    private ArrayList<BuildConfigTabDisplayModuleEntry> entries = new ArrayList<>();

    public BuildConfigTab(String title) {
        this.title = title;
    }

    public void addEntry(BuildConfigTabDisplayModuleEntry entry) {
        entries.add(entry);
    }

    public ArrayList<BuildConfigTabDisplayModuleEntry> getEntries() {
        return entries;
    }

    @Override
    public String getTitle(TokenContext context) {
        return title;
    }

    @Override
    public Image getIcon() {
        return null;
    }

    @Override
    public String getHint() {
        return null;
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
        return true;
    }

    @Override
    public DisplayModule createModule(Tab tab) {
        return new BuildConfigTabDisplayModule(this);
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
        return this == other;
    }

    @Override
    public boolean isTabCloseable() {
        return false;
    }


}
