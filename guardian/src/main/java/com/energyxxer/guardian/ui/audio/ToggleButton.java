package com.energyxxer.guardian.ui.audio;

import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;

public class ToggleButton extends Button {

    private String[] disabledKeys;
    private String[] enabledKeys;
    private boolean enabled = false;

    public ToggleButton(ThemeListenerManager tlm, String... keys) {
        super();

        disabledKeys = new String[keys.length*2];
        enabledKeys = new String[keys.length*2];

        for(int i = 0; i < keys.length; i++) {
            String prefix = keys[i];
            disabledKeys[i] = prefix + ".disabled";
            disabledKeys[i+keys.length] = prefix;
            enabledKeys[i] = prefix + ".enabled";
            enabledKeys[i+keys.length] = prefix;
        }

        setEnabled(false);

        addClickEvent(() -> setEnabled(!isEnabled()));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        super.batchSetKeys(enabled ? enabledKeys : disabledKeys);
    }
}
