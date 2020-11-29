package com.energyxxer.guardian.ui.floatingcanvas.styles;

import com.energyxxer.guardian.ui.theme.Theme;

public class IntStyleProperty extends StyleProperty<Integer> {

    public IntStyleProperty(Integer fallback, String... keys) {
        super(fallback, keys);
    }

    @Override
    public void themeUpdated(Theme t) {
        normal = t.getInteger(fallback, normalKeys);
        rollover = t.getInteger(normal, rolloverKeys);
        pressed = t.getInteger(rollover, pressedKeys);
    }
}
