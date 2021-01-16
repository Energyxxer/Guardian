package com.energyxxer.guardian.ui.common.transactions;

import java.util.ArrayList;

/**
 * Created by User on 1/5/2017.
 */
public class TransactionManager<T> {
    private ArrayList<Transaction<T>> transactions = new ArrayList<>();
    private int currentTransaction = 0;

    /**
     * Time interval in milliseconds within which transactions will be redone and undone together.
     * */
    private int chainDelay = 300;

    protected T target;

    public TransactionManager(T target) {
        this.target = target;
    }

    public void undo() {
        if(currentTransaction -1 >= 0) {
            if(canUndo()) {
                Transaction<T> transaction = transactions.get(--currentTransaction);
                transaction.undo(target);
                undone(transactions.get(currentTransaction));
                if(currentTransaction > 0 && Math.abs(transactions.get(currentTransaction).time - transactions.get(currentTransaction -1).time) <= chainDelay) {
                    undo();
                }
            }
        }
    }

    public void redo() {
        if(currentTransaction < transactions.size()) {
            if(canRedo()) {
                Transaction<T> transaction = transactions.get(currentTransaction++);
                transaction.redo(target);
                redone(transaction);
                if(currentTransaction < transactions.size() && Math.abs(transactions.get(currentTransaction -1).time - transactions.get(currentTransaction).time) <= chainDelay) {
                    redo();
                }
            }
        }
    }

    public void insertTransaction(Transaction<T> transaction) {
        if(transaction.redo(target)) {
            while(transactions.size() > currentTransaction) {
                transactions.remove(currentTransaction);
            }
            transactions.add(transaction);
            currentTransaction++;
            inserted(transaction);
        }
    }

    public int getChainDelay() {
        return chainDelay;
    }

    public void setChainDelay(int chainDelay) {
        this.chainDelay = chainDelay;
    }

    protected boolean canUndo() {
        return true;
    }
    protected boolean canRedo() {
        return true;
    }
    protected void undone(Transaction<T> transaction) {

    }
    protected void redone(Transaction<T> transaction) {

    }
    protected void inserted(Transaction<T> transaction) {
    }


}
