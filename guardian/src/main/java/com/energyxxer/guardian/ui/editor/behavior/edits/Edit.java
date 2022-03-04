package com.energyxxer.guardian.ui.editor.behavior.edits;

import com.energyxxer.guardian.ui.common.transactions.Transaction;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.editor.behavior.CustomDocument;
import com.energyxxer.guardian.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.guardian.ui.editor.behavior.caret.EditorCaret;

import javax.swing.text.BadLocationException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Function;

public abstract class Edit extends Transaction<AdvancedEditor> {
    protected CaretProfile previousProfile;
    private CaretProfile nextProfile;
    private final ArrayList<AtomicEdit> edits = new ArrayList<>();

    private boolean done = false;

    public Edit(AdvancedEditor editor) {
        this.previousProfile = editor.getCaret().getProfile();
        this.nextProfile = new CaretProfile();
    }

    protected abstract boolean doEdit(AdvancedEditor target) throws BadLocationException;

    protected void insert(CustomDocument doc, int start, String nextValue) throws BadLocationException {
        replace(doc, start, 0, nextValue);
    }

    protected void remove(CustomDocument doc, int start, int length) throws BadLocationException {
        replace(doc, start, length, "");
    }

    protected void replace(CustomDocument doc, int start, int length, String nextValue) throws BadLocationException {
        String previousValue = doc.getText(start, length);
        edits.add(new AtomicEdit(start, previousValue, nextValue));
    }

    public void putCaret(int dot) {
        putCaret(dot, dot);
    }

    public void putCaret(int dot, int mark) {
        nextProfile.add(dot, mark);
    }

    @Override
    public final boolean redo(AdvancedEditor target) {
        CustomDocument doc = target.getCustomDocument();
        EditorCaret caret = target.getCaret();

        if(!done) {
            done = true;
            try {
                if(!doEdit(target)) return false;
            } catch (BadLocationException e) {
                e.printStackTrace();
                return false;
            }
            for(int i = 0; i < edits.size(); i++) {
                Function<Integer, Integer> drift = edits.get(i).getDrift();
                for(int j = 0; j < nextProfile.size(); j += 2) {
                    if(i*2 != j) {
                        nextProfile.set(j, drift.apply(nextProfile.get(j)));
                        nextProfile.set(j+1, drift.apply(nextProfile.get(j+1)));
                    }
                }
            }
            edits.sort(Comparator.comparingInt(e -> e.index));
        }

        try {
            for(int i = edits.size()-1; i >= 0; i--) {
                AtomicEdit edit = edits.get(i);
                target.registerCharacterDrift(edit.redo(doc));
            }
        } catch(BadLocationException x) {
            x.printStackTrace();
        }

        caret.setProfile(nextProfile);
        return true;
    }

    @Override
    public final boolean undo(AdvancedEditor target) {
        CustomDocument doc = target.getCustomDocument();
        EditorCaret caret = target.getCaret();

        try {
            for(int i = 0; i < edits.size(); i++) {
                AtomicEdit edit = edits.get(i);
                target.registerCharacterDrift(edit.undo(doc));
            }
        } catch(BadLocationException x) {
            x.printStackTrace();
        }

        caret.setProfile(previousProfile);
        return true;
    }

    private static class AtomicEdit {
        public final int index;
        public final String previousValue;
        public final String nextValue;
        public final int previousLength;
        public final int nextLength;

        public AtomicEdit(int index, String previousValue, String nextValue) {
            this.index = index;
            this.previousValue = previousValue;
            this.nextValue = nextValue;

            this.previousLength = previousValue.length();
            this.nextLength = nextValue.length();
        }

        public Function<Integer, Integer> getDrift() {
            return o -> (o >= index) ? ((o <= index + previousLength) ? index + nextLength : o - previousLength + nextLength) : o;
        }

        public Function<Integer, Integer> redo(CustomDocument doc) throws BadLocationException {
            doc.removeTrusted(index, previousLength);
            doc.insertStringTrusted(index, nextValue, null);

            return o -> (o >= index) ? ((o <= index + previousLength) ? index + nextLength : o - previousLength + nextLength) : o;
        }

        public Function<Integer, Integer> undo(CustomDocument doc) throws BadLocationException {
            doc.removeTrusted(index, nextLength);
            doc.insertStringTrusted(index, previousValue, null);

            return o -> (o >= index) ? ((o <= index + nextLength) ? index + previousLength : o - nextLength + previousLength) : o;
        }
    }
}
