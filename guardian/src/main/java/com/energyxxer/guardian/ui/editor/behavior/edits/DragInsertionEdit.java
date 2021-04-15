package com.energyxxer.guardian.ui.editor.behavior.edits;

import com.energyxxer.guardian.ui.common.transactions.Transaction;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.guardian.ui.editor.behavior.caret.EditorCaret;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.util.ArrayList;

public class DragInsertionEdit extends Transaction<AdvancedEditor> {
    private String value;
    private final ArrayList<String> previousValues = new ArrayList<>();
    private CaretProfile previousProfile;

    public DragInsertionEdit(String value, AdvancedEditor editor) {
        this.value = value;
        this.previousProfile = editor.getCaret().getProfile();
    }

    @Override
    public boolean redo(AdvancedEditor target) {

        Document doc = target.getDocument();
        EditorCaret caret = target.getCaret();
        try {
            String result = doc.getText(0, doc.getLength()); //Result

            int characterDrift = 0;

            previousValues.clear();
            CaretProfile nextProfile = new CaretProfile();

            for (int i = 0; i < previousProfile.size() - 1; i += 2) {
                int start = previousProfile.get(i) + characterDrift;
                int end = previousProfile.get(i + 1) + characterDrift;
                if(end < start) {
                    int temp = start;
                    start = end;
                    end = temp;
                }
                previousValues.add(result.substring(start, end));
                result = result.substring(0, start) + value + result.substring(end);

                nextProfile.add(start,start+value.length());

                ((AbstractDocument) doc).replace(start, end - start, value, null);

                characterDrift += value.length() - (end - start);

                final int fstart = start;
                final int fend = end;
                final int flen = value.length();

                target.registerCharacterDrift(o -> (o >= fstart) ? ((o <= fend) ? fstart + flen : o + value.length() - (fend - fstart)): o);
            }
            caret.setProfile(nextProfile);

        } catch(BadLocationException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean undo(AdvancedEditor target) {

        Document doc = target.getDocument();
        EditorCaret caret = target.getCaret();
        try {
            String str = doc.getText(0, doc.getLength());

            for (int i = 0; i < previousProfile.size() - 1; i += 2) {
                int start = Math.min(previousProfile.get(i), previousProfile.get(i+1));
                int resultEnd = start + value.length();
                if(resultEnd < start) {
                    int temp = start;
                    start = resultEnd;
                    resultEnd = temp;
                }

                String previousValue = previousValues.get(i / 2);

                str = str.substring(0, start) + previousValue + str.substring(resultEnd);

                ((AbstractDocument) doc).replace(start, resultEnd - start, previousValue, null);

                final int fstart = start;
                final int flen = value.length();
                final int fplen = previousValue.length();

                target.registerCharacterDrift(o -> (o >= fstart) ? ((o <= fstart + flen) ? fstart + fplen : o + (fplen - flen)): o);
            }

            caret.setProfile(previousProfile);

        } catch(BadLocationException e) {
            e.printStackTrace();
        }
        return true;
    }
}
