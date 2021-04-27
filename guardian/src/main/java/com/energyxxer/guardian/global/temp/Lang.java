package com.energyxxer.guardian.global.temp;

import com.energyxxer.enxlex.lexical_analysis.EagerLexer;
import com.energyxxer.enxlex.lexical_analysis.LazyLexer;
import com.energyxxer.enxlex.lexical_analysis.Lexer;
import com.energyxxer.enxlex.lexical_analysis.inspections.InspectionModule;
import com.energyxxer.enxlex.lexical_analysis.profiles.LexerProfile;
import com.energyxxer.enxlex.lexical_analysis.summary.SummaryModule;
import com.energyxxer.enxlex.lexical_analysis.token.SourceFile;
import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenStream;
import com.energyxxer.enxlex.pattern_matching.TokenMatchResponse;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.report.Notice;
import com.energyxxer.enxlex.report.NoticeType;
import com.energyxxer.enxlex.suggestions.ComplexSuggestion;
import com.energyxxer.enxlex.suggestions.SuggestionModule;
import com.energyxxer.guardian.global.Commons;
import com.energyxxer.guardian.global.temp.lang_defaults.parsing.MCFunctionProductions;
import com.energyxxer.guardian.global.temp.lang_defaults.presets.JSONLexerProfile;
import com.energyxxer.guardian.global.temp.lang_defaults.presets.MCFunctionLexerProfile;
import com.energyxxer.guardian.global.temp.lang_defaults.presets.PropertiesLexerProfile;
import com.energyxxer.guardian.global.temp.projects.Project;
import com.energyxxer.guardian.global.temp.projects.ProjectManager;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.main.window.sections.tools.ConsoleBoard;
import com.energyxxer.guardian.ui.dialogs.settings.SnippetLexerProfile;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.editor.behavior.caret.EditorCaret;
import com.energyxxer.guardian.ui.editor.completion.SuggestionDialog;
import com.energyxxer.guardian.ui.editor.completion.SuggestionToken;
import com.energyxxer.guardian.ui.editor.completion.snippets.Snippet;
import com.energyxxer.prismarine.plugins.syntax.PrismarineMetaLexerProfile;
import com.energyxxer.prismarine.plugins.syntax.PrismarineMetaProductions;
import com.energyxxer.prismarine.summaries.PrismarineSummaryModule;
import com.energyxxer.prismarine.summaries.SummarySymbol;
import com.energyxxer.prismarine.summaries.SymbolSuggestion;
import com.energyxxer.util.Factory;
import com.energyxxer.util.logger.Debug;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;

/**
 * Created by User on 2/9/2017.
 */
public class Lang {
    private static final ArrayList<Lang> registeredLanguages = new ArrayList<>();

    public static final Lang JSON = new Lang("JSON", "JSON",
            false,
            JSONLexerProfile::new,
            "json", "mcmeta", ProjectManager.WORKSPACE_CONFIG_FILE_NAME.substring(1)
    ) {
        {
            setIconName("json");
        }

        @Override
        public boolean isBraceToken(Token token) {
            return token.type == JSONLexerProfile.BRACE;
        }

        @Override
        public boolean isStringToken(Token token) {
            return token.type == JSONLexerProfile.STRING_LITERAL;
        }
    };
    public static final Lang PROPERTIES = new Lang("PROPERTIES", "Properties",
            false,
            PropertiesLexerProfile::new,
            "properties", "lang"
    ) {{this.putProperty("line_comment_marker","#");}};
    public static final Lang MCFUNCTION = new Lang("MCFUNCTION", "Minecraft Function",
            true,
            MCFunctionLexerProfile::new,
            () -> MCFunctionProductions.FILE,
            "mcfunction"
    ) {
        {
            this.putProperty("line_comment_marker","#");
            this.setIconName("function");
        }
    };
    public static final Lang PRISMARINE_SYNTAX = new Lang("PRISMARINE_META_SYNTAX", "Prismarine Meta Syntax",
            false,
            PrismarineMetaLexerProfile::new,
            () -> PrismarineMetaProductions.FILE
    ) {
        {
            this.putProperty("line_comment_marker","//");
        }

        @Override
        public boolean isBraceToken(Token token) {
            return token.type == PrismarineMetaLexerProfile.BRACE;
        }

        @Override
        public boolean isStringToken(Token token) {
            return token.type == PrismarineMetaLexerProfile.STRING_LITERAL;
        }
    };

    public static final Lang SNIPPET = new Lang("GUARDIAN_SNIPPET", "Guardian Editor Snippet",
            false,
            SnippetLexerProfile::new
    );

    private final String code;
    private final String friendlyName;
    private final boolean lazy;
    private final Factory<LexerProfile> lexerProfileFactory;
    private final Factory<TokenPatternMatch> parserProduction;
    private final List<String> extensions;
    private final HashMap<String, String> properties = new HashMap<>();
    private String iconName;

    public Lang(String code, String friendlyName, boolean lazy, Factory<LexerProfile> lexerProfileFactory, String... extensions) {
        this(code, friendlyName, lazy, lexerProfileFactory, null, extensions);
    }

