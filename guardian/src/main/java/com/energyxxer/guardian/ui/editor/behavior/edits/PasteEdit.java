package com.energyxxer.guardian.ui.editor.behavior.edits;

import com.energyxxer.guardian.ui.common.transactions.Transaction;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.guardian.ui.editor.behavior.caret.EditorCaret;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.util.ArrayList;

public class PasteEdit extends Transaction<AdvancedEditor> {
    private String[] values;
    private final ArrayList<String> previousValues = new ArrayList<>();
    private CaretProfile previousProfile;

    public PasteEdit(String value, AdvancedEditor editor) {
        this.values = new String[] {value};
        this.previousProfile = editor.getCaret().getProfile();
        String[] newValues = new String[previousProfile.size()];
        for(int i = 0; i < newValues.length; i++) {
            newValues[i] = value;
        }
        this.values = newValues;
    }

    public PasteEdit(String[] values, AdvancedEditor editor) {
        this.values = values;
        this.previousProfile = editor.getCaret().getProfile();

        if(this.previousProfile.size()/2 <= 1) {
            StringBuilder sb = new StringBuilder();
            for(String str : this.values) {
                sb.append(str);
            }
            this.values = new String[] {sb.toString()};
        } else if(this.values.length == 1) {
            String[] newValues = new String[previousProfile.size()];
            for(int i = 0; i < newValues.length; i++) {
                newValues[i] = this.values[0];
            }
            this.values = newValues;
        }
    }

    @Override
    public boolean redo(AdvancedEditor target) {

        Document doc = target.getDocument();
        EditorCaret caret = target.getCaret();
        try {
            int characterDrift = 0;

            previousValues.clear();
            CaretProfile nextProfile = new CaretProfile();

            int i = 0;
            for(String value : values) {
                if(i >= previousProfile.size()-1) break;
                int start = previousProfile.get(i) + characterDrift;
                int end = previousProfile.get(i + 1) + characterDrift;
                if(end < start) {
                    int temp = start;
                    start = end;
                    end = temp;
                }
                previousValues.add(doc.getText(start, end-start));

                nextProfile.add(start+value.length(),start+value.length());

                ((AbstractDocument) doc).replace(start, end - start, value, null);

                characterDrift += value.length() - (end - start);

                final int fstart = start;
                final int fend = end;
                final int flen = value.length();

                target.registerCharacterDrift(o -> (o >= fstart) ? ((o <= fend) ? fstart + flen : o + value.length() - (fend - fstart)): o);

                i += 2;
            }
            for(; i < previousProfile.size()-1; i++) {
                int start = previousProfile.get(i) + characterDrift;
                int end = previousProfile.get(i + 1) + characterDrift;
                nextProfile.add(start, end);
            }
            caret.setProfile(nextProfile);
        } catch(BadLocationException x) {
            x.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean undo(AdvancedEditor target) {

        Document doc = target.getDocument();
        EditorCaret caret = target.getCaret();
        try {

            int i = 0;
            for(String value : values) {
                if(i >= previousProfile.size()-1) break;
                int start = previousProfile.get(i);
                int end = previousProfile.get(i + 1);
                if(end < start) {
                    start = end;
                    //No further use of end
                }

                ((AbstractDocument) doc).replace(start, value.length(), previousValues.get(i/2), null);

                final int fstart = start;
                final int fplen = previousValues.get(i/2).length();
                final int flen = value.length();

                target.registerCharacterDrift(o -> (o >= fstart) ? ((o <= fstart + flen) ? fstart + fplen : o + (fplen - flen)): o);

                i += 2;
            }

            caret.setProfile(previousProfile);
        } catch(BadLocationException x) {
            x.printStackTrace();
            return false;
        }
        return true;
    }
}
