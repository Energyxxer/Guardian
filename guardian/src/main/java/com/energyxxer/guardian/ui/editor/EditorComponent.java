package com.energyxxer.guardian.ui.editor;

import com.energyxxer.enxlex.lexical_analysis.LazyLexer;
import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenSection;
import com.energyxxer.enxlex.suggestions.SuggestionModule;
import com.energyxxer.guardian.global.Commons;
import com.energyxxer.guardian.global.Preferences;
import com.energyxxer.guardian.global.Status;
import com.energyxxer.guardian.global.temp.Lang;
import com.energyxxer.guardian.global.temp.projects.Project;
import com.energyxxer.guardian.global.temp.projects.ProjectManager;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.main.window.sections.tools.ConsoleBoard;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.editor.behavior.IndentationManager;
import com.energyxxer.guardian.ui.editor.completion.SuggestionDialog;
import com.energyxxer.guardian.ui.editor.inspector.Inspector;
import com.energyxxer.prismarine.summaries.PrismarineSummaryModule;
import com.energyxxer.util.logger.Debug;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Timer;
import java.util.*;
import java.util.function.Function;

public class EditorComponent extends AdvancedEditor implements KeyListener, CaretListener, FocusListener {

    public static boolean logHighlighterTimes = false;

    private static final int MAX_HIGHLIGHTED_TOKENS_PER_LINE = 150;
    private EditorModule parent;

    private StyledDocument sd;

    private Inspector inspector = null;
    private SuggestionDialog suggestionBox = new SuggestionDialog(this);

    private long lastEdit;

    private final Timer timer = new Timer();
    private Thread highlightingThread = null;
    private HighlightingWorker highlightingWorker = null;

    public static final Preferences.SettingPref<Integer> AUTOREPARSE_DELAY = new Preferences.SettingPref<>("settings.editor.auto_reparse_delay", 500, Integer::parseInt);
    public static final Preferences.SettingPref<Boolean> SHOW_SUGGESTIONS = new Preferences.SettingPref<>("settings.editor.show_suggestions", true, Boolean::parseBoolean);


