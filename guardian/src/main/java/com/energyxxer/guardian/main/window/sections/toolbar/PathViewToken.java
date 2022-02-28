package com.energyxxer.guardian.main.window.sections.toolbar;

import com.energyxxer.guardian.global.Commons;
import com.energyxxer.guardian.ui.Tab;
import com.energyxxer.guardian.ui.display.DisplayModule;
import com.energyxxer.guardian.ui.modules.FileModuleToken;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.ui.styledcomponents.StyledPopupMenu;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.util.Collection;

public class PathViewToken implements ModuleToken {
    private FileModuleToken fileToken;
    private boolean isActiveProject;

    public PathViewToken(File file, boolean isActiveProject) {
        this.fileToken = new FileModuleToken(file);
        this.isActiveProject = isActiveProject;
        fileToken.setHideFolderIcon(true);
    }

    @Override
    public String getTitle(TokenContext context) {
        return fileToken.getTitle(TokenContext.TAB);
    }

    @Override
    public Image getIcon() {
        return fileToken.getIcon();
    }

    @Override
    public String getHint() {
        return fileToken.getHint();
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
    public void onInteract() {
        Commons.showInProjectExplorer(fileToken.getFile());
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
        return other instanceof PathViewToken && ((PathViewToken) other).fileToken.equals(this.fileToken);
    }

    @Override
    public boolean isTabCloseable() {
        return false;
    }

    @Override
    public float getAlpha() {
        return isActiveProject ? 1.0f : 0.75f;
    }
}
