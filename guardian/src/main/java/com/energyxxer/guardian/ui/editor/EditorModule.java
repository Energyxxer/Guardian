package com.energyxxer.guardian.ui.editor;

import com.energyxxer.guardian.global.Commons;
import com.energyxxer.guardian.global.Preferences;
import com.energyxxer.guardian.global.temp.Lang;
import com.energyxxer.guardian.global.temp.projects.Project;
import com.energyxxer.guardian.global.temp.projects.ProjectManager;
import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.main.window.sections.editor_search.FindAndReplaceBar;
import com.energyxxer.guardian.ui.Tab;
import com.energyxxer.guardian.ui.display.DisplayModule;
import com.energyxxer.guardian.ui.editor.behavior.caret.Dot;
import com.energyxxer.guardian.ui.editor.behavior.edits.DeletionEdit;
import com.energyxxer.guardian.ui.modules.FileModuleToken;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.ui.scrollbar.OverlayScrollPaneLayout;
import com.energyxxer.guardian.ui.theme.Theme;
import com.energyxxer.guardian.ui.theme.ThemeManager;
import com.energyxxer.guardian.ui.theme.change.ThemeChangeListener;
import com.energyxxer.guardian.util.linenumber.TextLineNumber;
import com.energyxxer.util.Disposable;
import com.energyxxer.util.Lazy;
import com.energyxxer.util.logger.Debug;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Display module for the main text editor of the program.
 */
public class EditorModule extends JPanel implements DisplayModule, UndoableEditListener, MouseListener, ThemeChangeListener, Disposable {

    public static Preferences.SettingPref<Boolean> INSERT_TRAILING_NEWLINE = new Preferences.SettingPref<>("settings.editor.insert_trailing_newline", false, Boolean::parseBoolean);
    public static Preferences.SettingPref<Boolean> WORD_WRAP = new Preferences.SettingPref<>("settings.editor.word_wrap", false, Boolean::parseBoolean);

    JScrollPane scrollPane;

    File file;
    Tab associatedTab;
    Lang forcedLang = null;

    public EditorComponent editorComponent;
    private TextLineNumber tln;
    protected Theme syntax;

    Style defaultParagraphStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
    Style collapsedParagraphStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
    private final ArrayList<String> styles = new ArrayList<>();
    List<HierarchicalStyle> hierarchicalStyles = Collections.synchronizedList(new ArrayList<>());

    private Lazy<FindAndReplaceBar> searchBar = new Lazy<>(() -> new FindAndReplaceBar(this));
    private boolean searchBarVisible = false;

    long highlightTime = 0;

    //public long lastToolTip = new Date().getTime();

