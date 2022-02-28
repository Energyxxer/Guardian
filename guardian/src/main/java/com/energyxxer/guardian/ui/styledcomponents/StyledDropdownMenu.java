package com.energyxxer.guardian.ui.styledcomponents;

import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.XDropdownMenu;

import java.awt.*;

/**
 * Created by User on 2/11/2017.
 */
public class StyledDropdownMenu<T> extends XDropdownMenu<T> {

    private String namespace = null;

    private ThemeListenerManager tlm = new ThemeListenerManager();

    public StyledDropdownMenu() {
        setup();
    }

    public StyledDropdownMenu(String namespace) {
        this.namespace = namespace;
        setup();
    }

    public StyledDropdownMenu(T[] options) {
        super(options);
        setup();
    }

    public StyledDropdownMenu(T[] options, String namespace) {
        super(options);
        this.namespace = namespace;
        setup();
    }

    private void setup() {
        this.setPopupFactory(StyledPopupMenu::new);
        this.setPopupItemFactory(StyledMenuItem::new);

        tlm.addThemeChangeListener(t -> {
            if(this.namespace != null) {
                setBackground       (t.getColor(new Color(215, 215, 215), this.namespace + ".dropdown.background","General.dropdown.background"));
                setForeground       (t.getColor(Color.BLACK, this.namespace + ".dropdown.foreground","General.dropdown.foreground", "General.foreground"));
                setBorder           (t.getColor(new Color(200, 200, 200), this.namespace + ".dropdown.border.color","General.dropdown.border.color"), Math.max(t.getInteger(1,this.namespace + ".dropdown.border.thickness","General.dropdown.border.thickness"),0));
                setRolloverColor    (t.getColor(new Color(200, 202, 205), this.namespace + ".dropdown.hover.background","General.dropdown.hover.background"));
                setPressedColor     (t.getColor(Color.WHITE, this.namespace + ".dropdown.pressed.background","General.dropdown.pressed.background"));
                setFont(t.getFont(this.namespace+".dropdown","General.dropdown","General"));
            } else {
                setBackground       (t.getColor(new Color(215, 215, 215), "General.dropdown.background"));
                setForeground       (t.getColor(Color.BLACK, "General.dropdown.foreground","General.foreground"));
                setBorder           (t.getColor(new Color(200, 200, 200), "General.dropdown.border.color"), Math.max(t.getInteger(1,"General.dropdown.border.thickness"),0));
                setRolloverColor    (t.getColor(new Color(200, 202, 205), "General.dropdown.hover.background"));
                setPressedColor     (t.getColor(Color.WHITE, "General.dropdown.pressed.background"));
                setFont(t.getFont("General.dropdown","General"));
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
