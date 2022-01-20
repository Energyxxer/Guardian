package com.energyxxer.guardian.ui.editor;

import com.energyxxer.guardian.global.Commons;
import com.energyxxer.guardian.global.Preferences;
import com.energyxxer.guardian.global.temp.projects.Project;
import com.energyxxer.guardian.global.temp.projects.ProjectManager;
import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.guardian.ui.Tab;
import com.energyxxer.guardian.ui.display.DisplayModule;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.modules.FileModuleToken;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.ui.styledcomponents.StyledLabel;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.guardian.util.linenumber.PlainTextLineNumber;
import com.energyxxer.util.Disposable;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;

public class PlainTextEditorModule extends JPanel implements DisplayModule, Disposable  {
    private Tab tab;
    private File file;

    private ThemeListenerManager tlm;

    private JScrollPane scrollPane;
    private JTextArea textArea;
    private PlainTextLineNumber tln;

    private int edits = 0;

    public PlainTextEditorModule(Tab tab, File file, byte[] bytes) {
        this.setLayout(new BorderLayout());
        this.tab = tab;
        this.file = file;

        this.scrollPane = new JScrollPane();
        this.textArea = new JTextArea();
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        tln = new PlainTextLineNumber(textArea, scrollPane);
        tln.setPadding(10);

        JPanel container = new JPanel(new BorderLayout());
        container.add(textArea);
        scrollPane.setViewportView(container);
        this.add(scrollPane, BorderLayout.CENTER);

        scrollPane.getVerticalScrollBar().setUnitIncrement(17);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(17);
        scrollPane.getVerticalScrollBar().setValue(0);
        scrollPane.getHorizontalScrollBar().setValue(0);

        scrollPane.setRowHeaderView(tln);

        if(bytes != null) {
            String text = new String(bytes, Guardian.DEFAULT_CHARSET);
            setText(text);
        } else {
            reloadFromDisk();
        }

        textArea.getDocument().addUndoableEditListener(e -> {
            edits++;
            if(tab != null) tab.onEdit();
        });


        JPanel disclaimer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        StyledLabel disclaimerLabel = new StyledLabel("<html>Many features are not available in this lightweight editor,<br>such as undo/redo, syntax highlighting, multiple selections, among others.</html>", tlm);
        disclaimer.add(disclaimerLabel);
        this.add(disclaimer, BorderLayout.NORTH);

        tlm = new ThemeListenerManager();

        tlm.addThemeChangeListener(t -> {
            disclaimer.setBackground(t.getColor(Color.WHITE, "TabList.background"));
            textArea.setBackground(t.getColor(Color.WHITE, "Editor.background"));
            this.setBackground(textArea.getBackground());
            textArea.setForeground(t.getColor(Color.BLACK, "Editor.foreground","General.foreground"));
            textArea.setSelectedTextColor(textArea.getForeground());
            textArea.setCaretColor(textArea.getForeground());
            textArea.setSelectionColor(t.getColor(new Color(50, 100, 175), "Editor.selection.background"));
            textArea.setFont(new Font(AdvancedEditor.FONT.get(), Font.PLAIN, Preferences.getModifiedEditorFontSize()));

            tln.setBackground(t.getColor(new Color(235, 235, 235), "Editor.lineNumber.background"));
            tln.setForeground(t.getColor(new Color(150, 150, 150), "Editor.lineNumber.foreground"));
            tln.setCurrentLineForeground(t.getColor(tln.getForeground(), "Editor.lineNumber.currentLine.foreground"));
            tln.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(
                                    0,
                                    0,
                                    0,
                                    Math.max(t.getInteger(1,"Editor.lineNumber.border.thickness"),0),
                                    t.getColor(new Color(200, 200, 200), "Editor.lineNumber.border.color","General.line")
                            ),
                            BorderFactory.createEmptyBorder(
                                    0,
                                    0,
                                    0,
                                    15
                            )
                    )
            );
            tln.setFont(new Font(t.getString("Editor.lineNumber.font","default:monospaced"),Font.PLAIN, Preferences.getModifiedEditorFontSize()));
        });
    }

    private void setText(String text) {
        if(textArea != null) textArea.setText(text);
    }

    private String getText() {
        return textArea != null ? textArea.getText() : "";
    }

    public void reloadFromDisk() {
        if(file == null) return;
        byte[] encoded;
        try {
            encoded = Files.readAllBytes(file.toPath());
            String s = new String(encoded, Guardian.DEFAULT_CHARSET);
            setText(s);
            if(tab != null) tab.updateSavedValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void displayCaretInfo() {

    }

    @Override
    public Object getValue() {
        return edits;
    }

    @Override
    public boolean canSave() {
        return true;
    }

    @Override
    public Object save() {
        if(file == null) return null;
        PrintWriter writer;
        try {
            writer = new PrintWriter(file, "UTF-8");

            String text = getText();

            if(EditorModule.INSERT_TRAILING_NEWLINE.get() && !text.endsWith("\n")) {
                try {
                    textArea.getDocument().insertString(text.length(),"\n",null);
                    text = text.concat("\n");
                } catch(BadLocationException e) {
                    e.printStackTrace();
                }
            }

            writer.print(text);
            writer.close();

            Project associatedProject = ProjectManager.getAssociatedProject(file);
            if(associatedProject != null && associatedProject.getProjectType().isProjectIdentity(file)) {
                ProjectManager.loadWorkspace();
            }

            Commons.index(associatedProject);

            return getValue();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void focus() {
        this.requestFocus();
    }

    @Override
    public boolean moduleHasFocus() {
        return this.isFocusOwner();
    }

    @Override
    public boolean transform(ModuleToken newToken) {
        if(newToken instanceof FileModuleToken) {
            file = ((FileModuleToken) newToken).getFile();
            return true;
        }
        return false;
    }

    @Override
    public void dispose() {
        file = null;
        tab = null;
        textArea = null;
        tlm.dispose();
        tlm = null;
    }
}
