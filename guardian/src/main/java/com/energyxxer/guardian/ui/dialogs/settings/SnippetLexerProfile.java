package com.energyxxer.guardian.ui.dialogs.settings;

import com.energyxxer.enxlex.lexical_analysis.profiles.LexerContext;
import com.energyxxer.enxlex.lexical_analysis.profiles.LexerProfile;
import com.energyxxer.enxlex.lexical_analysis.profiles.ScannerContextResponse;
import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenType;

public class SnippetLexerProfile extends LexerProfile {

    private static final TokenType VARIABLE_MARKER = new TokenType("VARIABLE_MARKER"); // $A$
    private static final TokenType END_MARKER = new TokenType("END_MARKER"); // $END$

    public SnippetLexerProfile() {
        //$END$
        LexerContext endContext = new LexerContext() {

            @Override
            public ScannerContextResponse analyze(String str, int startIndex, LexerProfile profile) {
                if(startIndex < str.length() && str.charAt(startIndex) == '$') {
                    for(int i = startIndex+1; i < str.length(); i++) {
                        char c = str.charAt(i);
                        if(c == '$') {
                            if(i != startIndex+1) {
                                String content = str.substring(startIndex, i+1);
                                return ScannerContextResponse.success(content, content.equals("$END$") ? END_MARKER : VARIABLE_MARKER);
                            } else {
                                return ScannerContextResponse.FAILED;
                            }
                        } else if(!Character.isUpperCase(c) && c != '_') {
                            return ScannerContextResponse.FAILED;
                        }
                    }
                }
                return ScannerContextResponse.FAILED;
            }

            @Override
            public boolean handlesType(TokenType type) {
                return type == VARIABLE_MARKER || type == END_MARKER;
            }
        };
        contexts.add(endContext);
    }

    @Override
    public boolean canMerge(char ch0, char ch1) {
        return Character.isJavaIdentifierPart(ch0) && Character.isJavaIdentifierPart(ch1);
    }

    @Override
    public void putHeaderInfo(Token header) {
        header.putAttribute("TYPE","snippet");
        header.putAttribute("DESC","Snippet Preview");
    }
}
