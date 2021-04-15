package com.energyxxer.guardian.global.temp.lang_defaults.presets;

import com.energyxxer.enxlex.lexical_analysis.profiles.LexerContext;
import com.energyxxer.enxlex.lexical_analysis.profiles.LexerProfile;
import com.energyxxer.enxlex.lexical_analysis.profiles.ScannerContextResponse;
import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenType;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by User on 4/8/2017.
 */
public class PropertiesLexerProfile extends LexerProfile {

    private static final TokenType
            COMMENT = new TokenType("COMMENT",false),
            KEY = new TokenType("KEY"),
            SEPARATOR = new TokenType("SEPARATOR"),
            VALUE = new TokenType("VALUE");

    private TokenType stage = KEY;

    /**
     * Creates a JSON Analysis Profile.
     * */
    public PropertiesLexerProfile() {
        LexerContext propertyContext = new LexerContext() {
            @Override
            public ScannerContextResponse analyze(String str, int startIndex, LexerProfile profile) {
                if(str.startsWith("\n", startIndex)) {
                    stage = KEY;
                    return ScannerContextResponse.FAILED;
                }
                //If everything else is whitespace, fail.
                for(int i = startIndex; i < str.length(); i++) {
                    char c = str.charAt(i);
                    if(!Character.isWhitespace(c)) break;
                    if(i == str.length()-1) return ScannerContextResponse.FAILED;
                }

                if(stage == KEY) {

                    char firstNonWhitespaceChar = 0;
                    char continueUntil = 0;
                    TokenType tokenType = null;

                    int i;
                    for(i = startIndex; i < str.length(); i++) {
                        char c = str.charAt(i);

                        if(firstNonWhitespaceChar == 0 && !Character.isWhitespace(c)) {
                            firstNonWhitespaceChar = c;

                            if(c == '#') {
                                continueUntil = '\n';
                                tokenType = COMMENT;
                            } else {
                                continueUntil = '=';
                                tokenType = KEY;
                            }
                        }

                        if(c == continueUntil || c == '\n') {
                            if(c == '=') {
                                stage = SEPARATOR;
                            }
                            break;
                        }
                    }

                    //all whitespace, fail
                    if(firstNonWhitespaceChar == 0) return ScannerContextResponse.FAILED;

                    return new ScannerContextResponse(true, str.substring(startIndex, i), tokenType);
                } else if(stage == SEPARATOR) {
                    stage = VALUE;
                    return new ScannerContextResponse(true, "=", SEPARATOR);
                } else if(stage == VALUE) {
                    stage = KEY;
                    int endIndex = str.indexOf('\n', startIndex);
                    if(endIndex == -1) endIndex = str.length();

                    if(startIndex == endIndex) return ScannerContextResponse.FAILED;

                    return new ScannerContextResponse(true, str.substring(startIndex, endIndex), VALUE);
                }
                return null;
            }

            @Override
            public Collection<TokenType> getHandledTypes() {
                return Arrays.asList(KEY, SEPARATOR, VALUE, COMMENT);
            }
        };

        contexts.add(propertyContext);
    }

    @Override
    public void putHeaderInfo(Token header) {
        header.putAttribute("TYPE","properties");
        header.putAttribute("DESC","Java Properties File");
    }
}
