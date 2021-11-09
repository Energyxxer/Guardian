package com.energyxxer.guardian.ui.editor.behavior.edits;

import com.energyxxer.guardian.ui.common.transactions.Transaction;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.editor.behavior.CustomDocument;
import com.energyxxer.guardian.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.guardian.ui.editor.behavior.caret.EditorCaret;

import javax.swing.text.BadLocationException;
import java.util.ArrayList;

public class LineDuplicationEdit extends Transaction<AdvancedEditor> {

    private CaretProfile previousProfile;
    private final ArrayList<Integer> copiedLines = new ArrayList<>();

    public LineDuplicationEdit(AdvancedEditor editor) {
        previousProfile = editor.getCaret().getProfile();
    }

    @Override
    public boolean redo(AdvancedEditor target) {
        CustomDocument doc = target.getCustomDocument();
        EditorCaret caret = target.getCaret();

        try {
            String text = doc.getText(0, doc.getLength()); //Result
            this.copiedLines.clear();

            CaretProfile nextProfile = new CaretProfile(previousProfile);

            EditUtils.fetchSelectedLines(previousProfile, target, copiedLines, new EditUtils.Configuration() {
                int carriedDrift = 0;
                {
                    includeNewline = true;
                    includeLastLine = true;
                    mergeLinesPerSelection = true;
                    lineHandler = ((start, end, selectionIndex) -> {
                        nextProfile.set(selectionIndex*2, nextProfile.get(selectionIndex*2) + carriedDrift + (end-start));
                        nextProfile.set(selectionIndex*2+1, nextProfile.get(selectionIndex*2+1) + carriedDrift + (end-start));
                        carriedDrift += end-start;
                        //Move down
                    });
                }
            });

            int characterDrift = 0;
            for(int i = 0; i < copiedLines.size(); i+=2) {
                int lineStart = copiedLines.get(i);
                int lineEnd = copiedLines.get(i+1);
                String toCopy = text.substring(lineStart, lineEnd);

                doc.insertStringTrusted(lineEnd+characterDrift, toCopy, null);
                characterDrift += lineEnd-lineStart;
            }

            caret.setProfile(nextProfile);
        } catch(BadLocationException x) {
            x.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean undo(AdvancedEditor target) {
        CustomDocument doc = target.getCustomDocument();
        EditorCaret caret = target.getCaret();

        try {
            for(int i = 0; i < copiedLines.size(); i+=2) {
                doc.removeTrusted(copiedLines.get(i+1), copiedLines.get(i+1)-copiedLines.get(i));
            }
        } catch(BadLocationException x) {
            x.printStackTrace();
        }
        caret.setProfile(previousProfile);
        return true;
    }
}
