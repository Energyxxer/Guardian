package com.energyxxer.guardian.ui.floatingcanvas.styles;

import com.energyxxer.guardian.ui.theme.Theme;
import com.energyxxer.xswing.SystemDefaults;

import java.awt.*;

public class FontStyleProperty extends StyleProperty<Font> {

    public FontStyleProperty(String... keys) {
        this(SystemDefaults.FONT, keys);
    }

    public FontStyleProperty(Font fallback, String... keys) {
        super(fallback, keys);
    }

    @Override
    public void themeUpdated(Theme t) {
        normal = t.getFont(fallback, normalKeys);
        rollover = t.getFont(normal, rolloverKeys);
        pressed = t.getFont(rollover, pressedKeys);
    }
}
