package com.energyxxer.guardian.ui.orderlist;

import com.energyxxer.guardian.ui.Tab;
import com.energyxxer.guardian.ui.display.DisplayModule;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface CompoundActionModuleToken extends ModuleToken {
    @Override
    default String getHint() {
        return null;
    }

    @Override
    default Collection<? extends ModuleToken> getSubTokens() {
        return null;
    }

    @Override
    default boolean isExpandable() {
        return false;
    }

    @Override
    default boolean isModuleSource() {
        return false;
    }

    @Override
    default DisplayModule createModule(Tab tab) {
        return null;
    }

    @Override
    default String getIdentifier() {
        return null;
    }

    @NotNull
    List<ItemAction> getActions();

    default boolean canRemove() {return true;}

    default void onReorder() {}
}
