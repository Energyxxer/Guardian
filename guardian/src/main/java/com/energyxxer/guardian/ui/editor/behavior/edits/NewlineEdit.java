package com.energyxxer.guardian.ui.editor.behavior.edits;

import com.energyxxer.guardian.ui.common.transactions.Transaction;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.editor.behavior.CustomDocument;
import com.energyxxer.guardian.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.guardian.ui.editor.behavior.caret.Dot;
import com.energyxxer.guardian.ui.editor.behavior.caret.EditorCaret;
import com.energyxxer.util.StringUtil;

import javax.swing.text.BadLocationException;
import java.util.ArrayList;

/**
 * Created by User on 3/20/2017.
 */
public class NewlineEdit extends Transaction<AdvancedEditor> {
    private CaretProfile previousProfile;
    private CaretProfile nextProfile;
    private final ArrayList<Integer> modificationIndices = new ArrayList<>();
    private final ArrayList<String> previousValues = new ArrayList<>();
    private final ArrayList<String> nextValues = new ArrayList<>();

    private final boolean pushCaret;

    public NewlineEdit(AdvancedEditor editor) {
        this(editor, true);
    }

    public NewlineEdit(AdvancedEditor editor, boolean pushCaret) {
        this.previousProfile = editor.getCaret().getProfile();
        this.pushCaret = pushCaret;
    }

    @Override
    public boolean redo(AdvancedEditor target) {
        CustomDocument doc = target.getCustomDocument();
        EditorCaret caret = target.getCaret();

        modificationIndices.clear();
        previousValues.clear();
        nextValues.clear();

        boolean actionPerformed = false;

        nextProfile = (pushCaret) ? new CaretProfile() : new CaretProfile(previousProfile);

        try {
            String text = doc.getText(0, doc.getLength()); //Result

            int characterDrift = 0;

            for(int i = 0; i < previousProfile.size() - 1; i += 2) {
                int start = Math.min(previousProfile.get(i), previousProfile.get(i+1)) + characterDrift;
                int end = Math.max(previousProfile.get(i), previousProfile.get(i+1)) + characterDrift;

                String str = "\n";



                String afterCaret = text.substring(start - characterDrift);
                char beforeCaretChar = '\0';
                for(int j = start - characterDrift - 1; j >= 0; j--) {
                    char c = text.charAt(j);
                    if(!Character.isWhitespace(c)) {
                        beforeCaretChar = c;
                        break;
                    }
                }

                char afterCaretChar = '\0';
                for(int j = start - characterDrift; j < text.length(); j++) {
                    char c = text.charAt(j);
                    if(!Character.isWhitespace(c) || c == '\n') {
                        afterCaretChar = c;
                        break;
                    }
                }
                String placeAfterCaret = "";
                int tabs = target.getIndentationLevelAt(start);

                if(Dot.SMART_KEYS_INDENT.get() && !afterCaret.isEmpty()) {
                    if(target.getIndentationManager().match(beforeCaretChar, afterCaretChar)) {
                        placeAfterCaret = '\n' + StringUtil.repeat(" ", target.tabSize * Math.max(tabs-1, 0));
                    } else if(target.getIndentationManager().isClosingBrace(afterCaret.charAt(0))) {
                        tabs--;
                    }
                } else {
                    tabs = 0;
                }

                //Debug.log(beforeCaretChar + "|" + afterCaretChar);

                str += StringUtil.repeat(" ", target.tabSize * tabs);
                str += placeAfterCaret;

                modificationIndices.add(start);
                previousValues.add(text.substring(start - characterDrift, end - characterDrift));
                nextValues.add(str);

                doc.removeTrusted(start, end-start);
                doc.insertStringTrusted(start, str, null);
                actionPerformed = true;

                if(pushCaret) {
                    int dot = start + str.length() - placeAfterCaret.length();
                    nextProfile.add(dot, dot);
                } else nextProfile.pushFrom(start+1, str.length());
                characterDrift += (end - start) + (tabs * 4) + 1 + placeAfterCaret.length();

                int ftabs = tabs;

                target.registerCharacterDrift(o -> (o >= start) ? ((o <= end) ? start + (ftabs * 4) + 1 : o + (ftabs * 4) + 1 - (end - start)) : o);
            }
        } catch(BadLocationException x) {
            x.printStackTrace();
        }

        caret.setProfile(nextProfile);
        return actionPerformed;
    }

    @Override
    public boolean undo(AdvancedEditor target) {
        CustomDocument doc = target.getCustomDocument();
        EditorCaret caret = target.getCaret();

        try {
            for(int i = modificationIndices.size()-1; i >= 0; i--) {
                int start = modificationIndices.get(i);
                doc.removeTrusted(start, nextValues.get(i).length());
                doc.insertStringTrusted(start, previousValues.get(i), null);

                final int fnlen = nextValues.get(i).length();
                final int fplen = previousValues.get(i).length();

                target.registerCharacterDrift(o -> (o >= start) ? ((o <= start + fnlen) ? start + fplen : o - fnlen + fplen) : o);
            }
        } catch(BadLocationException x) {
            x.printStackTrace();
        }

        caret.setProfile(previousProfile);
        return true;
    }

    @Override
    public boolean canMerge(Transaction<AdvancedEditor> other) {
        return false;
    }
}
