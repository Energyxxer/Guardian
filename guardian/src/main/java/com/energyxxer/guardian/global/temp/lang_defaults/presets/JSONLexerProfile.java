package com.energyxxer.guardian.global.temp.lang_defaults.presets;

import com.energyxxer.enxlex.lexical_analysis.profiles.*;
import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenType;

import java.util.regex.Pattern;

/**
 * Created by User on 2/6/2017.
 */
public class JSONLexerProfile extends LexerProfile {

    /**
     * Holds the previous token for multi-token analysis.
     * */
    private Token tokenBuffer = null;

    private static final Pattern numberRegex = Pattern.compile("(-?(?:\\d*(\\.\\d+)|\\d+)(?:E[+-]\\d+)?)");

    public static final TokenType
            BRACE = new TokenType("BRACE"), // (, ), {, }...
            COMMA = new TokenType("COMMA"), // ,
            COLON = new TokenType("COLON"), // :
            NUMBER = new TokenType("NUMBER"), // 0.1
            STRING_LITERAL = new TokenType("STRING_LITERAL"), // "STRING LITERAL"
            BOOLEAN = new TokenType("BOOLEAN"), // true, false
            COMMENT = new TokenType("COMMENT", false); // true, false

    public static final LexerContext STRING_LEXER_CONTEXT = new StringLiteralLexerContext("\"'", STRING_LITERAL);

    /**
     * Creates a JSON Analysis Profile.
     * */
    public JSONLexerProfile() {
        //String
        LexerContext stringContext = STRING_LEXER_CONTEXT;
        //Numbers
        LexerContext numberContext = new RegexLexerContext(numberRegex, NUMBER, true);
        //Braces
        LexerContext braceContext = new StringMatchLexerContext(BRACE, "[","]","{","}").setOnlyWhenExpected(false);

        //Misc
        LexerContext miscellaneousContext = new StringTypeMatchLexerContext(new String[] {",",":"}, new TokenType[] {COMMA, COLON});

        contexts.add(stringContext);
        contexts.add(braceContext);
        contexts.add(miscellaneousContext);
        contexts.add(numberContext);

        contexts.add(new CommentLexerContext("//", COMMENT) {
            @Override
            public ContextCondition getCondition() {
                return ContextCondition.NONE;
            }
        });
        contexts.add(new CommentLexerContext("/*", "*/", COMMENT) {
            @Override
            public ContextCondition getCondition() {
                return ContextCondition.NONE;
            }
        });
    }

    @Override
    public boolean canMerge(char ch0, char ch1) {
        return Character.isJavaIdentifierPart(ch0) && Character.isJavaIdentifierPart(ch1);
    }

    @Override
    public boolean filter(Token token) {
        if(token.type == TokenType.UNKNOWN) {
            if(token.value.equals("true") || token.value.equals("false")) {
                token.type = BOOLEAN;
            }
        }
        if(token.type == STRING_LITERAL) {
            if(tokenBuffer != null) this.stream.write(tokenBuffer, true);
            tokenBuffer = token;
            return true;
        }
        if(token.type == COLON && tokenBuffer != null) {
            tokenBuffer.putAttribute("IS_PROPERTY", true);
            this.stream.write(tokenBuffer, true);
            tokenBuffer = null;
            return false;
        }
        if(tokenBuffer != null) {
            this.stream.write(tokenBuffer, true);
            tokenBuffer = null;
        }
        return false;
    }

    @Override
    public void putHeaderInfo(Token header) {
        header.putAttribute("TYPE","json");
        header.putAttribute("DESC","JavaScript Object Notation File");
    }
}
