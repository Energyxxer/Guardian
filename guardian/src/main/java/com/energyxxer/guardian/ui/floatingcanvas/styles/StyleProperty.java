package com.energyxxer.guardian.ui.floatingcanvas.styles;

import com.energyxxer.guardian.ui.floatingcanvas.FloatingComponent;
import com.energyxxer.guardian.ui.theme.Theme;

public abstract class StyleProperty<T> {
    protected T fallback;

    protected String[] normalKeys;
    protected String[] rolloverKeys;
    protected String[] pressedKeys;

    protected T normal;
    protected T rollover;
    protected T pressed;

    public StyleProperty(T fallback, String... keys) {
        this.fallback = fallback;

        setKeys(keys);
    }

    public T getCurrent(FloatingComponent component) {
        return component.isPressed() && pressed != null ? pressed : component.isRollover() && rollover != null ? rollover : normal != null ? normal : fallback;
    }

    public T getNormal() {
        return normal;
    }

    public T getRollover() {
        return rollover;
    }

    public T getPressed() {
        return pressed;
    }

    public void setKeys(String... keys) {
        normalKeys = new String[keys.length];
        rolloverKeys = new String[keys.length];
        pressedKeys = new String[keys.length];

        for(int i = 0; i < keys.length; i++) {
            String template = keys[i];
            normalKeys[i] = template.replace("*.", "");
            rolloverKeys[i] = template.replace("*", "hover");
            pressedKeys[i] = template.replace("*", "pressed");
        }
    }

    public abstract void themeUpdated(Theme t);

    public void setFallback(T fallback) {
        this.fallback = fallback;
    }
}
