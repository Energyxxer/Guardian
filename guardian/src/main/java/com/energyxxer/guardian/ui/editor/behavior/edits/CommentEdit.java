package com.energyxxer.guardian.ui.editor.behavior.edits;

import com.energyxxer.guardian.global.Status;
import com.energyxxer.guardian.global.temp.Lang;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.common.transactions.Transaction;
import com.energyxxer.guardian.ui.editor.EditorComponent;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.editor.behavior.CustomDocument;
import com.energyxxer.guardian.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.guardian.ui.editor.behavior.caret.Dot;
import com.energyxxer.guardian.ui.editor.behavior.caret.EditorCaret;

import javax.swing.text.BadLocationException;
import java.util.ArrayList;

public class CommentEdit extends Transaction<AdvancedEditor> {

    private CaretProfile previousProfile;
    /**
     * ArrayList containing information about how to undo the edit.
     * Must contain even entries.
     * Every index contains the position in the document where comment markers were added/removed.
     * */
    private final ArrayList<Integer> modifications = new ArrayList<>();
    private final String commentMarker;
    private boolean uncomment = false;

    public CommentEdit(AdvancedEditor editor) {
        previousProfile = editor.getCaret().getProfile();
        if(editor instanceof EditorComponent) {
            Lang language = ((EditorComponent) editor).getParentModule().getLanguage();
            if(language == null) {
                GuardianWindow.setStatus(new Status("Unknown language, don't know how to comment it out."));
                commentMarker = null;
            } else {
                commentMarker = language.getProperty("line_comment_marker");
                if(commentMarker == null) GuardianWindow.setStatus(new Status("Language '" + language + "' has no comments"));
            }
        } else {
            commentMarker = null;
        }
    }

    @Override
    public boolean redo(AdvancedEditor target) {
        if(commentMarker == null) return false;
        CustomDocument doc = target.getCustomDocument();
        EditorCaret caret = target.getCaret();

        try {
            String text = doc.getText(0, doc.getLength()); //Result
            this.modifications.clear();
            this.uncomment = true;


            CaretProfile nextProfile = new CaretProfile(previousProfile);

            //First, make a list of all line starts and decide whether to comment or uncomment.

            EditUtils.fetchSelectedLines(previousProfile, target, modifications, new EditUtils.Configuration() {
                {
                    fetchEnd = false;
                    lineHandler = (start, end, index) -> {
                        if(!text.startsWith(commentMarker, start)) uncomment = false;
                    };
                }
            });

            for(int i = 0; i < previousProfile.size()-1; i+= 2) {
                int selectionStart = previousProfile.get(i);
                int selectionEnd = previousProfile.get(i + 1);

                if(selectionStart == selectionEnd) {
                    int below = new Dot(selectionStart, target).getPositionBelow();
                    nextProfile.set(i>>1, below);
                    nextProfile.set((i>>1)+1, below);
                }
            }

            //List done, start adding/removing comment markers

            int characterDrift = 0;
            for(int lineStart : modifications) {
                if(uncomment) {
                    doc.removeTrusted(lineStart+characterDrift, commentMarker.length());
                    nextProfile.pushFrom(lineStart+characterDrift+commentMarker.length(), -commentMarker.length());
                    characterDrift -= commentMarker.length();
                } else {
                    doc.insertStringTrusted(lineStart+characterDrift, commentMarker, null);
                    nextProfile.pushFrom(lineStart+characterDrift, commentMarker.length());
                    characterDrift += commentMarker.length();
                }
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
            for(int lineStart : modifications) {
                if(!uncomment) {
                    doc.removeTrusted(lineStart, commentMarker.length());
                } else {
                    doc.insertStringTrusted(lineStart, commentMarker, null);
                }
            }
        } catch(BadLocationException x) {
            x.printStackTrace();
        }
        caret.setProfile(previousProfile);
        return true;
    }
}
