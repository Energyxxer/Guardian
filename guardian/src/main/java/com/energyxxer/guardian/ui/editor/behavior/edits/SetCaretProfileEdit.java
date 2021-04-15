package com.energyxxer.guardian.ui.editor.behavior.edits;

import com.energyxxer.guardian.ui.common.transactions.Transaction;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.editor.behavior.caret.CaretProfile;

public class SetCaretProfileEdit extends Transaction<AdvancedEditor> {
    private CaretProfile nextProfile;
    private CaretProfile previousProfile;

    public SetCaretProfileEdit(CaretProfile nextProfile, AdvancedEditor editor) {
        this.nextProfile = nextProfile;
        this.previousProfile = editor.getCaret().getProfile();
    }

    @Override
    public boolean redo(AdvancedEditor target) {
        target.getCaret().setProfile(nextProfile);
        return true;
    }

    @Override
    public boolean undo(AdvancedEditor target) {
        target.getCaret().setProfile(previousProfile);
        return true;
    }
}
