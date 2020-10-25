package com.energyxxer.guardian.ui.modules;

import com.energyxxer.guardian.global.temp.projects.Project;
import com.energyxxer.guardian.ui.Tab;
import com.energyxxer.guardian.ui.display.DisplayModule;
import com.energyxxer.guardian.ui.styledcomponents.StyledPopupMenu;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface ModuleToken {
    enum TokenContext {
        EXPLORER, TAB
    }

    default String getTitle() {
        return getTitle(TokenContext.EXPLORER);
    }
    String getTitle(TokenContext context);
    default boolean ellipsisFromLeft() {return false;}
    java.awt.Image getIcon();
    String getHint();
    Collection<? extends ModuleToken> getSubTokens();
    boolean isExpandable();
    boolean isModuleSource();
    DisplayModule createModule(Tab tab);
    default void onInteract() {}
    StyledPopupMenu generateMenu(@NotNull ModuleToken.TokenContext context);

    default boolean isTabCloseable() {
        return true;
    }

    default String getSearchTerms() { return null; }

    String getIdentifier();

    boolean equals(ModuleToken other);
    default String getSubTitle() {
        return null;
    }

    default File getAssociatedProjectRoot() {
        return null;
    }

    default Project getAssociatedProject() {
        return null;
    }

    default int getDefaultXOffset() {
        return 0;
    }

    default float getAlpha() {
        return 1;
    }

    class Static {
        public static List<ModuleTokenFactory> tokenFactories = new ArrayList<>();

        static {
            tokenFactories.add(FileModuleToken.factory);
        }

        public static ModuleToken createFromIdentifier(String identifier) {
            for(ModuleTokenFactory factory : tokenFactories) {
                ModuleToken created = factory.createFromIdentifier(identifier);
                if(created != null) return created;
            }
            return null;
        }
    }
}
