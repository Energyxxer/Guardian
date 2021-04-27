package com.energyxxer.guardian.global.temp;

import com.energyxxer.commodore.CommandUtils;
import com.energyxxer.guardian.global.Preferences;
import com.energyxxer.guardian.global.temp.projects.ProjectManager;
import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.guardian.util.FileCommons;
import com.energyxxer.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectTemplates {
    public static void create(File templateRoot, String name) {
        File destination = Preferences.getWorkspace().toPath().resolve(name).toFile();
        destination.mkdirs();
        FileCommons.copyFiles(templateRoot.listFiles(), destination);

        scanForTemplateFiles(destination, s -> {
            int flagsIndex = s.indexOf(".");
            String flags = "";
            if(flagsIndex >= 0) {
                flags = s.substring(flagsIndex+1);
                s = s.substring(0, flagsIndex);
            }
            String result = null;
            switch(s.toUpperCase(Locale.ENGLISH)) {
                case "PROJECT_NAME": {
                    result = name;
                    break;
                }
                case "INITIALS": {
                    result = StringUtil.getInitials(name);
                    break;
                }

                case "PROJECT_ROOT": {
                    result = destination.getAbsolutePath();
                    break;
                }
                case "GLOBAL_LIBRARIES_ROOT": {
                    result = Guardian.core.getGlobalLibrariesDir().getAbsolutePath();
                    break;
                }

                case "UUID": {
                    result = UUID.randomUUID().toString();
                    break;
                }
            }

            if(result != null) {
                if(flags.contains("E")) {
                    result = CommandUtils.escape(result, true);
                }
                if(flags.contains("Q")) {
                    result = CommandUtils.quote(result);
                }
                if(flags.contains("L")) {
                    result = result.toLowerCase(Locale.ENGLISH);
                }
                if(flags.contains("U")) {
                    result = result.toUpperCase(Locale.ENGLISH);
                }
            }

            return result;
        });

        ProjectManager.loadWorkspace();
    }

    private static final Pattern TEMPLATE_VARIABLE_PATTERN = Pattern.compile("\\$\\$\\$([\\w_]+(?:.[\\w]+))\\$\\$\\$");

    private static void scanForTemplateFiles(File root, Function<String, String> variableProvider) {
        ArrayDeque<File> queue = new ArrayDeque<>();
        queue.add(root);

        while(!queue.isEmpty()) {
            File file = queue.remove();
            try {
                if(file.isDirectory()) {
                    for(File subfile : file.listFiles()) {
                        if(subfile.isDirectory() || subfile.getName().endsWith(".guardiantemplate"))
                            queue.add(subfile);
                    }
                } else if(file.getName().endsWith(".guardiantemplate")) {
                    // Replace template
                    StringBuffer newContent = new StringBuffer();
                    Matcher matcher = null;
                    for(String line : Files.readAllLines(file.toPath())) {
                        if(matcher == null) {
                            matcher = TEMPLATE_VARIABLE_PATTERN.matcher(line);
                        } else {
                            matcher.reset(line);
                        }
                        while(matcher.find()) {
                            String replacement = variableProvider.apply(matcher.group(1));
                            if(replacement == null) replacement = matcher.group(0);
                            matcher.appendReplacement(newContent, Matcher.quoteReplacement(replacement));
                        }
                        matcher.appendTail(newContent);
                        newContent.append('\n');
                    }
                    if(newContent.length() > 0) {
                        newContent.setLength(newContent.length()-1);
                    }
                    FileCommons.writeFile(file.toPath().getParent().resolve(file.getName().substring(0, file.getName().length()-".guardiantemplate".length())), newContent.toString());
                    file.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
