package com.energyxxer.guardian.ui.editor;

import com.energyxxer.guardian.ui.Tab;

import java.io.File;

public class TextFileTooLargeException extends RuntimeException {
    public File file;
    public Tab tab;
    public byte[] bytes;

    public TextFileTooLargeException(File file, Tab tab, byte[] bytes) {
        this.file = file;
        this.tab = tab;
        this.bytes = bytes;
    }
}
