package com.energyxxer.guardian.ui.editor.behavior.edits;

import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.editor.behavior.CustomDocument;
import com.energyxxer.guardian.ui.editor.behavior.caret.Dot;
import com.energyxxer.guardian.ui.editor.completion.SuggestionDialog;
import com.energyxxer.util.StringUtil;

import javax.swing.text.BadLocationException;

/**
 * Created by User on 1/10/2017.
 */
public class InsertionEdit extends Edit {
    private String value;

    public InsertionEdit(String value, AdvancedEditor editor) {
        super(editor);
        this.value = value;
    }

    @Override
    protected boolean doEdit(AdvancedEditor target) throws BadLocationException {
        CustomDocument doc = target.getCustomDocument();
        String text = doc.getText(0, doc.getLength());
        for(int i = 0; i < previousProfile.size() - 1; i += 2) {
            int start = previousProfile.get(i);
            int end = previousProfile.get(i + 1);
            if (end < start) {
                int temp = start;
                start = end;
                end = temp;
            }

            String valueToWrite = value;
            if(Dot.SMART_KEYS_INDENT.get() && valueToWrite.length() == 1 && target.getIndentationManager().isClosingBrace(valueToWrite) && start <= new Dot(start, start, target).getRowContentStart()) {
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
                        char charToWrite = valueToWrite.charAt(0);
                        Character charToComplete = ((SuggestionDialog) target.getSuggestionInterface()).getLatestResults().getPairCompletionAtIndex(charToWrite, previousProfile.get(i));
                        if(charToComplete != null) {
                            valueToWrite += charToComplete;
                            caretOffset--;
                            pairCompleted = true;
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
                    ) && text.startsWith(valueToWrite,start) ) {
                valueToWrite = "";
                caretOffset++;
            }

            putCaret(start + valueToWrite.length()+caretOffset, start + valueToWrite.length()+caretOffset);
            replace(doc, start, end-start, valueToWrite);
        }
        return true;
    }
}
