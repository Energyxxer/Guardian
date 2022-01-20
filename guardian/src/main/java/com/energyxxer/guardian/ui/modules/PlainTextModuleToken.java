package com.energyxxer.guardian.ui.modules;

import com.energyxxer.guardian.ui.Tab;
import com.energyxxer.guardian.ui.display.DisplayModule;
import com.energyxxer.guardian.ui.editor.PlainTextEditorModule;

import java.io.File;

public class PlainTextModuleToken extends FileModuleToken {
    private byte[] bytes;
    public PlainTextModuleToken(File file, byte[] bytes) {
        super(file);
        this.bytes = bytes;
    }

    @Override
    public DisplayModule createModule(Tab tab) {
        return new PlainTextEditorModule(tab, getFile(), bytes);
    }
}
