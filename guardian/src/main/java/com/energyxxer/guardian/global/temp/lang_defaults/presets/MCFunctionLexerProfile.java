package com.energyxxer.guardian.global.temp.lang_defaults.presets;

import com.energyxxer.enxlex.lexical_analysis.profiles.LexerProfile;
import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenType;
import com.energyxxer.guardian.global.temp.lang_defaults.presets.mcfunction.MCFunction;

import java.util.Locale;
import java.util.regex.Pattern;

public class MCFunctionLexerProfile extends LexerProfile {

    private static final Pattern numberRegex = Pattern.compile("([+-]?\\d+(\\.\\d+)?[bdfsL]?)");

    public MCFunctionLexerProfile() {
    }

    @Override
    public boolean filter(Token token) {
        if(token.type == TokenType.UNKNOWN) {
            if(Character.isJavaIdentifierPart(token.value.charAt(0))) {
                if(token.value.equals(token.value.toLowerCase(Locale.ENGLISH))) {
                    token.type = MCFunction.LOWERCASE_IDENTIFIER;
                } else {
                    token.type = MCFunction.MIXED_IDENTIFIER;
                }
            } else {
                token.type = MCFunction.SYMBOL;
            }
        }
        return false;
    }

    @Override
    public boolean canMerge(char ch0, char ch1) {
        return isValidIdentifierPart(ch0) && isValidIdentifierPart(ch1);
    }

    private boolean isValidIdentifierPart(char ch) {
        return ch != '$' && Character.isJavaIdentifierPart(ch);
    }

    @Override
    public boolean useNewlineTokens() {
        return true;
    }

    @Override
    public void putHeaderInfo(Token header) {
        header.putAttribute("TYPE","mcfunction");
        header.putAttribute("DESC","Minecraft Function File");
    }
}
