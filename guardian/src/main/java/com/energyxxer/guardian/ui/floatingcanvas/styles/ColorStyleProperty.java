package com.energyxxer.guardian.ui.floatingcanvas.styles;

import com.energyxxer.guardian.ui.theme.Theme;

import java.awt.*;

public class ColorStyleProperty extends StyleProperty<Color> {

    public ColorStyleProperty(Color fallback, String... keys) {
        super(fallback, keys);
    }

    @Override
    public void themeUpdated(Theme t) {
        normal = t.getColor(fallback, normalKeys);
        rollover = t.getColor(normal, rolloverKeys);
        pressed = t.getColor(rollover, pressedKeys);
    }
}
