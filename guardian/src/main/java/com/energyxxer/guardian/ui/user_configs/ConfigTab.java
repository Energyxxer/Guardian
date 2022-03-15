package com.energyxxer.guardian.ui.user_configs;

import com.energyxxer.guardian.global.temp.projects.Project;
import com.energyxxer.guardian.ui.Tab;
import com.energyxxer.guardian.ui.display.DisplayModule;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.ui.styledcomponents.StyledPopupMenu;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class ConfigTab implements ModuleToken {
    private String title;
    private Project project;
    private ArrayList<ConfigTabDisplayModuleEntry> entries = new ArrayList<>();

    public ConfigTab(String title, Project project) {
        this.title = title;
        this.project = project;
    }

    public void addEntry(ConfigTabDisplayModuleEntry entry) {
        entries.add(entry);
    }

    public ArrayList<ConfigTabDisplayModuleEntry> getEntries() {
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
        return new ConfigTabDisplayModule(this, project);
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
