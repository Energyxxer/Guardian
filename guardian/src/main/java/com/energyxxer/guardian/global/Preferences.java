package com.energyxxer.guardian.global;

import com.energyxxer.commodore.CommandUtils;
import com.energyxxer.guardian.global.temp.projects.ProjectManager;
import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.guardian.main.WorkspaceDialog;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.main.window.sections.tools.ConsoleBoard;
import com.energyxxer.guardian.ui.common.MenuItems;
import com.energyxxer.guardian.ui.theme.change.ThemeChangeListener;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.xswing.ScalableGraphics2D;

import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.function.Function;
import java.util.prefs.BackingStoreException;

public class Preferences {

    private static java.util.prefs.Preferences prefs = java.util.prefs.Preferences
            .userNodeForPackage(Guardian.core.getResourceRegistry());

    private static int baseFontSize = 12;
    private static int editorFontSize = 12;
    private static double globalScaleFactor = 2;

    static {
        if(prefs.get("theme",null) == null) prefs.put("theme", "Electron Dark");
        if(prefs.get("workspace_dir", null) == null) {
            promptWorkspace();
        }
        if(prefs.get("username",null) == null) prefs.put("username", "User");
        baseFontSize = Integer.parseInt(prefs.get("base_font_size","12"));
        if(prefs.get("base_font_size",null) == null) prefs.put("base_font_size", "12");
        editorFontSize = Integer.parseInt(prefs.get("editor_font_size","12"));
        if(prefs.get("editor_font_size",null) == null) prefs.put("editor_font_size", "12");
        setGlobalScaleFactor(Double.parseDouble(prefs.get("global_scale_factor","1")));
    }

    public static LinkedHashSet<String> WORKSPACE_HISTORY = new LinkedHashSet<>();

    static {
        String[] savedWorkspaceHistory = Preferences.get("workspace_history","").split(File.pathSeparator, -1);
        if (savedWorkspaceHistory.length != 1 || !savedWorkspaceHistory[0].isEmpty()) {
            WORKSPACE_HISTORY.addAll(Arrays.asList(savedWorkspaceHistory));
        }
    }

    public static void promptWorkspace() {
        WorkspaceDialog.prompt();
    }

