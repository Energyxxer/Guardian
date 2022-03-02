package com.energyxxer.guardian.ui.dialogs.build_configs;

public interface Property<T, S> {
    T get(S subject);
    void set(S subject, T value);
}
