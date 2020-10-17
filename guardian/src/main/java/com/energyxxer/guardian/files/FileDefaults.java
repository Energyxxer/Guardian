package com.energyxxer.guardian.files;

import com.energyxxer.guardian.util.ResourceReader;
import com.energyxxer.util.logger.Debug;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by User on 1/21/2017.
 */
public class FileDefaults {
    public static final HashMap<String, String> defaults = new HashMap<>();

    private static final String[] indexes = new String[0];

    public static void loadAll() {
        defaults.clear();

        for(String name : indexes) {
            defaults.put(name, ResourceReader.read("/resources/defaults/" + name + ".txt").replace("\t","    "));
        }

        Debug.log("Loaded file defaults");
    }

    public static String populateTemplate(String template, HashMap<String, String> variables) {
        for(Map.Entry<String, String> variable : variables.entrySet()) {
            String pattern = Pattern.compile("\\$" + variable.getKey().toUpperCase(Locale.ENGLISH) + "\\$", Pattern.CASE_INSENSITIVE).toString();
            template = template.replaceAll(pattern, variable.getValue());
        }
        return template;
    }
}
