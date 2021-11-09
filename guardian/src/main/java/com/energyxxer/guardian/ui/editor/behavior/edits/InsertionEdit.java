package com.energyxxer.guardian.ui.editor.behavior.edits;

import com.energyxxer.enxlex.suggestions.PairSuggestion;
import com.energyxxer.enxlex.suggestions.Suggestion;
import com.energyxxer.guardian.ui.common.transactions.Transaction;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.editor.behavior.CustomDocument;
import com.energyxxer.guardian.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.guardian.ui.editor.behavior.caret.Dot;
import com.energyxxer.guardian.ui.editor.behavior.caret.EditorCaret;
import com.energyxxer.guardian.ui.editor.completion.SuggestionDialog;
import com.energyxxer.util.StringUtil;

import javax.swing.text.BadLocationException;
import java.util.ArrayList;

/**
 * Created by User on 1/10/2017.
 */
public class InsertionEdit extends Transaction<AdvancedEditor> {
    private String value;
    private final ArrayList<Integer> undoIndices = new ArrayList<>();
    private final ArrayList<String> previousValues = new ArrayList<>();
    private final ArrayList<String> writingValues = new ArrayList<>();
    private CaretProfile previousProfile;

    public InsertionEdit(String value, AdvancedEditor editor) {
        this.value = value;
        this.previousProfile = editor.getCaret().getProfile();
    }

    @Override
    public boolean redo(AdvancedEditor target) {

        CustomDocument doc = target.getCustomDocument();
        EditorCaret caret = target.getCaret();
        try {
            String result = doc.getText(0, doc.getLength()); //Result

            int characterDrift = 0;

            previousValues.clear();
            undoIndices.clear();
            writingValues.clear();
            CaretProfile nextProfile = new CaretProfile();

            for (int i = 0; i < previousProfile.size() - 1; i += 2) {
                int start = previousProfile.get(i) + characterDrift;
                int end = previousProfile.get(i + 1) + characterDrift;
                if (end < start) {
                    int temp = start;
                    start = end;
                    end = temp;
                }

                String valueToWrite = value;
                if (Dot.SMART_KEYS_INDENT.get() && valueToWrite.length() == 1 && target.getIndentationManager().isClosingBrace(valueToWrite) && start <= new Dot(start, start, target).getRowContentStart()) {
                    int rowStart = new Dot(start, start, target).getRowStart();
                    int whitespace = start - rowStart;
                    int properWhitespace = 4 * Math.max(target.getIndentationLevelAt(start) - 1, 0);
                    int diff = properWhitespace - whitespace;
                    start += diff;
                    if(end < start) {
                        valueToWrite = StringUtil.repeat(" ", start - end) + valueToWrite;
                        start = end;
                    }
                }
                int caretOffset = 0;
                boolean pairCompleted = false;
                if(target.getIndentationManager().isPairCompletionSyntaxDriven()) {
                    if(valueToWrite.length() == 1
                            && (Dot.SMART_KEYS_BRACES.get() && target.getIndentationManager().isOpeningBrace(valueToWrite) || Dot.SMART_KEYS_QUOTES.get() && "\"'".contains(valueToWrite))) {
                        if(target.getSuggestionInterface() != null && ((SuggestionDialog) target.getSuggestionInterface()).getLatestResults() != null) {
                            for(Suggestion suggestion : ((SuggestionDialog) target.getSuggestionInterface()).getLatestResults().getSuggestions()) {
                                if(
                                        suggestion instanceof PairSuggestion
                                                && ((PairSuggestion) suggestion).getStartIndex() <= previousProfile.get(i)
                                                && previousProfile.get(i) <= ((PairSuggestion) suggestion).getEndIndex()
                                                && ((PairSuggestion) suggestion).getOpenSymbol().equals(valueToWrite)
                                ) {
                                    valueToWrite += ((PairSuggestion) suggestion).getCloseSymbol();
                                    caretOffset--;
                                    pairCompleted = true;
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    if(valueToWrite.length() == 1 && Dot.SMART_KEYS_BRACES.get() && target.getIndentationManager().isOpeningBrace(valueToWrite) && target.getIndentationManager().isBalanced()) {
                        valueToWrite += target.getIndentationManager().getMatchingBraceChar(valueToWrite);
                        caretOffset--;
                        pairCompleted = true;
                    }
                }
                if(!pairCompleted && valueToWrite.length() == 1 &&
                        ((
                                Dot.SMART_KEYS_BRACES.get() &&
                                target.getIndentationManager().isClosingBrace(valueToWrite) &&
                                target.getIndentationManager().isBalanced()
                        ) ||
                        (
                                Dot.SMART_KEYS_QUOTES.get() &&
                                "\"'".contains(valueToWrite) &&
                                !target.getStyledDocument().getCharacterElement(start).getAttributes().containsAttributes(target.getStyle(AdvancedEditor.STRING_ESCAPE_STYLE))
                        )
                        ) && result.startsWith(valueToWrite,start) ) {
                    valueToWrite = "";
                    caretOffset++;
                }

                undoIndices.add(start);
                previousValues.add(result.substring(start, end));
                writingValues.add(valueToWrite);
                result = result.substring(0, start) + valueToWrite + result.substring(end);

                nextProfile.add(start + valueToWrite.length()+caretOffset, start + valueToWrite.length()+caretOffset);

                doc.replaceTrusted(start, end - start, valueToWrite, null);

                characterDrift += valueToWrite.length() - (end - start);

                final int fstart = start;
                final int fend = end;
                final int flen = valueToWrite.length();

                target.registerCharacterDrift(o -> (o >= fstart) ? ((o <= fend) ? fstart + flen : o + flen - (fend - fstart)) : o);
            }
            caret.setProfile(nextProfile);

        } catch(BadLocationException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean undo(AdvancedEditor target) {

        CustomDocument doc = target.getCustomDocument();
        EditorCaret caret = target.getCaret();
        try {
            int characterDrift = 0;
            for (int i = 0; i < undoIndices.size(); i++) {
                int start = undoIndices.get(i) + characterDrift;
                int resultEnd = start + writingValues.get(i).length();

                String previousValue = previousValues.get(i);

                doc.replaceTrusted(start, resultEnd - start, previousValue, null);

                final int fstart = start;
                final int flen = resultEnd - start;
                final int fplen = previousValue.length();
                characterDrift += fplen-flen;

                target.registerCharacterDrift(o -> (o >= fstart) ? ((o <= fstart + flen) ? fstart + fplen : o + (fplen - flen)): o);
            }

            caret.setProfile(previousProfile);

        } catch(BadLocationException e) {
            e.printStackTrace();
        }
        return true;
    }
}
