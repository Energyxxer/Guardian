package com.energyxxer.guardian.ui.editor.behavior.edits;

import com.energyxxer.guardian.ui.common.transactions.Transaction;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.editor.behavior.caret.CaretProfile;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import java.util.function.Function;

public class DocumentEdit extends Transaction<AdvancedEditor> {

    private int offs;
    private int len;
    private String text;
    private AttributeSet attrs;

    private String prevText;

    private CaretProfile previousProfile;

    public DocumentEdit(int offs, int len, String text, AttributeSet attrs, AdvancedEditor editor) {
        this.offs = offs;
        this.len = len;
        this.text = text;
        this.attrs = attrs;

        this.previousProfile = editor.getCaret().getProfile();
    }

    @Override
    public boolean redo(AdvancedEditor target) {
        try {
            prevText = target.getCustomDocument().getText(offs, len);
            CaretProfile nextProfile = new CaretProfile(previousProfile);

            target.getCustomDocument().replaceTrusted(offs, len, text, attrs);

            int tlen = text != null ? text.length() : 0;

            Function<Integer, Integer> driftFunction = o -> (o >= offs) ? ((o <= (offs + len)) ? offs + tlen : o + tlen - (len)) : o;
            target.registerCharacterDrift(driftFunction);
            nextProfile.apply(driftFunction);
            target.getCaret().setProfile(nextProfile);

            return !(len == 0 && (text == null || text.length() == 0));
        } catch (BadLocationException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean undo(AdvancedEditor target) {
        try {
            int undoOffs = offs;
            int undoLen = text != null ? text.length() : 0;
            String undoText = prevText;

            int tlen = undoText != null ? undoText.length() : 0;

            target.getCustomDocument().replaceTrusted(undoOffs, undoLen, undoText, attrs);

            target.registerCharacterDrift(o -> (o >= undoOffs) ? ((o <= (undoOffs + undoLen)) ? undoOffs + tlen : o + tlen - (undoLen)) : o);

            target.getCaret().setProfile(previousProfile);

            return !(undoLen == 0 && (undoText == null || undoText.length() == 0));
        } catch (BadLocationException e) {
            e.printStackTrace();
            return false;
        }
    }
}