    public EditorModule(Tab tab, File file) {
        super(new BorderLayout());
        this.file = file;
        this.associatedTab = tab;
        this.scrollPane = new JScrollPane();

        editorComponent = new EditorComponent(this) {
            @Override
            public Dimension getPreferredSize() {
                if(WORD_WRAP.get()) {
                    int width = scrollPane.getViewport().getSize().width;
                    int height = super.getPreferredSize().height;
                    return new Dimension(width, height);
                } else return super.getPreferredSize();
            }
        };

        JPanel container = new JPanel(new BorderLayout());
        container.add(editorComponent);
        scrollPane.setViewportView(container);

        this.add(scrollPane, BorderLayout.CENTER);

        tln = new TextLineNumber(editorComponent, scrollPane);
        tln.setPadding(10);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        editorComponent.addMouseListener(this);

        scrollPane.setRowHeaderView(tln);

        scrollPane.setLayout(new OverlayScrollPaneLayout(scrollPane, editorComponent.tlm));

        scrollPane.getVerticalScrollBar().setUnitIncrement(17);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(17);
        scrollPane.getVerticalScrollBar().setValue(0);
        scrollPane.getHorizontalScrollBar().setValue(0);

        scrollPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                focus();
            }
        });
        scrollPane.getVerticalScrollBar().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                focus();
            }
        });
        scrollPane.getHorizontalScrollBar().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                focus();
            }
        });
        scrollPane.getViewport().addChangeListener(l -> {
            if(editorComponent != null) editorComponent.viewportChanged();
        });

        addThemeChangeListener();

        startEditListeners();

        reloadFromDisk();
    }

    public void showSearchBar() {
        Dot dot = editorComponent.getCaret().getDots().get(0);
        if(!searchBarVisible) {
            this.add(searchBar.getValue(), BorderLayout.NORTH);
            searchBar.getValue().onReveal();
            revalidate();
            searchBarVisible = true;

            scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getValue() + searchBar.getValue().getHeight());
            repaint();
        }

        if(!dot.isPoint()) {
            try {
                searchBar.getValue().setFindText(editorComponent.getDocument().getText(dot.getMin(), dot.getMax()-dot.getMin()));
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        searchBar.getValue().focus();
    }

    public void hideSearchBar() {
        if(searchBarVisible) {
            scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getValue() - searchBar.getValue().getHeight());

            this.remove(searchBar.getValue());
            searchBar.getValue().onDismiss();
            revalidate();
            focus();
            searchBarVisible = false;
            repaint();
        }
    }

    @Override
    public void performModuleAction(String key) {
        switch(key) {
            case "editor.find": {
                showSearchBar();
                break;
            }
            case "editor.reload": {
                reloadFromDisk();
                break;
            }
            case "editor.jump_to_matching_brace": {
                editorComponent.jumpToMatchingBrace();
                break;
            }
            case "undo": {
                editorComponent.getTransactionManager().undo();
                break;
            }
            case "redo": {
                editorComponent.getTransactionManager().redo();
                break;
            }
            case "copy": {
                editorComponent.copy();
                break;
            }
            case "cut": {
                editorComponent.cut();
                break;
            }
            case "paste": {
                editorComponent.paste();
                break;
            }
            case "show_hints": {
                editorComponent.showHints();
                break;
            }
            case "delete": {
                editorComponent.getTransactionManager().insertTransaction(new DeletionEdit(editorComponent));
                break;
            }
        }
    }

    public void reloadFromDisk() {
        if(file == null) return;
        byte[] encoded;
        try {
            encoded = Files.readAllBytes(file.toPath());
            String s = new String(encoded, Guardian.DEFAULT_CHARSET);
            setText(s);
            editorComponent.setCaretPosition(0);
            if(associatedTab != null) associatedTab.updateSavedValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startEditListeners() {
        editorComponent.getDocument().addUndoableEditListener(this);
    }

    private void clearStyles() {
        for(String key : this.styles) {
            editorComponent.removeStyle(key);
        }
        for(HierarchicalStyle style : this.hierarchicalStyles) {
            editorComponent.removeStyle(style.key);
        }
        editorComponent.removeStyle("_DEFAULT_STYLE");
        this.styles.clear();
        this.hierarchicalStyles.clear();
    }

    private void setSyntax(Theme newSyntax) {
        if(newSyntax == null) {
            syntax = null;
            clearStyles();
            return;
        }
        if(newSyntax.getThemeType() != Theme.ThemeType.SYNTAX_THEME) {
            Debug.log("Theme \"" + newSyntax + "\" is not a syntax theme!", Debug.MessageType.ERROR);
            return;
        }

        this.syntax = newSyntax;
        clearStyles();
        for(String value : syntax.getValues().keySet()) {
            if(!value.contains(".")) continue;
            //if(sections.length > 2) continue;

            String name = value.substring(0,value.lastIndexOf("."));
            Style style = editorComponent.getStyle(name);
            if(style == null) {
                style = editorComponent.addStyle(name, null);
                this.styles.add(name);
                if(name.startsWith("$")) {
                    hierarchicalStyles.add(new HierarchicalStyle(name));
                }
            }
            switch(value.substring(value.lastIndexOf(".")+1)) {
                case "foreground": {
                    StyleConstants.setForeground(style, syntax.getColor(value));
                    break;
                }
                case "background": {
                    StyleConstants.setBackground(style, syntax.getColor(value));
                    break;
                }
                case "italic": {
                    StyleConstants.setItalic(style, syntax.getBoolean(value));
                    break;
                }
                case "bold": {
                    StyleConstants.setBold(style, syntax.getBoolean(value));
                    break;
                }
            }
        }
    }

    public void setText(String text) {
        if(editorComponent != null) {
            editorComponent.setText(text);

            editorComponent.highlight();
        }
    }

    public String getText() {
        return editorComponent != null ? editorComponent.getText() : "";
    }

    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        if (!e.getEdit().getPresentationName().equals("style change") && editorComponent != null) {
            editorComponent.highlight();
            if(associatedTab != null) associatedTab.onEdit();
        }
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {

    }

    @Override
    public void mouseEntered(MouseEvent arg0) {

    }

    @Override
    public void mouseExited(MouseEvent arg0) {

    }

    @Override
    public void mousePressed(MouseEvent arg0) {
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {

    }

    public void ensureVisible(int index) {
        try {
            Rectangle view = scrollPane.getViewport().getViewRect();
            Rectangle rect = editorComponent.modelToView(index);
            if(rect == null) return;
            rect.width = 2;
            rect.x -= view.x;
            rect.y -= view.y;
            scrollPane.getViewport().scrollRectToVisible(rect);
        } catch (BadLocationException x) {
            x.printStackTrace();
        }
    }

    public void scrollToCenter(int index) {
        try {
            Rectangle view = scrollPane.getViewport().getViewRect();
            Rectangle rect = editorComponent.modelToView(index);
            if(rect == null) return;
            rect.width = view.width;
            rect.height = view.height;
            rect.x -= rect.width/2;
            rect.y -= rect.height/2;
            rect.x -= view.x;
            rect.y -= view.y;
            scrollPane.getViewport().scrollRectToVisible(rect);
        } catch (BadLocationException x) {
            x.printStackTrace();
        }
    }

    @Override
    public void themeChanged(Theme t) {
        tln.setBackground(t.getColor(new Color(235, 235, 235), "Editor.lineNumber.background"));
        tln.setForeground(t.getColor(new Color(150, 150, 150), "Editor.lineNumber.foreground"));
        //tln current line background
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

        updateSyntax(t);
    }

    public void updateSyntax() {
        updateSyntax(GuardianWindow.getTheme());
    }

    public void updateSyntax(Theme t) {
        if(editorComponent == null) return;
        collapsedParagraphStyle = editorComponent.addStyle("_COLLAPSED_STYLE", null);
        StyleConstants.setLineSpacing(collapsedParagraphStyle, -1f);
        StyleConstants.setFontSize(collapsedParagraphStyle, 0);

        editorComponent.getStyledDocument().setParagraphAttributes(0, editorComponent.getStyledDocument().getLength(), defaultParagraphStyle, false);

        Lang lang = getLanguage();
        if(lang != null) {
            setSyntax(ThemeManager.getSyntaxForGUITheme(lang, t));
            editorComponent.highlight();
        }
    }

    @Override
    public void displayCaretInfo() {
        editorComponent.displayCaretInfo();
    }

    @Override
    public Object getValue() {
        return getText();
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

            if(INSERT_TRAILING_NEWLINE.get() && !text.endsWith("\n")) {
                try {
                    editorComponent.getDocument().insertString(text.length(),"\n",null);
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
        editorComponent.requestFocus();
    }

    @Override
    public boolean moduleHasFocus() {
        return this.hasFocus() || editorComponent.hasFocus();
    }

    @Override
    public void onSelect() {
        Project project = ProjectManager.getAssociatedProject(file);
        if(project != null && project.getInstantiationTime() >= highlightTime) {
            editorComponent.highlight();
        }
    }

    @Override
    public void dispose() {
        this.disposeTLM();
        if(searchBar.hasValue()) searchBar.getValue().dispose();
        editorComponent.dispose();
        tln.dispose();
        tln = null;
        editorComponent = null;
    }

    public void setEditable(boolean editable) {
        editorComponent.setEditable(editable);
    }

    public Lang getLanguage() {
        return forcedLang != null ? forcedLang : file != null ? Lang.getLangForFile(file.getPath()) : null;
    }

    public void setForcedLanguage(Lang forcedLang) {
        this.forcedLang = forcedLang;
    }

    public File getFileForAnalyzer() {
        if(file != null) return file;
        if(forcedLang != null) return new File(Preferences.get("workspace_dir"));
        return null;
    }

    @Override
    public boolean transform(ModuleToken newToken) {
        if(newToken instanceof FileModuleToken) {
            file = ((FileModuleToken) newToken).getFile();
            updateSyntax();
            editorComponent.getStyledDocument().setCharacterAttributes(0, editorComponent.getStyledDocument().getLength(), StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE), true);
            editorComponent.getStyledDocument().setParagraphAttributes(0, editorComponent.getStyledDocument().getLength(), defaultParagraphStyle, false);
            editorComponent.highlight();
        }
        return true;
    }

    public static class HierarchicalStyle {
        public String key;
        public String[] parts;

        public HierarchicalStyle(String key) {
            this(key, key.substring(1).toUpperCase(Locale.ENGLISH).split("\\."));
        }

        public HierarchicalStyle(String key, String[] parts) {
            this.key = key;
            this.parts = parts;
        }
    }
}