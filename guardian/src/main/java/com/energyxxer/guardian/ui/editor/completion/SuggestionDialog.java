package com.energyxxer.guardian.ui.editor.completion;

import com.energyxxer.enxlex.suggestions.Suggestion;
import com.energyxxer.enxlex.suggestions.SuggestionModule;
import com.energyxxer.guardian.global.Status;
import com.energyxxer.guardian.global.keystrokes.KeyMap;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.main.window.sections.quick_find.StyledExplorerMaster;
import com.energyxxer.guardian.ui.common.KeyFixDialog;
import com.energyxxer.guardian.ui.common.transactions.CompoundTransaction;
import com.energyxxer.guardian.ui.editor.EditorComponent;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.guardian.ui.editor.behavior.caret.Dot;
import com.energyxxer.guardian.ui.editor.behavior.edits.DeletionEdit;
import com.energyxxer.guardian.ui.editor.behavior.edits.InsertionEdit;
import com.energyxxer.guardian.ui.editor.completion.snippets.Snippet;
import com.energyxxer.guardian.ui.editor.completion.snippets.SnippetManager;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.ui.scrollbar.OverlayScrollPane;
import com.energyxxer.guardian.ui.styledcomponents.StyledLabel;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.guardian.util.ConcurrencyUtil;
import com.energyxxer.prismarine.summaries.PrismarineSummaryModule;
import com.energyxxer.util.Lazy;
import com.energyxxer.util.StringBounds;
import com.energyxxer.util.StringUtil;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.xswing.ScalableDimension;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SuggestionDialog extends KeyFixDialog implements KeyListener, FocusListener, SuggestionInterface {
    private EditorComponent editor;

    private OverlayScrollPane scrollPane;
    private StyledLabel parameterLabel;
    private StyledExplorerMaster explorer;

    private ThemeListenerManager tlm = new ThemeListenerManager();

    private PrismarineSummaryModule summary = null;
    private PrismarineSummaryModule lastSuccessfulSummary = null;

    private boolean locked = false;
    private boolean forceLocked = false;

    private SuggestionModule activeResults = null;

    private final ArrayList<ExpandableSuggestionToken> activeTokens = new ArrayList<>();
    private boolean safe = false;

    public SuggestionDialog(EditorComponent editor) {
        super(GuardianWindow.jframe, false);
        this.setUndecorated(true);
        this.editor = editor;

        this.explorer = new StyledExplorerMaster("EditorSuggestions");

        JPanel contentPane = new JPanel(new BorderLayout());
        scrollPane = new OverlayScrollPane(tlm, explorer);
        contentPane.add(scrollPane);
        contentPane.add(parameterLabel = new StyledLabel(" <ENTITY>", "EditorSuggestions.header", tlm), BorderLayout.NORTH);

        this.setContentPane(contentPane);

        tlm.addThemeChangeListener(t -> {
            int thickness = Math.max(t.getInteger(1, "EditorSuggestions.border.thickness"), 0);
            contentPane.setBackground(t.getColor(new Color(200, 200, 200), "EditorSuggestions.header.background"));
            contentPane.setBorder(BorderFactory.createMatteBorder(thickness, thickness, thickness, thickness, t.getColor(new Color(200, 200, 200), "EditorSuggestions.border.color")));
            parameterLabel.setBorder(BorderFactory.createMatteBorder(0, 0, thickness, 0, t.getColor(new Color(200, 200, 200), "EditorSuggestions.border.color")));
            explorer.setFont(editor.getFont());
        });

        editor.addKeyListener(this);
        editor.addFocusListener(this);
        this.addKeyListener(this);
        contentPane.addKeyListener(this);
        scrollPane.addKeyListener(this);
        explorer.addKeyListener(this);

        try {
            editor.getHighlighter().addHighlight(0, 0, new SnippetVariableHighlighter());
        } catch (BadLocationException ignore) {
            //Literally impossible
        }

        editor.addCharacterDriftListener(h -> {
            if(summary != null) {
                summary.updateIndices(h);
            }
            if(!snippetProfilesLocked) {
                if(snippetEnd != null) snippetEnd.apply(h);
                int i = 0;
                for(CaretProfile profile : snippetVariables) {
                    if(i == snippetVariableActive) {
                        for(int j = 0; j < profile.size(); j++) {
                            int before = profile.get(j);
                            int after = h.apply(before);

                            if(j % 2 == 0) {
                                if(before != after && h.apply(before-1) == before -1) {
                                    //This is the start of the edit, keep it where it is
                                } else {
                                    profile.set(j, after);
                                }
                            } else {
                                profile.set(j, after);
                            }
                        }
                    } else {
                        profile.apply(h);
                    }
                    i++;
                }
            }
        });
    }

    public void showSuggestions(SuggestionModule results) {
        if(this.isVisible()) return;
        if(!safe) return;
        activeResults = results;
        ConcurrencyUtil.runAsync(() -> {
            explorer.clear();
            activeTokens.clear();

            boolean any = false;
            boolean anyExpandable = false;

            if(results != null) {
                for(Snippet snippet : SnippetManager.getAll()) {
                    snippet.expanderApplied = false;
                }
                StringBuilder headerSB = new StringBuilder();
                boolean createdEverywhereSnippets = false;
                for (int i = 0; i < results.getSuggestions().size(); i++) {
                    Suggestion suggestion = results.getSuggestions().get(i);
                    for (SuggestionToken token : SuggestionExpander.expand(suggestion, this, results)) {
                        if(token instanceof ExpandableSuggestionToken) {
                            SuggestionExplorerItem item = new SuggestionExplorerItem(((ExpandableSuggestionToken) token), explorer);
                            item.setDetailed(true);
                            explorer.addElement(item);
                            activeTokens.add(((ExpandableSuggestionToken) token));
                            if (!anyExpandable) {
                                item.setSelected(true);
                                explorer.setSelected(item, null);
                            }
                            anyExpandable = true;
                        } else if(token instanceof ParameterNameSuggestionToken) {
                            headerSB.append(((ParameterNameSuggestionToken) token).getParameterName());
                            headerSB.append(", ");
                        }
                        any = true;
                    }
                    if(!createdEverywhereSnippets && i == results.getSuggestions().size()-1) {
                        SnippetManager.createSuggestionsForTag(null, results.getSuggestions());
                        createdEverywhereSnippets = true;
                    }
                }
                if(headerSB.length() > 0) {
                    headerSB.setLength(headerSB.length()-2);
                    parameterLabel.setText(" <" + headerSB.toString() + ">");
                } else {
                    parameterLabel.setText("");
                }
            }

            if(any) {
                SwingUtilities.invokeLater(() -> {
                    this.setVisible(true);
                    filter();
                    relocate(Math.min(results.getSuggestionIndex(), editor.getDocument().getLength()));
                    editor.requestFocus();
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    this.setVisible(false);
                });
            }
        });
    }

    private static Pattern SNIPPET_MARKER_PATTERN = Pattern.compile("\\$([A-Z_]+)\\$");

    private final ArrayList<CaretProfile> snippetVariables = new ArrayList<>();
    private final ArrayList<String> snippetVariableNames = new ArrayList<>();
    private CaretProfile snippetEnd = new CaretProfile();
    private int snippetVariableActive = -1;
    private boolean snippetProfilesLocked = false;

    public void submit(String text, Suggestion suggestion, boolean dismiss, int endIndex) {
        int deletionsInSuggestion = StringUtil.getSequenceCount(text, "\b");
        submit(text.substring(deletionsInSuggestion), suggestion, dismiss, endIndex, editor.getCaretPosition() - activeResults.getSuggestionIndex() + deletionsInSuggestion, SNIPPET_MARKER_PATTERN);
    }

    public void submit(String text, Suggestion suggestion, boolean dismiss, int endIndex, int driftFromCaret, Pattern snippetMarkerPattern) {
        if(dismiss) {
            this.setVisible(false);
        } else {
            this.forceLocked = true;
        }

        boolean thisSuggestionHasVariables = false;

        int deletionsInSuggestion = StringUtil.getSequenceCount(text, "\b");


        if(suggestion instanceof SnippetSuggestion) {
            text = text.replace("\n", "\n" + editor.getIndentationManager().indent(editor.getDocumentIndentationAt(editor.getCaretPosition())));

            Matcher matcher = snippetMarkerPattern.matcher(text);
            StringBuffer sb = new StringBuffer();

            int drift = 0;
            while(matcher.find()) {

                if(!thisSuggestionHasVariables) {
                    thisSuggestionHasVariables = true;
                    snippetEnd.clear();
                    snippetVariables.clear();
                    snippetVariableNames.clear();
                    snippetVariableActive = -1;
                    snippetProfilesLocked = true;
                }

                String varName = matcher.group(1);

                CaretProfile profileToAppend;
                if("END".equals(varName)) {
                    matcher.appendReplacement(sb, "");
                    profileToAppend = snippetEnd;
                } else {
                    matcher.appendReplacement(sb, varName);
                    if(!snippetVariableNames.contains(varName)) {
                        snippetVariableNames.add(varName);
                        snippetVariables.add(new CaretProfile());
                    }

                    int varIndex = snippetVariableNames.indexOf(varName);

                    profileToAppend = snippetVariables.get(varIndex);
                    if(snippetVariableActive == -1) {
                        snippetVariableActive = varIndex;
                    }
                }

                int driftThisBy = matcher.group(0).length() - ("END".equals(varName) ? 0 : varName.length());

                for(Dot dot : editor.getCaret().getDots()) {
                    profileToAppend.add(dot.getMin() + matcher.start() + drift - driftFromCaret, dot.getMin() + matcher.end() + drift - driftFromCaret - driftThisBy);
                }

                drift -= driftThisBy;
            }
            matcher.appendTail(sb);

            text = sb.toString();
        }

        String finalText = text.substring(deletionsInSuggestion);

        CompoundTransaction<AdvancedEditor> edit = new CompoundTransaction<>();
        edit.append(new Lazy<>(() -> new DeletionEdit(editor, driftFromCaret)));
        edit.append(new Lazy<>(() -> new InsertionEdit(finalText, editor)));
        editor.getTransactionManager().insertTransaction(edit);
        if(endIndex > -1) {
            editor.getCaret().moveBy(endIndex - finalText.length());
        } else if(snippetVariableActive != -1 && thisSuggestionHasVariables) {
            prepareSnippetVariable();
        } else if(snippetEnd != null && snippetEnd.size() > 0) {
            editor.getCaret().setProfile(snippetEnd);
            snippetEnd.clear();
        }

        if(thisSuggestionHasVariables) {
            snippetProfilesLocked = false;
        }

        if(!dismiss) {
            this.forceLocked = false;
            lock();
        }

        if(dismiss) {
            setSafeToSuggest(false);
        }
    }

    private Status snippetVariableStatus = new Status();

    private void prepareSnippetVariable() {
        CaretProfile profile = snippetVariables.get(snippetVariableActive);
        editor.getCaret().setProfile(profile);
        snippetVariableStatus.setMessage("Now inserting: " + snippetVariableNames.get(snippetVariableActive));
        GuardianWindow.setStatus(snippetVariableStatus);
    }

    private void nextSnippetVariable() {
        snippetVariableActive++;
        if(snippetVariableActive >= snippetVariables.size()) {
            snippetVariableActive = -1;
            if(snippetEnd != null && snippetEnd.size() > 0) {
                editor.getCaret().setProfile(snippetEnd);
                snippetEnd.clear();
            }
            GuardianWindow.dismissStatus(snippetVariableStatus);
        } else {
            prepareSnippetVariable();
        }
        editor.repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        boolean editorNotFocused = GuardianWindow.jframe.getFocusOwner() == null;
        if(editorNotFocused) {
            editor.requestFocus();
            if(!e.isConsumed()) {
                editor.keyTyped(e);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(snippetVariableActive != -1 && (!this.isVisible() || !anyEnabled)) {
            if(KeyMap.SUGGESTION_SELECT.wasPerformedExact(e)) {
                e.consume();
                nextSnippetVariable();
                return;
            } else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                snippetVariableActive = -1;
                e.consume();
                editor.repaint();
                return;
            }
        }
        boolean editorNotFocused = GuardianWindow.jframe.getFocusOwner() == null;
        if(!this.isVisible() || !anyEnabled) {
            if(editorNotFocused) {
                editor.requestFocus();
                if(!e.isConsumed()) {
                    editor.keyPressed(e);
                }
            }
            return;
        }
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            this.setVisible(false);
            e.consume();
            return;
        }
        int selectedIndex = explorer.getFirstSelectedIndex();
        if(selectedIndex < 0) selectedIndex = 0;
        if(e.getKeyCode() == KeyEvent.VK_DOWN) {
            selectedIndex++;
            if(selectedIndex >= explorer.getTotalCount()) {
                selectedIndex = 0;
            }
            explorer.setSelectedIndex(selectedIndex);
            e.consume();
        } else if(e.getKeyCode() == KeyEvent.VK_UP) {
            selectedIndex--;
            if(selectedIndex < 0) {
                selectedIndex = explorer.getTotalCount()-1;
            }
            explorer.setSelectedIndex(selectedIndex);
            e.consume();
        } else if(KeyMap.SUGGESTION_SELECT.wasPerformedExact(e)) {
            java.util.List<ModuleToken> tokens = explorer.getSelectedTokens();
            if(!tokens.isEmpty()) {
                tokens.get(0).onInteract();
            }
            e.consume();
        }
        if(editorNotFocused) {
            editor.requestFocus();
            if(!e.isConsumed()) {
                editor.keyPressed(e);
            }
        }

        Rectangle rect = explorer.getVisibleRect(selectedIndex);
        rect.y -= scrollPane.getViewport().getViewRect().y;

        scrollPane.getViewport().scrollRectToVisible(rect);
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void dismiss(boolean force) {
        if(isVisible() && !forceLocked) {
            if (force || !locked || (activeResults != null && editor.getCaretWordPosition() != activeResults.getSuggestionIndex() && editor.getSoftCaretWordPosition() != activeResults.getSuggestionIndex())) {
                this.setVisible(false);
            }
        }
        locked = false;
    }

    public EditorComponent getEditor() {
        return editor;
    }

    private boolean anyEnabled = true;

    public void filter() {
        if(isVisible() && activeResults != null) {
            try {
                int cwpos = activeResults.getSuggestionIndex();
                if(editor.getCaretPosition() < cwpos || editor.getSoftCaretWordPosition() > cwpos || (!activeResults.changedSuggestionIndex() && editor.getCaretWordPosition() > cwpos)) {
                    dismiss(true);
                    return;
                }
                String typed = editor.getDocument().getText(cwpos, editor.getCaretPosition() - cwpos);

                anyEnabled = false;
                for(ExpandableSuggestionToken token : activeTokens) {
                    token.setEnabledFilter(typed);
                    if(token.isEnabled()) anyEnabled = true;
                }

                if(anyEnabled) this.explorer.setForceSelectNext(true);

                explorer.repaint();
                relocate();
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void resize() {
        int shownTokens = 0;
        for(ExpandableSuggestionToken token : activeTokens) {
            if(token.isEnabled()) shownTokens += 1;
        }
        this.setSize(new ScalableDimension(400, Math.min(300, explorer.getRowHeight() * shownTokens + 2 + parameterLabel.getPreferredSize().height)));
    }

    private void relocate(int index) {
        resize();
        try {
            Rectangle rect = editor.modelToView(index);
            if(rect == null) return;
            Point loc = rect.getLocation();
            loc.y += rect.height;
            loc.translate(editor.getLocationOnScreen().x, editor.getLocationOnScreen().y);
            if(loc.y + this.getHeight() >= GuardianWindow.jframe.getLocationOnScreen().y + GuardianWindow.jframe.getHeight()) {
                loc.y -= rect.height;
                loc.y -= this.getHeight();
            }
            this.setLocation(loc);
        } catch (BadLocationException x) {
            Debug.log("BadLocationException: " + x.getMessage() + "; index " + x.offsetRequested(), Debug.MessageType.ERROR);
            x.printStackTrace();
        } catch(IllegalComponentStateException ignored) {

        }
    }

    @Override
    public void relocate() {
        if(editor != null && this.isVisible() && this.isShowing()) {
            relocate(activeResults != null ? Math.min(activeResults.getSuggestionIndex(), editor.getDocument().getLength()) : editor.getCaretWordPosition());
        }
    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        if(e.getOppositeComponent() != null && e.getOppositeComponent() != this && e.getOppositeComponent() != explorer) {
            dismiss(true);
        }
    }

    public void setSummary(PrismarineSummaryModule summary, boolean matched) {
        this.summary = summary;
        if(lastSuccessfulSummary == null || matched) lastSuccessfulSummary = summary;
    }

    public PrismarineSummaryModule getSummary() {
        return summary;
    }

    public PrismarineSummaryModule getLastSuccessfulSummary() {
        return lastSuccessfulSummary;
    }

    @Override
    public void lock() {
        locked = true;
        filter();
    }

    @Override
    public void setSafeToSuggest(boolean safe) {
        //Debug.log("Set safe to suggest: " + safe);
        this.safe = safe;
    }

    private boolean disposed = false;

    @Override
    public void dispose() {
        if(!disposed) {
            super.dispose();
            tlm.dispose();
            explorer.dispose();
        }
        summary = null;
        lastSuccessfulSummary = null;
        scrollPane = null;
        activeTokens.clear();
        disposed = true;
    }

    private class SnippetVariableHighlighter implements Highlighter.HighlightPainter {

        private Color highlightColor;
        private Color highlightBorderColor;

        public SnippetVariableHighlighter() {
            tlm.addThemeChangeListener(t -> {
                highlightColor = t.getColor(t.getColor(Color.GREEN, "Editor.find.highlight", "Editor.snippet.variable.highlight"));
                highlightBorderColor = t.getColor(t.getColor(Color.YELLOW, "Editor.find.highlight.border", "Editor.snippet.variable.highlight.border"));
            });
        }

        @Override
        public void paint(Graphics g, int p0, int p1, Shape ignore, JTextComponent c) {
            if(snippetVariableActive == -1) return;

            CaretProfile activeProfile = snippetVariables.get(snippetVariableActive);

            for(int i = 0; i < activeProfile.size() - 1; i += 2) {
                int start = activeProfile.get(i);
                int end = activeProfile.get(i+1);

                try {
                    StringBounds bounds = new StringBounds(editor.getLocationForOffset(start), editor.getLocationForOffset(end));

                    for (int l = bounds.start.line; l <= bounds.end.line; l++) {
                        Rectangle rectangle;
                        if (l == bounds.start.line) {
                            rectangle = c.modelToView(bounds.start.index);
                            if (bounds.start.line == bounds.end.line) {
                                rectangle.width = c.modelToView(bounds.end.index).x - rectangle.x;
                            } else {
                                rectangle.width = c.getWidth() - rectangle.x;
                            }
                        } else if (l == bounds.end.line) {
                            rectangle = c.modelToView(bounds.end.index);
                            rectangle.width = rectangle.x - c.modelToView(0).x;
                            rectangle.x = c.modelToView(0).x; //0
                        } else {
                            rectangle = c.modelToView(bounds.start.index);
                            rectangle.x = c.modelToView(0).x; //0
                            rectangle.y += rectangle.height * (l - bounds.start.line);
                            rectangle.width = c.getWidth();
                        }

                        if(rectangle.width < 0) {
                            rectangle.x += rectangle.width;
                            rectangle.width *= -1;
                        }
                        rectangle.width = Math.abs(rectangle.width);

                        g.setColor(highlightColor);

                        g.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);

                        g.setColor(highlightBorderColor);

                        g.fillRect(rectangle.x, rectangle.y, 1, rectangle.height-1);
                        g.fillRect(rectangle.x, rectangle.y + rectangle.height - 1, rectangle.width-1, 1);
                        g.fillRect(rectangle.x + rectangle.width - 1, rectangle.y+1, 1, rectangle.height-1);
                        g.fillRect(rectangle.x+1, rectangle.y, rectangle.width-1, 1);

                    }
                } catch (BadLocationException e) {
                    //Can't render
                }
            }
        }
    }
}
