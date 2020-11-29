package com.energyxxer.guardian.ui.floatingcanvas.styles;

import com.energyxxer.guardian.ui.theme.Theme;

public class FloatStyleProperty extends StyleProperty<Float> {

    public FloatStyleProperty(Float fallback, String... keys) {
        super(fallback, keys);
    }

    @Override
    public void themeUpdated(Theme t) {
        normal = t.getFloat(fallback, normalKeys);
        rollover = t.getFloat(normal, rolloverKeys);
        pressed = t.getFloat(rollover, pressedKeys);
    }
}