    public Lang(String code, String friendlyName, boolean lazy, Factory<LexerProfile> lexerProfileFactory, Factory<TokenPatternMatch> parserProduction, String... extensions) {
        this.code = code;
        this.friendlyName = friendlyName;
        this.lazy = lazy;
        this.lexerProfileFactory = lexerProfileFactory;
        this.parserProduction = parserProduction;
        this.extensions = new ArrayList<>();
        this.extensions.addAll(Arrays.asList(extensions));

        registeredLanguages.add(this);
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public void addExtension(String extension) {
        extensions.add(extension);
    }

    public LexerProfile createProfile() {
        return lexerProfileFactory.createInstance();
    }

    public Factory<TokenPatternMatch> getParserProduction() {
        return parserProduction;
    }

    public static Lang getLangForFile(String path) {
        if(path == null) return null;
        for(Lang lang : Lang.values()) {
            for(String extension : lang.extensions) {
                if(path.endsWith("." + extension)) {
                    return lang;
                }
            }
        }
        return null;
    }

    public String getIconName() {
        return iconName;
    }

    protected void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public LangAnalysisResponse analyze(File file, String text, SuggestionModule suggestionModule, SummaryModule summaryModule) {
        TokenPatternMatch patternMatch = (parserProduction != null) ? parserProduction.createInstance() : null;
        if(patternMatch == null && lazy) {
            return null;
        }

        Lexer lexer = this.lazy ? new LazyLexer(new TokenStream(true), patternMatch) : new EagerLexer(new TokenStream(true));
        TokenMatchResponse response = null;
        ArrayList<Notice> notices = new ArrayList<>();


        lexer.setSummaryModule(summaryModule);
        if(suggestionModule != null) {
            lexer.setSuggestionModule(suggestionModule);
            suggestionModule.setLexer(lexer);
        }
        lexer.setInspectionModule(new InspectionModule());
        lexer.start(new SourceFile(file), text, createProfile());

        lexer.getStream().tokens.remove(0);

        if(lexer instanceof LazyLexer) {
            response = ((LazyLexer) lexer).getMatchResponse();
        } else {
            if(patternMatch != null) {
                lexer.getStream().tokens.removeIf(token -> !token.type.isSignificant());

                try {
                    response = patternMatch.match(0, lexer);
                    notices.addAll(lexer.getNotices());
                } catch(Exception x) {
                    notices.addAll(lexer.getNotices());
                    GuardianWindow.showException(x);
                    x.printStackTrace();
                    return new LangAnalysisResponse(lexer, null, lexer.getStream().tokens, notices);
                }

                if(response != null && !response.matched) {
                    notices.add(new Notice(NoticeType.ERROR, response.getErrorMessage(), response.faultyToken));
                }
            }
        }

        if(summaryModule != null) {
            for(int pass = 0; ((PrismarineSummaryModule) summaryModule).runFileAwareProcessors(pass); pass++);
        }

        notices.addAll(lexer.getNotices());

        return new LangAnalysisResponse(lexer, response, lexer.getStream().tokens, notices);
    }

    @Override
    public String toString() {
        return code;
    }

    public String getCode() {
        return code;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public static Collection<Lang> values() {
        return registeredLanguages;
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public void putProperty(String key, String value) {
        properties.put(key, value);
    }

    public boolean usesSuggestionModule() {
        return false;
    }

    public PrismarineSummaryModule createSummaryModule() {
        return null;
    }

    public void joinToProjectSummary(SummaryModule summaryModule, File file, Project project) {
        throw new UnsupportedOperationException();
    }

    public void expandComplexSuggestion(ComplexSuggestion suggestion, ArrayList<SuggestionToken> tokens, SuggestionDialog dialog, SuggestionModule suggestionModule) {
    }

    public void expandSymbolSuggestion(SymbolSuggestion suggestion, ArrayList<SuggestionToken> tokens, SuggestionDialog dialog, SuggestionModule suggestionModule) {
    }

    public Image getIconForFile(File file) {
        return (iconName != null) ? Commons.getIcon(iconName) : null;
    }

    public boolean isBraceToken(Token token) {
        return false;
    }
    public boolean isStringToken(Token token) {
        return false;
    }
    public boolean isCommentToken(Token token) {
        return false;
    }

    public void onEditorAdd(AdvancedEditor editor, EditorCaret caret) {

    }

    public void addDefaultSnippets(ArrayList<Snippet> snippets) {

    }

    public String getIconKeyForSuggestionTags(Collection<String> tags) {
        return null;
    }

    public String formatDocumentation(SummarySymbol sym) {
        return null;
    }

    public static class LangAnalysisResponse {
        public Lexer lexer;
        public TokenMatchResponse response;
        public ArrayList<Token> tokens;
        public ArrayList<Notice> notices;

        public LangAnalysisResponse(Lexer lexer, TokenMatchResponse response, ArrayList<Token> tokens, ArrayList<Notice> notices) {
            this.lexer = lexer;
            this.response = response;
            this.tokens = tokens;
            this.notices = notices;
        }
    }

    static {

        ConsoleBoard.registerCommandHandler("langs", new ConsoleBoard.CommandHandler() {
            @Override
            public String getDescription() {
                return "Lists all the loaded languages";
            }

            @Override
            public void printHelp() {
                Debug.log("Lists all of the loaded languages");
            }

            @Override
            public void handle(String[] args, String rawArgs) {
                Debug.log("Languages:");
                for(Lang lang : registeredLanguages) {
                    Debug.log("  - " + lang.getFriendlyName() + " (" + lang.getCode() + ")");
                    Debug.log("    Extensions: " + lang.getExtensions());
                }
            }
        });
    }
}