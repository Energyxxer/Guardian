package com.energyxxer.guardian.ui.modules;

public interface ModuleTokenFactory<T extends ModuleToken> {
    T createFromIdentifier(String identifier);
}