    EditorComponent(EditorModule parent) {
        this.parent = parent;

        this.setPaddingEnabled(true);
        sd = this.getStyledDocument();

        //if(Lang.getLangForFile(parent.associatedTab.path) != null) this.inspector = new Inspector(this);
        if(parent.file != null && parent.getLanguage() != null && parent.getLanguage().getParserProduction() != null) {
            this.inspector = new Inspector(this);
        }

        this.addCaretListener(this);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerTicked();
            }
        }, 20, 20);

        //this.setTransferHandler(GuardianWindow.editArea.dragToOpenFileHandler);
        this.setTransferHandler(new TransferHandler("string") {
            @Override
            public boolean importData(TransferSupport support) {
                Debug.log("called importData");
                if(support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    return GuardianWindow.editArea.dragToOpenFileHandler.importData(support);
                } else {
                    return getEditorTransferHandler().importData(support);
                }
            }

            @Override
            public boolean canImport(TransferSupport support) {
                return getEditorTransferHandler().canImport(support) || GuardianWindow.editArea.dragToOpenFileHandler.canImport(support);
            }

            @Nullable
            @Override
            protected Transferable createTransferable(JComponent c) {
                return EditorComponent.this.createTransferable();
            }

            @Override
            public void exportAsDrag(JComponent comp, InputEvent e, int action) {
                super.exportAsDrag(comp, e, action);
            }

            @Override
            public int getSourceActions(JComponent c) {
                return TransferHandler.COPY_OR_MOVE | getEditorTransferHandler().getSourceActions(c);
            }
        });

        this.setSuggestionInterface(suggestionBox);

        //this.setOpaque(false);
        this.addFocusListener(this);

        Lang lang = parent.getLanguage();
        if(lang != null) {
            lang.onEditorAdd(this, this.getCaret());
        }
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        super.caretUpdate(e);
        displayCaretInfo();
        parent.ensureVisible(getCaret().getDot());
    }

    public int getCaretWordPosition() {
        int index = this.getCaretPosition();
        if(index <= 0) return 0;
        while (true) {
            char c = 0;
            try {
                c = this.getDocument().getText(index-1, 1).charAt(0);
            } catch (BadLocationException x) {
                x.printStackTrace();
            }
            if (!(Character.isJavaIdentifierPart(c) && c != '$') || --index < 1)
                break;
        }
        return index;
    }

    public int getSoftCaretWordPosition() {
        int index = this.getCaretPosition();
        if(index <= 0) return 0;
        while (true) {
            char c = 0;
            try {
                c = this.getDocument().getText(index-1, 1).charAt(0);
            } catch (BadLocationException x) {
                x.printStackTrace();
            }
            if (!((Character.isJavaIdentifierPart(c) && c != '$') || "$#:/.-".contains(c+"")) || --index < 1)
                break;
        }
        return index;
    }

    private Status errorStatus = new Status();

    private Lang.LangAnalysisResponse startSyntaxHighlighting() {
        if(parent.syntax == null) return null;

        try {
            long startTime = System.currentTimeMillis();
            String text = getText();

            Lang lang = parent.getLanguage();
            if (lang == null) return null;
            Project project = parent.file != null ? ProjectManager.getAssociatedProject(parent.file) : null;

            SuggestionModule suggestionModule = (SHOW_SUGGESTIONS.get() && project != null && lang.usesSuggestionModule()) ? new SuggestionModule(this.getCaretWordPosition(), this.getCaretPosition()) : null;
            PrismarineSummaryModule summaryModule = project != null ? lang.createSummaryModule() : null;

            if(summaryModule != null) {
                lang.joinToProjectSummary(summaryModule, parent.file, project);
            }

            File file = parent.getFileForAnalyzer();
            if(Thread.interrupted()) return null;
            if(logHighlighterTimes) Debug.log("Pre analysis time: " + (System.currentTimeMillis() - startTime) + " ms");
            startTime = System.currentTimeMillis();
            Lang.LangAnalysisResponse analysis = file != null ? lang.analyze(file, text, suggestionModule, summaryModule) : null;
            if(logHighlighterTimes) Debug.log("Analysis time: " + (System.currentTimeMillis() - startTime) + " ms");
            startTime = System.currentTimeMillis();
            if (analysis == null) return null;

            if(Thread.interrupted()) return null;
            return analysis;
        } catch(Exception x) {
            x.printStackTrace();
            GuardianWindow.showException(x);
        }
        return null;
    }

    private final ArrayList<String> previousTokenStyles = new ArrayList<>();

    private void performTokenStyling(Lang.LangAnalysisResponse analysis, Lang lang, HighlightingWorker worker) {
        long startTime = System.currentTimeMillis();
        try {
            Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

            if(analysis.response != null) suggestionBox.setSummary((PrismarineSummaryModule) analysis.lexer.getSummaryModule(), analysis.response.matched);
            if(analysis.lexer.getSuggestionModule() != null) {
                suggestionBox.showSuggestions(analysis.lexer.getSuggestionModule());
            }
            if(logHighlighterTimes) Debug.log("Suggestion update time: " + (System.currentTimeMillis() - startTime) + " ms");
            startTime = System.currentTimeMillis();

            if(Thread.interrupted()) return;

            Token prevToken = null;
            previousTokenStyles.clear();

            if(this.inspector != null) {
                this.inspector.clear();
                this.inspector.setInspectionModule(analysis.lexer.getInspectionModule());
                this.inspector.insertNotices(analysis.notices);
            }
            if(logHighlighterTimes)  Debug.log("Inspection update time: " + (System.currentTimeMillis() - startTime) + " ms");
            startTime = System.currentTimeMillis();

            if(Thread.interrupted()) return;

            if(analysis.response != null && !analysis.response.matched) {
                errorStatus.setMessage(analysis.response.getErrorMessage() + (analysis.response.faultyToken != null ? ". (line " + analysis.response.faultyToken.loc.line + " column " + analysis.response.faultyToken.loc.column + ")" : ""));
                GuardianWindow.setStatus(errorStatus);
                if(analysis.response.faultyToken != null && analysis.response.faultyToken.value != null && analysis.response.faultyToken.loc != null) sd.setCharacterAttributes(analysis.response.faultyToken.loc.index, analysis.response.faultyToken.value.length(), EditorComponent.this.getStyle("error"), true);
                if(analysis.lexer instanceof LazyLexer) return;
            }

            if(logHighlighterTimes) Debug.log("Error update time: " + (System.currentTimeMillis() - startTime) + " ms");
            startTime = System.currentTimeMillis();

            if(Thread.interrupted()) return;

            int tokensInLine = 0;

            for(Token token : analysis.lexer.getStream().tokens) {
                if(Thread.interrupted()) return;
                boolean shouldPaintStyles = true;
                if(prevToken != null && prevToken.loc.line != token.loc.line) tokensInLine = 0;
                tokensInLine++;
                if(tokensInLine > MAX_HIGHLIGHTED_TOKENS_PER_LINE) {
                    shouldPaintStyles = false;
                }
                Style style = EditorComponent.this.getStyle(token.type.toString().toLowerCase(Locale.ENGLISH));

                int previousTokenStylesIndex = previousTokenStyles.size();

                int styleStart = token.loc.index;

                if(shouldPaintStyles) {
                    if(style != null)
                        worker.setCharacterAttributes(token.loc.index, token.value.length(), style, true);
                    else
                        worker.setCharacterAttributes(token.loc.index, token.value.length(), defaultStyle, true);

                    if(token.getAttributes() != null) {
                        for(Map.Entry<String, Object> entry : token.getAttributes().entrySet()) {
                            if(!entry.getValue().equals(true)) continue;
                            Style attrStyle = EditorComponent.this.getStyle("~" + entry.getKey().toLowerCase(Locale.ENGLISH));
                            if(attrStyle == null) continue;

                            if(prevToken != null && previousTokenStyles.contains(entry.getKey().toLowerCase(Locale.ENGLISH))) {
                                styleStart = prevToken.loc.index + prevToken.value.length();
                            }
                            previousTokenStyles.add(entry.getKey().toLowerCase(Locale.ENGLISH));

                            worker.setCharacterAttributes(styleStart, token.value.length() + (token.loc.index - styleStart), attrStyle, false);
                        }
                    }
                    if(token.getSubSections() != null) {
                        for(Map.Entry<TokenSection, String> entry : token.getSubSections().entrySet()) {
                            TokenSection section = entry.getKey();
                            Style attrStyle = EditorComponent.this.getStyle("~" + entry.getValue().toLowerCase(Locale.ENGLISH));
                            if(attrStyle == null) continue;

                            worker.setCharacterAttributes(token.loc.index + section.start, section.length, attrStyle, false);
                        }
                    }
                    for(String tag : token.tags) {
                        Style attrStyle = EditorComponent.this.getStyle("$" + tag.toLowerCase(Locale.ENGLISH));
                        if(attrStyle == null) continue;

                        if(prevToken != null && previousTokenStyles.contains(tag.toLowerCase(Locale.ENGLISH))) {
                            styleStart = prevToken.loc.index + prevToken.value.length();
                        }
                        previousTokenStyles.add(tag.toLowerCase(Locale.ENGLISH));

                        worker.setCharacterAttributes(styleStart, token.value.length() + (token.loc.index - styleStart), attrStyle, false);
                    }

                    if(analysis.response != null) {
                        for (Map.Entry<String, String[]> entry : Collections.synchronizedSet(parent.parserStyles.entrySet())) {
                            String[] tagList = entry.getValue();
                            int startIndex = -1;
                            tgs:
                            do {
                                startIndex = indexOf(token.tags, tagList[0], startIndex + 1);
                                if (startIndex < 0) break;
                                for (int i = 0; i < tagList.length; i++) {
                                    if (startIndex + i >= token.tags.size() || !tagList[i].equalsIgnoreCase(token.tags.get(startIndex + i)))
                                        continue tgs;
                                }
                                Style attrStyle = EditorComponent.this.getStyle(entry.getKey());
                                if (prevToken != null && previousTokenStyles.contains(entry.getKey())) {
                                    styleStart = prevToken.loc.index + prevToken.value.length();
                                }
                                if(entry.getKey().equals("$class_function.dynamic_function.dynamic_function.code_block.statement_list")) {
                                    worker.setParagraphAttributes(styleStart, token.value.length() + (token.loc.index - styleStart), parent.collapsedParagraphStyle, false);
                                }
                                if (attrStyle == null) continue;
                                previousTokenStyles.add(entry.getKey());
                                worker.setCharacterAttributes(styleStart, token.value.length() + (token.loc.index - styleStart), attrStyle, false);
                            } while (true);
                        }
                    }
                }
                while(previousTokenStylesIndex > 0) {
                    previousTokenStyles.remove(0);
                    previousTokenStylesIndex--;
                }

                if(getIndentationManager().getBraceMatcher().matcher(token.value).find() && !lang.isBraceToken(token)) {
                    worker.setCharacterAttributes(token.loc.index, token.value.length(), getStyle(IndentationManager.NULLIFY_BRACE_STYLE), false);
                }

                if(lang.isStringToken(token)) {
                    worker.setCharacterAttributes(token.loc.index, token.value.length(), getStyle(AdvancedEditor.STRING_STYLE), false);

                    if(token.getSubSections() != null) {
                        for(TokenSection section : token.getSubSections().keySet()) {
                            worker.setCharacterAttributes(token.loc.index + section.start, section.length, getStyle(AdvancedEditor.STRING_ESCAPE_STYLE), false);
                        }
                    }
                }


                prevToken = token;
            }
            previousTokenStyles.clear();
            if(logHighlighterTimes) Debug.log("Character Attribute Update time: " + (System.currentTimeMillis() - startTime) + " ms");
            startTime = System.currentTimeMillis();

            if(analysis.response == null || analysis.response.matched) GuardianWindow.dismissStatus(errorStatus);

            if(Thread.interrupted()) return;
            worker.setParagraphAttributes(0, sd.getLength(), defaultStyle, false);

            if(logHighlighterTimes) Debug.log("Post Highlight time: " + (System.currentTimeMillis() - startTime) + " ms");
            startTime = System.currentTimeMillis();

        } catch(Exception x) {
            x.printStackTrace();
            GuardianWindow.showException(x);
        }
    }

    private static <T> int indexOf(ArrayList<T> arr, T value, int fromIndex) {
        for(int i = fromIndex; i < arr.size(); i++) {
            if(Objects.equals(arr.get(i), value)) return i;
        }
        return -1;
    }

    void highlight() {
        parent.highlightTime = System.currentTimeMillis();
        lastEdit = System.currentTimeMillis();
    }

    public void timerTicked() {
        if (lastEdit > -1 && System.currentTimeMillis() - lastEdit > AUTOREPARSE_DELAY.get() && (parent.associatedTab == null || parent.associatedTab.isActive())) {
            lastEdit = -1;
            if(highlightingThread != null) {
                highlightingWorker.cancel(true);
            }
            highlightingThread = new Thread(highlightingWorker = new HighlightingWorker() {
                @Override
                protected Lang.LangAnalysisResponse doInBackground() {
                    Lang.LangAnalysisResponse analysis = startSyntaxHighlighting();
                    if(analysis != null) {
                        performTokenStyling(analysis, parent.getLanguage(), this);
                    }
                    return null;
                }

                @Override
                protected void process(List<StyleChangeInstruction> chunks) {
                    for(StyleChangeInstruction instruction : chunks) {
                        instruction.perform(sd);
                    }
                }
            },"Text Highlighter");
            highlightingThread.setUncaughtExceptionHandler((t, e) -> {
                if(e instanceof ThreadDeath) return;
                e.printStackTrace();
                if(e instanceof Exception) {
                    GuardianWindow.showException((Exception) e);
                } else {
                    GuardianWindow.showException(e.getMessage());
                }
            });
            highlightingWorker.execute();

            Project project = ProjectManager.getAssociatedProject(parent.file);
            if(project != null && project.getSummary() == null) {
                Commons.index(project);
            }
        }
    }

    @Override
    public void registerCharacterDrift(Function<Integer, Integer> h) {
        super.registerCharacterDrift(h);

        if(this.inspector != null) this.inspector.registerCharacterDrift(h);
    }

    @Override
    public void repaint() {
        if(this.getParent() instanceof JViewport && this.getParent().getParent() instanceof JScrollPane) {
            this.getParent().getParent().repaint();
        } else super.repaint();
    }

    void displayCaretInfo() {
        GuardianWindow.statusBar.setCaretInfo(getCaretInfo());
        GuardianWindow.statusBar.setSelectionInfo(getSelectionInfo());
    }

    @Override
    public String getText() {
        try {
            return getDocument().getText(0, getDocument().getLength());
        } catch (BadLocationException x) {
            x.printStackTrace();
        }
        return null;
    }

    @Override
    public void dispose() {
        super.dispose();
        timer.cancel();
        timer.purge();
        suggestionBox.dispose();
    }

    @Override
    public void focusGained(FocusEvent e) {
        GuardianWindow.projectExplorer.clearSelected();
    }

    @Override
    public void focusLost(FocusEvent e) {

    }

    public EditorModule getParentModule() {
        return parent;
    }

    public void showHints() {
        if(inspector != null) {
            inspector.showHints(getCaretPosition());
        }
    }

    public void viewportChanged() {
        if(suggestionBox != null) {
            suggestionBox.relocate();
        }
        if(inspector != null) {
            inspector.getDialog().relocate();
        }
    }

    @Override
    public void caretChanged() {
        super.caretChanged();
        if(inspector != null) {
            inspector.getDialog().dismiss(false);
        }
    }

    private static abstract class StyleChangeInstruction {
        protected int offset;
        protected int length;
        protected AttributeSet style;
        protected boolean replace;

        public StyleChangeInstruction(int offset, int length, AttributeSet style, boolean replace) {
            this.offset = offset;
            this.length = length;
            this.style = style;
            this.replace = replace;
        }

        public abstract void perform(StyledDocument sd);
    }

    private static class CharacterStyleChangeInstruction extends StyleChangeInstruction {

        public CharacterStyleChangeInstruction(int offset, int length, AttributeSet style, boolean replace) {
            super(offset, length, style, replace);
        }

        @Override
        public void perform(StyledDocument sd) {
            sd.setCharacterAttributes(offset, length, style, replace);
        }
    }

    private static class ParagraphStyleChangeInstruction extends StyleChangeInstruction {

        public ParagraphStyleChangeInstruction(int offset, int length, AttributeSet style, boolean replace) {
            super(offset, length, style, replace);
        }

        @Override
        public void perform(StyledDocument sd) {
            sd.setParagraphAttributes(offset, length, style, replace);
        }
    }

    private abstract static class HighlightingWorker extends SwingWorker<Object, StyleChangeInstruction> {
        public void setCharacterAttributes(int offset, int length, AttributeSet style, boolean replace) {
            publish(new CharacterStyleChangeInstruction(offset, length, style, replace));
        }
        public void setParagraphAttributes(int offset, int length, AttributeSet style, boolean replace) {
            publish(new ParagraphStyleChangeInstruction(offset, length, style, replace));
        }
    }

    static {
        ConsoleBoard.registerCommandHandler("highlightertimes", new ConsoleBoard.CommandHandler() {
            @Override
            public String getDescription() {
                return "Toggles logging of analysis, suggestion and syntax highlighting times";
            }

            @Override
            public void printHelp() {
                Debug.log();
                Debug.log("HIGHLIGHTERTIMES: Toggles logging of analysis, suggestion and syntax highlighting times");
            }

            @Override
            public void handle(String[] args, String rawArgs) {
                logHighlighterTimes = !logHighlighterTimes;
                Debug.log("Logging of highlighter times is now: " + logHighlighterTimes);
            }
        });
    }
}
