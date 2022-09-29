package com.energyxxer.guardian.ui.editor.completion;

import com.energyxxer.enxlex.suggestions.*;
import com.energyxxer.guardian.global.temp.Lang;
import com.energyxxer.prismarine.summaries.SymbolSuggestion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class SuggestionExpander {
    public static Collection<SuggestionToken> expand(Suggestion suggestion, SuggestionDialog dialog, SuggestionModule suggestionModule) {
        if(suggestion instanceof LiteralSuggestion) {
            return Collections.singletonList(new ExpandableSuggestionToken(dialog, ((LiteralSuggestion) suggestion).getPreview(), ((LiteralSuggestion) suggestion).getLiteral(), suggestion));
        } else if(suggestion instanceof ParameterNameSuggestion) {
            return Collections.singletonList(new ParameterNameSuggestionToken(((ParameterNameSuggestion) suggestion).getParameterName()));
        } else if(suggestion instanceof SymbolSuggestion) {
            ArrayList<SuggestionToken> tokens = new ArrayList<>();
            Lang lang = Lang.getLangForFile(dialog.getEditor().getParentModule().getFileForAnalyzer().getPath());
            if(lang != null) {
                lang.expandSymbolSuggestion((SymbolSuggestion) suggestion, tokens, dialog, suggestionModule);
            }
            return tokens;
        } else if(suggestion instanceof ComplexSuggestion) {
            ArrayList<SuggestionToken> tokens = new ArrayList<>();
            Lang lang = Lang.getLangForFile(dialog.getEditor().getParentModule().getFileForAnalyzer().getPath());
            if(lang != null) {
                lang.expandComplexSuggestion((ComplexSuggestion) suggestion, tokens, dialog, suggestionModule);
            }
            return tokens;
        }
        throw new IllegalArgumentException();
    }
}
