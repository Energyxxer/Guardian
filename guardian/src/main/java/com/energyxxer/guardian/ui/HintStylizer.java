package com.energyxxer.guardian.ui;

import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.theme.Theme;
import com.energyxxer.xswing.hints.Hint;

import java.awt.*;

public class HintStylizer {

    public static void style(Hint hint) {
        style(hint, null);
    }

    public static void style(Hint hint, String type) {
        Theme t = GuardianWindow.getTheme();

        hint.setBackgroundColor(t.getColor(Color.BLACK, "Hint."+type+".background","Hint.background"));
        hint.setBorderColor(t.getColor(Color.WHITE, "Hint."+type+".border","Hint.border"));
        hint.setForeground(t.getColor(Color.WHITE, "Hint."+type+".foreground","Hint.foreground","General.foreground"));
        hint.setFont(t.getFont("Hint."+type,"Hint","General"));
    }
}
