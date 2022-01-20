package com.energyxxer.guardian.ui;

import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.display.DisplayModule;
import com.energyxxer.guardian.ui.editor.EditorModule;
import com.energyxxer.guardian.ui.editor.TextFileTooLargeException;
import com.energyxxer.guardian.ui.modules.FileModuleToken;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.ui.modules.PlainTextModuleToken;
import com.energyxxer.guardian.ui.styledcomponents.StyledButton;
import com.energyxxer.guardian.ui.styledcomponents.StyledLabel;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.Disposable;

import javax.swing.*;
import java.io.File;

public class TextFileTooLargeModule extends JPanel implements DisplayModule, Disposable {
    File file;
    Tab tab;
    byte[] bytes;
    private ThemeListenerManager tlm;

    public TextFileTooLargeModule(TextFileTooLargeException x) {
        tlm = new ThemeListenerManager();
        this.tab = x.tab;
        this.file = x.file;
        this.bytes = x.bytes;
        this.setOpaque(false);


        StyledLabel label = new StyledLabel("This file (" + x.bytes.length / 1024 / 1024 + " MB) exceeds the maximum size allowed for the default editor (" + EditorModule.MAX_FILESIZE_MB.get() + " MB)", tlm);
        Box box = Box.createVerticalBox();
        label.setAlignmentX(0.5f);
        box.add(label);

        if(tab != null) {
            StyledButton button = new StyledButton("Open anyway", tlm);
            button.setAlignmentX(0.5f);
            box.add(button);
            button.addActionListener(a -> openPlainText());
        }

        this.add(box);
    }

    private void openPlainText() {
        GuardianWindow.tabManager.closeTab(tab, true);
        GuardianWindow.tabManager.openTab(new PlainTextModuleToken(file, bytes));
    }

    @Override
    public void displayCaretInfo() {

    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public boolean canSave() {
        return false;
    }

    @Override
    public Object save() {
        return null;
    }

    @Override
    public void focus() {

    }

    @Override
    public boolean moduleHasFocus() {
        return this.isFocusOwner();
    }

    @Override
    public boolean transform(ModuleToken moduleToken) {
        if(moduleToken instanceof FileModuleToken) {
            file = ((FileModuleToken) moduleToken).getFile();
            return true;
        }
        return false;
    }

    @Override
    public void dispose() {
        tlm.dispose();
        tlm = null;
    }
}
