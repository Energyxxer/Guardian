package com.energyxxer.guardian.ui.theme.change;

import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.util.Disposable;
import com.energyxxer.guardian.ui.theme.Theme;

import java.util.ArrayList;

public interface ThemeChangeListener extends Disposable {
	
	ArrayList<ThemeChangeListener> listeners = new ArrayList<>();

	static void addThemeChangeListener(ThemeChangeListener l) {
		addThemeChangeListener(l, false);
	}

	static void addThemeChangeListener(ThemeChangeListener l, boolean priority) {
		if(priority) listeners.add(0, l);
		else listeners.add(l);

		l.themeChanged(GuardianWindow.getTheme());
	}

	default void addThemeChangeListener() {
		addThemeChangeListener(this);
	}
	
	static void dispatchThemeChange(Theme t) {
		for(ThemeChangeListener listener : listeners) {
			listener.themeChanged(t);
		}
	}
	
	void themeChanged(Theme t);

	default void disposeTLM() {
		listeners.remove(this);
	}

	default void dispose() {
		disposeTLM();
	}

}