    public static void reset() {
        try {
            prefs.clear();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public static String get(String key, String def) {
        return prefs.get(key, def);
    }

    public static String get(String key) {
        return prefs.get(key, null);
    }

    public static void put(String key, String value) {
        prefs.put(key, value);
    }

    public static void remove(String key) {
        prefs.remove(key);
    }

    public static int getBaseFontSize() {
        return baseFontSize;
    }

    public static int getModifiedFontSize() {
        return (int)Math.floor(baseFontSize * globalScaleFactor);
    }

    public static int getEditorFontSize() {
        return editorFontSize;
    }

    public static int getModifiedEditorFontSize() {
        return getModifiedEditorFontSize(1);
    }

    public static int getModifiedEditorFontSize(float instanceFactor) {
        return (int)Math.floor(editorFontSize * globalScaleFactor * instanceFactor);
    }

    public static void setBaseFontSize(int fontSize) {
        if(fontSize > 0) {
            baseFontSize = fontSize;
            prefs.put("base_font_size", ""+fontSize);
        }
    }

    public static void setEditorFontSize(int fontSize) {
        if(fontSize > 0) {
            editorFontSize = fontSize;
            prefs.put("editor_font_size", ""+fontSize);
        }
    }

    public static void setGlobalScaleFactor(double factor) {

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        double min = 0.25;
        double max = 3 * (gd.getDisplayMode().getHeight() / 1080d);

        factor = Math.max(min, Math.min(factor, max));

        globalScaleFactor = factor;
        ScalableGraphics2D.SCALE_FACTOR = factor;
        prefs.put("global_scale_factor", ""+factor);
        ThemeChangeListener.dispatchThemeChange(GuardianWindow.getTheme());
    }

    public static double getGlobalScaleFactor() {
        return globalScaleFactor;
    }

    static {
        ConsoleBoard.registerCommandHandler("prefs", new ConsoleBoard.CommandHandler() {
            @Override
            public String getDescription() {
                return "Manages the program's preferences";
            }

            @Override
            public void printHelp() {
                Debug.log();
                Debug.log("PREFS: Manages the program's preferences");
                Debug.log();
                Debug.log("Available subcommands:");
                Debug.log("  > prefs list                         (prints a list of all stored keys and their values)");
                Debug.log("  > prefs get <key>                    (prints the stored value for the given preference key)");
                Debug.log("  > prefs set <key> <value...>         (sets the stored value for the given preference key)");
                Debug.log("  > prefs remove <key>                 (removes any value for the specified key)");
                Debug.log("  > prefs clear_i_know_what_i_am_doing (removes preferences set, acting as a hard reset for the program)");
            }

            @Override
            public void handle(String[] args, String rawArgs) {
                if(args.length <= 1) {
                    printHelp();
                } else {
                    switch(args[1]) {
                        case "get": {
                            if(args.length <= 2) {
                                Debug.log("Missing argument <key>");
                                return;
                            }
                            String key = args[2];
                            String value = Preferences.get(key);
                            if(value != null) {
                                value = CommandUtils.quote(value);
                            } else {
                                value = "(unset)";
                            }
                            Debug.log("Value for preference '" + key + "': " + value);
                            return;
                        }
                        case "set": {
                            if(args.length <= 2) {
                                Debug.log("Missing argument <key>");
                                return;
                            }
                            if(args.length <= 3) {
                                Debug.log("Missing argument <value...>");
                                return;
                            }
                            String key = args[2];
                            StringBuilder sb = new StringBuilder();
                            for(int i = 3; i < args.length; i++) {
                                sb.append(args[i]);
                                if(i+1<args.length) sb.append(' ');
                            }
                            String value = sb.toString();

                            Debug.log("Value set for preference '" + key + "': " + CommandUtils.quote(value));
                            Preferences.put(key, value);

                            SettingPref<?> relatedSetting = SettingPref.LOADED_SETTINGS.get(key);
                            if(relatedSetting != null) relatedSetting.load();
                            return;
                        }
                        case "remove": {
                            if(args.length <= 2) {
                                Debug.log("Missing argument <key>");
                                return;
                            }
                            String key = args[2];
                            Preferences.remove(key);
                            Debug.log("Value removed for preference '" + key + "'");

                            SettingPref<?> relatedSetting = SettingPref.LOADED_SETTINGS.get(key);
                            if(relatedSetting != null) relatedSetting.load();
                            return;
                        }
                        case "clear_i_know_what_i_am_doing": {
                            Preferences.reset();
                            Debug.log("All preferences cleared");
                            return;
                        }
                        case "list": {
                            try {
                                for(String key : prefs.keys()) {
                                    Debug.log("" + key + ": " + CommandUtils.quote(Preferences.get(key)));
                                }
                            } catch (BackingStoreException e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                    }
                    printHelp();
                }
            }
        });
        ConsoleBoard.registerCommandHandler("gscale", new ConsoleBoard.CommandHandler() {
            @Override
            public String getDescription() {
                return "Changes the global scale";
            }

            @Override
            public void printHelp() {
                Debug.log();
                Debug.log("GSCALE: Changes the global GUI scale factor");
                Debug.log();
                Debug.log("Available subcommands:");
                Debug.log("  > gscale <scale>                     (sets the scale to the given value)");
            }

            @Override
            public void handle(String[] args, String rawArgs) {
                if(args.length <= 1) {
                    printHelp();
                } else {
                    setGlobalScaleFactor(Double.parseDouble(args[1]));
                }
            }
        });
    }

    public static File getWorkspace() {
        return new File(Preferences.get("workspace_dir", Guardian.core.getDefaultWorkspace().toString()));
    }

    public static void setWorkspace(File workspace) {
        Preferences.put("workspace_dir", workspace.getAbsolutePath());

        WORKSPACE_HISTORY.remove(workspace.getAbsolutePath());
        WORKSPACE_HISTORY.add(workspace.getAbsolutePath());

        saveWorkspaceHistory();
        MenuItems.regenerateChangeWorkspaceMenu();

        ProjectManager.setWorkspaceDir(workspace.toString());
        ProjectManager.loadWorkspace();
        GuardianWindow.projectExplorer.refresh();
        GuardianWindow.toolbar.updateActiveFile();
    }

    private static void saveWorkspaceHistory() {
        String joined = String.join(File.pathSeparator, WORKSPACE_HISTORY);
        Preferences.put("workspace_history", joined);
    }

    public static class SettingPref<T> {
        static final HashMap<String, SettingPref<?>> LOADED_SETTINGS = new HashMap<>();

        private T defaultValue;
        private T value;
        private String key;
        private final Function<String, T> prefToVal;
        private final Function<T, String> valToPref;

        public SettingPref(String key, T defaultValue, Function<String, T> prefToVal) {
            this(key, defaultValue, prefToVal, String::valueOf);
        }

        public SettingPref(String key, T defaultValue, Function<String, T> prefToVal, Function<T, String> valToPref) {
            this.defaultValue = defaultValue;
            this.key = key;
            this.value = defaultValue;
            this.prefToVal = prefToVal;
            this.valToPref = valToPref;

            LOADED_SETTINGS.put(key, this);

            load();
        }

        public T get() {
            return value;
        }

        public void set(T newValue) {
            value = newValue;
            save();
        }

        private void save() {
            Preferences.put(key, valToPref.apply(value));
        }

        private void load() {
            String existing = Preferences.get(key, null);
            if(existing != null) {
                value = prefToVal.apply(existing);
            } else {
                value = defaultValue;
            }
        }
    }
}
