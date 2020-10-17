package com.energyxxer.guardian.ui.theme;

import com.energyxxer.guardian.global.Preferences;
import com.energyxxer.guardian.global.temp.Lang;
import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.main.window.sections.tools.ConsoleBoard;
import com.energyxxer.util.logger.Debug;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by User on 12/13/2016.
 */
public class ThemeManager {

    private static HashSet<String> guiThemesInJar = new HashSet<>();
    private static HashSet<String> syntaxThemesInJar = new HashSet<>();

    private static HashMap<String, Theme> guiThemes = new HashMap<>();
    private static HashMap<String, Theme> syntaxThemes = new HashMap<>();

    private static Theme nullTheme = new Theme("null");

    public static Theme currentGUITheme = new Theme("null");

    public static final Path THEME_DIR_PATH = Guardian.core.getThemeDir();

    public static void loadAll() {
        guiThemes.clear();
        syntaxThemes.clear();

        ThemeReader tr = new ThemeReader();
        for(String file : guiThemesInJar) {
            try {
                Theme theme = tr.read(Theme.ThemeType.GUI_THEME, file);
                if(theme != null) guiThemes.put(theme.getName(),theme);
            } catch(ThemeParserException e) {
                Debug.log(e.getMessage(), Debug.MessageType.WARN);
            }
        }

        for(String file : syntaxThemesInJar) {
            try {
                Theme theme = tr.read(Theme.ThemeType.SYNTAX_THEME, file);
                if(theme != null) syntaxThemes.put(theme.getName(),theme);
            } catch(ThemeParserException e) {
                Debug.log(e.getMessage(), Debug.MessageType.WARN);
            }
        }

        //Read theme directory

        String themeDirPath = THEME_DIR_PATH.toString() + File.separator;

        File themeDir = new File(themeDirPath);
        if(themeDir.exists()) {
            File guiThemeDirectory = new File(themeDirPath + "gui");
            if(guiThemeDirectory.exists()) {
                File[] files = guiThemeDirectory.listFiles();
                if(files != null) {
                    for(File file : files) {
                        try {
                            Theme theme = tr.read(Theme.ThemeType.GUI_THEME, file);
                            if(theme != null) guiThemes.put(theme.getName(),theme);
                        } catch(ThemeParserException e) {
                            Debug.log(e.getMessage(), Debug.MessageType.ERROR);
                        }
                    }
                }

            } else guiThemeDirectory.mkdir();

            File syntaxThemeDirectory = new File(themeDirPath + "syntax");
            if(syntaxThemeDirectory.exists()) {
                File[] files = syntaxThemeDirectory.listFiles();
                if(files != null) {
                    for(File file : files) {
                        try {
                            Theme theme = tr.read(Theme.ThemeType.SYNTAX_THEME, file);
                            if(theme != null) syntaxThemes.put(theme.getName(),theme);
                        } catch(ThemeParserException e) {
                            Debug.log(e.getMessage(), Debug.MessageType.WARN);
                        }
                    }
                }

            } else syntaxThemeDirectory.mkdir();
        } else themeDir.mkdirs();

        setGUITheme(Preferences.get("theme"));

        Debug.log("Loaded " + (guiThemes.size()) + " GUI themes");
        Debug.log("Loaded " + (syntaxThemes.size()) + " syntax themes");
    }

    public static HashMap<String, Theme> getGUIThemes() {
        return guiThemes;
    }

    public static List<Theme> getGUIThemesAsList() {
        return new ArrayList<>(guiThemes.values());
    }

    public static Theme[] getGUIThemesAsArray() {
        return getGUIThemesAsList().toArray(new Theme[0]);
    }

    public static Theme getGUITheme(String name) {
        return (name.equals("null")) ? null : guiThemes.get(name);
    }

    public static Theme getSyntaxTheme(String name) {
        return (name == null || name.equals("null")) ? null : syntaxThemes.get(name);
    }

    public static void setGUITheme(String name) {
        if(name != null && name.equals("null")) {
            Preferences.put("theme","null");
            GuardianWindow.setTheme(nullTheme);
            currentGUITheme = nullTheme;
            return;
        }

        Theme theme = getGUITheme(name);
        if(theme == null) theme = nullTheme;

        Preferences.put("theme",theme.getName());
        GuardianWindow.setTheme(theme);
        currentGUITheme = theme;

    }

    public static Theme getSyntaxForGUITheme(Lang lang, Theme guiTheme) {
        Theme explicitTheme = getSyntaxTheme(guiTheme.getString("Syntax." + lang.getCode().toLowerCase(Locale.ENGLISH)));
        if(explicitTheme != null) return explicitTheme;

        List<Theme> themesForLang = getSyntaxThemesForLanguage(lang);

        String guiThemeStyle = guiTheme.getString("Theme.style", "default:DARK");

        if(themesForLang.isEmpty()) return null;
        if(themesForLang.size() == 1) return themesForLang.get(0);
        themesForLang.sort((a, b) -> {
            String themeAStyle = a.getString("Theme.style", "default:DARK");
            String themeBStyle = b.getString("Theme.style", "default:DARK");

            boolean themeAMatches = themeAStyle.equals(guiThemeStyle);
            boolean themeBMatches = themeBStyle.equals(guiThemeStyle);

            return themeAMatches == themeBMatches ? 0 : (themeAMatches ? -1 : 1);
        });

        Debug.log("Language: " + lang.getCode());
        Debug.log("GUI Theme: " + guiTheme.getName());
        Debug.log("Selected Syntax Theme: " + themesForLang.get(0).getName());

        return themesForLang.get(0);
    }

    private static List<Theme> getSyntaxThemesForLanguage(Lang lang) {
        ArrayList<Theme> themes = new ArrayList<>();
        for(Theme theme : syntaxThemes.values()) {
            String themeLangCode = theme.getString("Theme.language");
            if(themeLangCode != null && themeLangCode.equalsIgnoreCase(lang.getCode())) {
                themes.add(theme);
            }
        }
        return themes;
    }

    public static void registerGUIThemeFromJar(String name) {
        guiThemesInJar.add(name);
    }
    public static void registerSyntaxThemeFromJar(String name) {
        syntaxThemesInJar.add(name);
    }

    static {
        ConsoleBoard.registerCommandHandler("themes", new ConsoleBoard.CommandHandler() {
            @Override
            public String getDescription() {
                return "Lists all the loaded themes";
            }

            @Override
            public void printHelp() {
                Debug.log("Lists all of the loaded GUI and Syntax themes");
            }

            @Override
            public void handle(String[] args) {
                Debug.log("GUI Themes:");
                for(Theme theme : guiThemes.values()) {
                    if(guiThemesInJar.contains(theme.getName())) {
                        Debug.log("  - " + theme.getName() + " (jar)");
                    } else {
                        Debug.log("  - " + theme.getName());
                    }
                }
                Debug.log("Syntax Themes:");
                for(Theme theme : syntaxThemes.values()) {
                    if(syntaxThemesInJar.contains(theme.getName())) {
                        Debug.log("  - " + theme.getName() + " (jar)");
                    } else {
                        Debug.log("  - " + theme.getName());
                    }
                }
            }
        });
    }
}
