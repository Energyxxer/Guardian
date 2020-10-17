package com.energyxxer.guardian.ui.editor.completion.snippets;

import com.energyxxer.commodore.CommandUtils;
import com.energyxxer.enxlex.lexical_analysis.profiles.ScannerContextResponse;
import com.energyxxer.guardian.global.Preferences;
import com.energyxxer.guardian.global.temp.Lang;
import com.energyxxer.guardian.global.temp.lang_defaults.presets.JSONLexerProfile;
import com.energyxxer.guardian.ui.editor.completion.SnippetSuggestion;
import com.energyxxer.util.logger.Debug;

import java.util.ArrayList;

public final class SnippetManager {
    private static ArrayList<Snippet> snippets = new ArrayList<>();

    /**
     * SnippetManager should not be instantiated.
     * */
    private SnippetManager() {

    }

    public static ArrayList<Snippet> getAll() {
        return snippets;
    }

    public static ArrayList<SnippetSuggestion> createSuggestionsForTag(String tag) {
        ArrayList<SnippetSuggestion> list = new ArrayList<>();
        for(Snippet snippet : snippets) {
            if(!snippet.expanderApplied && snippet.isContextEnabledForTag(tag)) {
                list.add(snippet.createSuggestion());
                snippet.expanderApplied = true;
            }
        }
        return list;
    }

    public static void load() {

        for(Lang lang : Lang.values()) {
            lang.addDefaultSnippets(snippets);
        }

        String saveData = Preferences.get("snippets", null);
        if(saveData == null) return;
        snippets.clear();

        while(saveData.length() > 0) {
            Snippet snippet = new Snippet();

            snippet.setEnabled(saveData.charAt(0) == 'e');
            ScannerContextResponse response;
            saveData = saveData.substring(1);

            response = JSONLexerProfile.STRING_LEXER_CONTEXT.analyze(saveData, 0, null);
            saveData = saveData.substring(response.value.length());
            snippet.setShorthand(CommandUtils.parseQuotedString(response.value));

            response = JSONLexerProfile.STRING_LEXER_CONTEXT.analyze(saveData, 0, null);
            saveData = saveData.substring(response.value.length());
            snippet.setDescription(CommandUtils.parseQuotedString(response.value));

            response = JSONLexerProfile.STRING_LEXER_CONTEXT.analyze(saveData, 0, null);
            saveData = saveData.substring(response.value.length());
            snippet.setText(CommandUtils.parseQuotedString(response.value));

            while(saveData.charAt(0) != ';') {
                response = JSONLexerProfile.STRING_LEXER_CONTEXT.analyze(saveData, 0, null);
                saveData = saveData.substring(response.value.length());
                snippet.setContextEnabled(SnippetContext.getContextForCode(CommandUtils.parseQuotedString(response.value)));
            }
            saveData = saveData.substring(1);

            snippets.add(snippet);
        }
    }

    public static void save() {
        StringBuilder sb = new StringBuilder();
        for(Snippet snippet : snippets) {
            sb.append(snippet.getSaveData());
        }
        Debug.log("Saving snippets: " + sb.toString());
        Preferences.put("snippets", sb.toString());
    }
}
