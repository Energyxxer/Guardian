package com.energyxxer.guardian.ui.styledcomponents;

import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.XTextField;

import java.awt.Color;

/**
 * Provides a text field that reacts to theme changes and
 * adjusts their own color. It also provides a namespace for
 * more specific paths on the theme file. If the namespace is
 * not specified, it defaults to the general style.
 */
public class StyledTextField extends XTextField {

    private String namespace = null;

    public StyledTextField(ThemeListenerManager tlm) {
        this(null,null,tlm);
    }

    public StyledTextField(String text, ThemeListenerManager tlm) {
        this(text,null,tlm);
    }

    public StyledTextField(String text, String namespace, ThemeListenerManager tlm) {
        this(text,namespace,-1,tlm);
    }

    public StyledTextField(String text, String namespace, int columns, ThemeListenerManager tlm) {
        if(text != null) this.setText(text);
        if(namespace != null) this.setNamespace(namespace);
        if(columns >= 0) this.setColumns(columns);

        tlm.addThemeChangeListener(t -> {
            if(this.namespace != null) {
                setBackground       (t.getColor(new Color(220, 220, 220), this.namespace + ".textfield.background","General.textfield.background"));
                setForeground       (t.getColor(Color.BLACK, this.namespace + ".textfield.foreground","General.textfield.foreground","General.foreground"));
                setSelectionColor   (t.getColor(new Color(50, 100, 175), this.namespace + ".textfield.selection.background","General.textfield.selection.background"));
                setSelectedTextColor(t.getColor(getForeground(), this.namespace + ".textfield.selection.foreground","General.textfield.selection.foreground"));
                setBorder(t.getColor(new Color(200, 200, 200), this.namespace + ".textfield.border.color","General.textfield.border.color"),Math.max(t.getInteger(1,this.namespace + ".textfield.border.thickness","General.textfield.border.thickness"),0));
                setFont(t.getFont(this.namespace+".textfield","General.textfield","General"));

                setDisabledTextColor(t.getColor(getForeground(), this.namespace + ".textfield.disabled.foreground","General.textfield.disabled.foreground"));
            } else {
                setBackground       (t.getColor(new Color(220, 220, 220), "General.textfield.background"));
                setForeground       (t.getColor(Color.BLACK, "General.textfield.foreground","General.foreground"));
                setSelectionColor   (t.getColor(new Color(50, 100, 175), "General.textfield.selection.background"));
                setSelectedTextColor(t.getColor(getForeground(), "General.textfield.selection.foreground"));
                setBorder(t.getColor(new Color(200, 200, 200), "General.textfield.border.color"),Math.max(t.getInteger(1,"General.textfield.border.thickness"),0));
                setFont(t.getFont("General.textfield","General"));

                setDisabledTextColor(t.getColor(getForeground(), "General.textfield.disabled.foreground"));
            }
        });
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return this.namespace;
    }
}
