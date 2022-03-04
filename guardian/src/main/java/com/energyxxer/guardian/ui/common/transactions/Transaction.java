package com.energyxxer.guardian.ui.common.transactions;

import java.util.Date;

/**
 * Created by User on 1/10/2017.
 */
public abstract class Transaction<T> {
    public final long time = new Date().getTime();

    public abstract boolean redo(T target);
    public abstract boolean undo(T target);

    public boolean canMerge(Transaction<T> other) {
        return true;
    }

    public boolean redoOnInsert(T target) {
        return redo(target);
    }
}
