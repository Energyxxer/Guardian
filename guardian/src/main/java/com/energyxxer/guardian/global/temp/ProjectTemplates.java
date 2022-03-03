package com.energyxxer.guardian.global.temp;

import com.energyxxer.commodore.CommandUtils;
import com.energyxxer.guardian.GuardianBinding;
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
    public static Function<String, String> getVariableProvider(File templateRoot, File destinationRoot) {
        String name = destinationRoot.getName();
        return s -> {
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
                    result = destinationRoot.getAbsolutePath();
                    break;
                }
                case "GLOBAL_LIBRARIES_ROOT": {
                    result = Guardian.core.getGlobalLibrariesDir().getAbsolutePath();
                    break;
                }

                case "TEMPLATE_INSIDE_PROJECT_NAME": {
                    if(templateRoot.toPath().startsWith(Guardian.core.getGlobalLibrariesDir().toPath())) {
                        result = templateRoot.toPath().getName(Guardian.core.getGlobalLibrariesDir().toPath().getNameCount()).toString();
                    } else if(templateRoot.toPath().startsWith(Preferences.getWorkspace().toPath())) {
                        result = templateRoot.toPath().getName(Preferences.getWorkspace().toPath().getNameCount()).toString();
                    }
                    break;
                }
                case "TEMPLATE_INSIDE_PROJECT_ROOT": {
                    if(templateRoot.toPath().startsWith(Guardian.core.getGlobalLibrariesDir().toPath())) {
                        result = templateRoot.toPath().getRoot().resolve(templateRoot.toPath().subpath(0, Guardian.core.getGlobalLibrariesDir().toPath().getNameCount()+1)).toString();
                    } else if(templateRoot.toPath().startsWith(Preferences.getWorkspace().toPath())) {
                        result = templateRoot.toPath().getRoot().resolve(templateRoot.toPath().subpath(0, Preferences.getWorkspace().toPath().getNameCount()+1)).toString();
                    }
                    break;
                }

                case "UUID": {
                    result = UUID.randomUUID().toString();
                    break;
                }
                default: {
                    for(GuardianBinding binding : Guardian.bindings) {
                        result = binding.getTemplateVariable(s.toUpperCase(Locale.ENGLISH), destinationRoot, templateRoot);
                        if(result != null) break;
                    }
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
        };
    }

    public static void create(File templateRoot, File destination) {
        destination.mkdirs();
        FileCommons.copyFiles(templateRoot.listFiles(), destination);

        scanForTemplateFiles(destination, getVariableProvider(templateRoot, destination));

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
                    String newContent = replaceVariables(file, variableProvider);
                    FileCommons.writeFile(file.toPath().getParent().resolve(file.getName().substring(0, file.getName().length()-".guardiantemplate".length())), newContent);
                    file.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String replaceVariables(File file, Function<String, String> variableProvider) throws IOException {
        return replaceVariables(new String(Files.readAllBytes(file.toPath()), Guardian.DEFAULT_CHARSET), variableProvider);
    }

    public static String replaceVariables(String text, Function<String, String> variableProvider) {
        StringBuffer newContent = new StringBuffer();
        Matcher matcher = TEMPLATE_VARIABLE_PATTERN.matcher(text);
        while(matcher.find()) {
            String replacement = variableProvider.apply(matcher.group(1));
            if(replacement == null) replacement = matcher.group(0);
            matcher.appendReplacement(newContent, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(newContent);
        newContent.append('\n');
        if(newContent.length() > 0) {
            newContent.setLength(newContent.length()-1);
        }

        return newContent.toString();
    }
}
