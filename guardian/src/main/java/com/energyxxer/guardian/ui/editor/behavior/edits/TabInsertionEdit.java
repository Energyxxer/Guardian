package com.energyxxer.guardian.ui.editor.behavior.edits;

import com.energyxxer.guardian.ui.common.transactions.Transaction;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.guardian.ui.editor.behavior.caret.EditorCaret;
import com.energyxxer.util.StringUtil;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.util.ArrayList;

/**
 * Created by User on 3/20/2017.
 */
public class TabInsertionEdit extends Transaction<AdvancedEditor> {
    private CaretProfile previousProfile;
    private CaretProfile nextProfile;
    private final ArrayList<Integer> spacesAdded = new ArrayList<>();

    public TabInsertionEdit(AdvancedEditor editor) {
        this.previousProfile = editor.getCaret().getProfile();
    }

    @Override
    public boolean redo(AdvancedEditor target) {
        Document doc = target.getDocument();
        EditorCaret caret = target.getCaret();

        boolean actionPerformed = false;

        try {
            String text = doc.getText(0, doc.getLength()); //Result
            spacesAdded.clear();
            int characterDrift = 0;

            nextProfile = new CaretProfile(previousProfile);

            for (int i = 0; i < previousProfile.size()-1; i += 2) {
                int columns = 0;

                int selectionStart = previousProfile.get(i) + characterDrift;
                //Trust that marks will be equal to their dots.

                for(int j = selectionStart-characterDrift-1; j >= 0; j--) {
                    if(text.charAt(j) != '\n') columns++;
                    else break;
                }

                int spacesToAdd = 4 - (columns % 4);
                spacesToAdd = (spacesToAdd > 0) ? spacesToAdd : 4;

                String str = StringUtil.repeat(" ", spacesToAdd);
                doc.insertString(selectionStart, str, null);
                spacesAdded.add(spacesToAdd);
                nextProfile.pushFrom(selectionStart, spacesToAdd);
                actionPerformed = true;

                characterDrift += spacesToAdd;

                int fsta = spacesToAdd;

                target.registerCharacterDrift(o -> (o >= selectionStart) ? o + fsta : o);
            }
        } catch(BadLocationException x) {
            x.printStackTrace();
        }
        caret.setProfile(nextProfile);
        return actionPerformed;
    }

    @Override
    public boolean undo(AdvancedEditor target) {
        Document doc = target.getDocument();
        EditorCaret caret = target.getCaret();

        try {
            for(int i = 0; i < previousProfile.size()-1; i += 2) {
                int selectionStart = previousProfile.get(i);
                int spacesToRemove = spacesAdded.get(i/2);

                doc.remove(selectionStart, spacesToRemove);

                target.registerCharacterDrift(o -> (o >= selectionStart) ? o - spacesToRemove : o);
            }
        } catch(BadLocationException x) {
            x.printStackTrace();
        }

        caret.setProfile(previousProfile);
        return true;
    }
}
