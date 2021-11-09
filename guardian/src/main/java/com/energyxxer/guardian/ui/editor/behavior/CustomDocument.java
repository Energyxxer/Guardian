package com.energyxxer.guardian.ui.editor.behavior;

import com.energyxxer.guardian.ui.editor.behavior.edits.DocumentEdit;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

public class CustomDocument extends DefaultStyledDocument {

    private AdvancedEditor editor;

    public CustomDocument(AdvancedEditor editor) {
        this.editor = editor;
    }

    private boolean redirectToTransaction = false;

    public void redirectToTransaction(boolean value) {
        redirectToTransaction = value;
    }

    @Override
    public void remove(int offs, int len) throws BadLocationException {
        if(redirectToTransaction) {
            editor.getTransactionManager().insertTransaction(new DocumentEdit(offs, len, null, null, editor));
        } else {
            removeTrusted(offs, len);
        }
    }

    public void removeTrusted(int offs, int len) throws BadLocationException {
        super.remove(offs, len);
    }

    @Override
    public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if(redirectToTransaction) {
            editor.getTransactionManager().insertTransaction(new DocumentEdit(offset, length, text, attrs, editor));
        } else {
            replaceTrusted(offset, length, text, attrs);
        }
    }
    public void replaceTrusted(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (length == 0 && (text == null || text.length() == 0)) {
            return;
        }
        if(length != 0) {
            removeTrusted(offset, length);
        }
        if(text != null && text.length() != 0) {
            insertStringTrusted(offset, text, attrs);
        }
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        if(redirectToTransaction) {
            editor.getTransactionManager().insertTransaction(new DocumentEdit(offs, 0, str, a, editor));
        } else {
            insertStringTrusted(offs, str, a);
        }
    }

    public void insertStringTrusted(int offs, String str, AttributeSet a) throws BadLocationException {
        super.insertString(offs, str, a);
    }
}
