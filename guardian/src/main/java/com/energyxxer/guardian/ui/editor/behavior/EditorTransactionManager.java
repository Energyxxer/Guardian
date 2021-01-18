package com.energyxxer.guardian.ui.editor.behavior;

import com.energyxxer.guardian.ui.common.transactions.Transaction;
import com.energyxxer.guardian.ui.common.transactions.TransactionManager;
import com.energyxxer.guardian.ui.editor.behavior.caret.CaretProfile;

public class EditorTransactionManager extends TransactionManager<AdvancedEditor> {
    private CaretProfile lastProfile = null;

    public EditorTransactionManager(AdvancedEditor target) {
        super(target);
    }

    @Override
    protected boolean canUndo() {
        boolean can = target.getCaret().getProfile().equals(lastProfile);
        if(!can) {
            target.getCaret().setProfile(lastProfile);
        }
        return can;
    }

    @Override
    protected boolean canRedo() {
        boolean can = target.getCaret().getProfile().equals(lastProfile);
        if(!can) {
            target.getCaret().setProfile(lastProfile);
        }
        return can;
    }

    @Override
    protected void undone(Transaction<AdvancedEditor> transaction) {
        lastProfile = target.getCaret().getProfile();
        super.undone(transaction);
    }

    @Override
    protected void redone(Transaction<AdvancedEditor> transaction) {
        lastProfile = target.getCaret().getProfile();
        super.redone(transaction);
    }

    @Override
    protected void inserted(Transaction<AdvancedEditor> transaction) {
        lastProfile = target.getCaret().getProfile();
        super.inserted(transaction);
    }
}
