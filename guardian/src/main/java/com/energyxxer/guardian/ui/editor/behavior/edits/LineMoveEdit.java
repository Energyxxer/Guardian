package com.energyxxer.guardian.ui.editor.behavior.edits;

import com.energyxxer.guardian.ui.common.transactions.Transaction;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.guardian.ui.editor.behavior.caret.Dot;
import com.energyxxer.guardian.ui.editor.behavior.caret.EditorCaret;
import com.energyxxer.util.logger.Debug;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Created by User on 1/26/2017.
 */
public class LineMoveEdit extends Transaction<AdvancedEditor> {
    private CaretProfile previousProfile;
    private String previousText;
    private final int dir;

    public LineMoveEdit(AdvancedEditor editor, int dir) {
        previousProfile = editor.getCaret().getProfile();
        if(dir < 2 || dir > 3) throw new IllegalArgumentException("Invalid direction '" + dir + "'");
        this.dir = dir;
    }

    @Override
    public boolean redo(AdvancedEditor target) {
        Document doc = target.getDocument();
        EditorCaret caret = target.getCaret();

        boolean actionPerformed = false;

        try {
            String text = doc.getText(0, doc.getLength()); //Result
            this.previousText = text;

            int characterDrift = 0;

            CaretProfile nextProfile = new CaretProfile();

            for (int i = (dir == Dot.UP) ? 0 : previousProfile.size()-2; (dir == Dot.UP) ? (i < previousProfile.size() - 1) : (i >= 0); i += (dir == Dot.UP) ? 2 : -2) {
                //Get bounds of the line to move
                int selectionStart = previousProfile.get(i) + characterDrift;
                int selectionEnd = previousProfile.get(i + 1) + characterDrift;

                int start = new Dot(Math.min(selectionStart,selectionEnd), target).getRowStart();
                int end = new Dot(Math.max(selectionStart,selectionEnd), target).getRowEnd()+1;

                //Check if invalid
                if((start == 0 && dir == Dot.UP) || (end == text.length()+1 && dir == Dot.DOWN)) {
                    nextProfile.add(new Dot(selectionStart,selectionEnd, target));
                    continue;
                }

                //Get start of the line to move to

                int shiftTo;
                if(dir == Dot.UP) {
                    shiftTo = new Dot(start, target).getPositionAbove();
                } else {
                    shiftTo = new Dot(end, target).getRowEnd()+1 - (end-start);
                }

                //Remove lines

                String value = text.substring(start,end-1) + "\n";

                if (end < text.length()) {
                    doc.remove(start, (end - start));
                } else {
                    doc.remove(start - 1, (end - start));
                }

                //Add value back

                if(shiftTo > doc.getLength()) {
                    value = "\n" + value.substring(0, value.length()-1);
                    doc.insertString(doc.getLength(),value,null);
                } else {
                    doc.insertString(shiftTo,value,null);
                }
                actionPerformed = true;

                //Add new dot to profile

                nextProfile.add(new Dot(selectionStart + (shiftTo - start),selectionEnd + (shiftTo - start), target));
            }

            caret.setProfile(nextProfile);

        } catch(BadLocationException x) {
            Debug.log("Offset requested: " + x.offsetRequested());
            x.printStackTrace();
        }
        return actionPerformed;
    }

    @Override
    public boolean undo(AdvancedEditor target) {
        Document doc = target.getDocument();
        EditorCaret caret = target.getCaret();

        //Too complicated, just put back the text from before.

        try {
            ((AbstractDocument) doc).replace(0, doc.getLength(), this.previousText, null);
        } catch (BadLocationException x) {
            x.printStackTrace();
        }
        caret.setProfile(previousProfile);
        return true;
    }
}
