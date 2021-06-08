package com.energyxxer.guardian.global;

import com.energyxxer.guardian.files.FileDefaults;
import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.guardian.ui.commodoreresources.DefinitionPacks;
import com.energyxxer.guardian.ui.commodoreresources.Plugins;
import com.energyxxer.guardian.ui.commodoreresources.TypeMaps;
import com.energyxxer.guardian.ui.commodoreresources.VersionFeatureResources;
import com.energyxxer.guardian.ui.theme.ThemeManager;
import com.energyxxer.guardian.util.LineReader;
import com.energyxxer.util.logger.Debug;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by User on 1/7/2017.
 */
public class Resources {
    public static final ArrayList<String> tips = new ArrayList<>();
    public static JsonObject resources = new JsonObject();
    private static final File resourceInfoFile = Guardian.core.getResourceInfoFile();

    public static final Pattern ISO_8601_REGEX = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z");

    public static final Font MINECRAFT_FONT;

    static {
        Font mcfont = new JLabel().getFont();
        try {
            mcfont = Font.createFont(Font.TRUETYPE_FONT, Resources.class.getResourceAsStream("/assets/fonts/minecraft.ttf"));
            boolean success = GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(mcfont);
            Debug.log(success);
        } catch(FontFormatException | IOException x) {
            x.printStackTrace();
        }
        MINECRAFT_FONT = mcfont;
    }

    public static void load() {
        ThemeManager.registerGUIThemeFromJar("Electron Dark");
        ThemeManager.registerGUIThemeFromJar("Electron Light");

        ThemeManager.registerSyntaxThemeFromJar("JSON Syntax Light");
        ThemeManager.registerSyntaxThemeFromJar("JSON Syntax Dark");
        ThemeManager.registerSyntaxThemeFromJar("Properties Syntax Dark");
        ThemeManager.registerSyntaxThemeFromJar("Properties Syntax Light");
        ThemeManager.registerSyntaxThemeFromJar("Snippet Syntax Dark");
        ThemeManager.registerSyntaxThemeFromJar("MCFunction Syntax Dark");
        ThemeManager.registerSyntaxThemeFromJar("Prismarine Meta Syntax Dark");

        Guardian.core.populateResources();

        tips.clear();
        try {
            ArrayList<String> lines = LineReader.read("/resources/tips.txt");
            tips.addAll(lines);
        } catch(IOException x) {
            x.printStackTrace();
        }


        resourceInfoFile.getParentFile().mkdirs();
        try {
            if(resourceInfoFile.exists()) {
                JsonObject obj = new Gson().fromJson(new FileReader(resourceInfoFile), JsonObject.class);
                if(obj != null) {
                    JsonElement lastCheckedDefCommit = obj.get("last-checked-definition-commit");
                    if(lastCheckedDefCommit != null && lastCheckedDefCommit.isJsonPrimitive() && lastCheckedDefCommit.getAsJsonPrimitive().isString() && ISO_8601_REGEX.matcher(lastCheckedDefCommit.getAsString()).matches()) {
                        resources.addProperty("last-checked-definition-commit", lastCheckedDefCommit.getAsString());
                    }
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }

        ThemeManager.loadAll();
        FileDefaults.loadAll();
        DefinitionPacks.loadAll();
        Plugins.loadAll();
        VersionFeatureResources.loadAll();
        TypeMaps.loadAll();
    }

    public static void saveAll() {
        try {
            Files.write(resourceInfoFile.toPath(), new Gson().toJson(resources).getBytes());
        } catch (IOException x) {
            x.printStackTrace();
        }
    }
}
