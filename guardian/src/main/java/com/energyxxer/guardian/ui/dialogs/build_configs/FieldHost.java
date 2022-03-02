package com.energyxxer.guardian.ui.dialogs.build_configs;

import java.util.function.Consumer;

public interface FieldHost<T> {
    void addOpenEvent(Consumer<T> event);
    void addApplyEvent(Consumer<T> event);
}
