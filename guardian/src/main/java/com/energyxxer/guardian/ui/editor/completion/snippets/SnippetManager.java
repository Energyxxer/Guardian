package com.energyxxer.guardian.ui.editor.completion.snippets;

import com.energyxxer.commodore.CommandUtils;
import com.energyxxer.enxlex.lexical_analysis.profiles.ScannerContextResponse;
import com.energyxxer.enxlex.suggestions.Suggestion;
import com.energyxxer.guardian.global.Preferences;
import com.energyxxer.guardian.global.temp.Lang;
import com.energyxxer.guardian.global.temp.lang_defaults.presets.JSONLexerProfile;
import com.energyxxer.guardian.main.window.sections.tools.ConsoleBoard;
import com.energyxxer.util.logger.Debug;

import java.util.ArrayList;
import java.util.List;

public final class SnippetManager {
    private static ArrayList<Snippet> snippets = new ArrayList<>();
    private static boolean everLoaded = false;

    /**
     * SnippetManager should not be instantiated.
     * */
    private SnippetManager() {

    }

    public static ArrayList<Snippet> getAll() {
        return snippets;
    }

    public static void createSuggestionsForTag(String tag, List<Suggestion> list) {
        for(Snippet snippet : snippets) {
            if(!snippet.expanderApplied && snippet.isContextEnabledForTag(tag)) {
                list.add(snippet.createSuggestion());
                snippet.expanderApplied = true;
            }
        }
    }

    public static void load() {

        for(Lang lang : Lang.values()) {
            lang.addDefaultSnippets(snippets);
        }

        String saveData = Preferences.get("snippets", null);
        if(saveData == null || saveData.isEmpty()) return;
        snippets.clear();

        while(saveData.length() > 0) {
            Snippet snippet = new Snippet();

            snippet.setEnabled(saveData.charAt(0) == 'e');
            ScannerContextResponse response;
            saveData = saveData.substring(1);

            response = JSONLexerProfile.STRING_LEXER_CONTEXT.analyze(saveData, 0, null);
            saveData = saveData.substring(response.value.length());
            snippet.setShorthand(CommandUtils.parseQuotedString(response.value));
            response.unlock();

            response = JSONLexerProfile.STRING_LEXER_CONTEXT.analyze(saveData, 0, null);
            saveData = saveData.substring(response.value.length());
            snippet.setDescription(CommandUtils.parseQuotedString(response.value));
            response.unlock();

            response = JSONLexerProfile.STRING_LEXER_CONTEXT.analyze(saveData, 0, null);
            saveData = saveData.substring(response.value.length());
            snippet.setText(CommandUtils.parseQuotedString(response.value));
            response.unlock();

            while(saveData.charAt(0) != ';') {
                response = JSONLexerProfile.STRING_LEXER_CONTEXT.analyze(saveData, 0, null);
                saveData = saveData.substring(response.value.length());
                snippet.setContextEnabled(SnippetContext.getContextForCode(CommandUtils.parseQuotedString(response.value)));
                response.unlock();
            }
            saveData = saveData.substring(1);

            snippets.add(snippet);
        }
        everLoaded = true;
    }

    public static void save() {
        StringBuilder sb = new StringBuilder();
        for(Snippet snippet : snippets) {
            sb.append(snippet.getSaveData());
        }
        Debug.log("Saving snippets: " + sb.toString());
        Preferences.put("snippets", sb.toString());
    }

    static {
        ConsoleBoard.registerCommandHandler("snippets", new ConsoleBoard.CommandHandler() {
            @Override
            public String getDescription() {
                return "Edits snippets";
            }

            @Override
            public void printHelp() {
                Debug.log();
                Debug.log("SNIPPETS: Edits snippets");
                Debug.log();
                Debug.log("Available subcommands:");
                Debug.log("  > snippets reload                     (reloads snippets from preferences)");
            }

            @Override
            public void handle(String[] args, String rawArgs) {
                if(args.length <= 1) {
                    printHelp();
                } else if(args[1].equals("reload")) {
                    load();
                    Debug.log("Reloaded snippets");
                } else printHelp();
            }
        });
    }
}
