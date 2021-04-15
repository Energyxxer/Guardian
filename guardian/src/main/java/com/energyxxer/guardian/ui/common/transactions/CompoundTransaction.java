package com.energyxxer.guardian.ui.common.transactions;

import com.energyxxer.util.Factory;
import com.energyxxer.util.Lazy;

import java.util.ArrayList;

/**
 * Created by User on 1/6/2017.
 */
public class CompoundTransaction<T> extends Transaction<T> {
    private final ArrayList<Lazy<Transaction<T>>> edits;

    public CompoundTransaction() {
        this.edits = new ArrayList<>();
    }

    public CompoundTransaction(ArrayList<Lazy<Transaction<T>>> edits) {
        this.edits = edits;
    }

    public void append(Lazy<Transaction<T>> edit) {
        edits.add(edit);
    }

    public void append(Factory<Transaction<T>> edit) {
        append(new Lazy<>(edit));
    }

    @Override
    public boolean redo(T target) {
        boolean actionPerformed = false;
        for(Lazy<Transaction<T>> e : edits) {
            if(e.getValue().redo(target)) actionPerformed = true;
        }
        return actionPerformed;
    }

    @Override
    public boolean undo(T target) {
        boolean actionPerformed = false;
        for(int i = edits.size()-1; i >= 0; i--) {
            Lazy<Transaction<T>> e = edits.get(i);
            if(e.getValue().undo(target)) actionPerformed = true;
        }
        return actionPerformed;
    }

    @Override
    public boolean redoOnInsert(T target) {
        boolean actionPerformed = false;
        for(Lazy<Transaction<T>> e : edits) {
            if(e.getValue().redoOnInsert(target)) actionPerformed = true;
        }
        return actionPerformed;
    }

    public int size() {
        return edits.size();
    }
}
