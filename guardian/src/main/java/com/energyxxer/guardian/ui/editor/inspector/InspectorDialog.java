package com.energyxxer.guardian.ui.editor.inspector;

import com.energyxxer.enxlex.lexical_analysis.inspections.Inspection;
import com.energyxxer.enxlex.lexical_analysis.inspections.SuggestionInspection;
import com.energyxxer.guardian.global.keystrokes.KeyMap;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.main.window.sections.quick_find.StyledExplorerMaster;
import com.energyxxer.guardian.ui.editor.EditorComponent;
import com.energyxxer.guardian.ui.editor.behavior.caret.CaretProfile;
import com.energyxxer.guardian.ui.editor.behavior.editmanager.edits.CompoundEdit;
import com.energyxxer.guardian.ui.editor.behavior.editmanager.edits.DeletionEdit;
import com.energyxxer.guardian.ui.editor.behavior.editmanager.edits.InsertionEdit;
import com.energyxxer.guardian.ui.editor.behavior.editmanager.edits.SetCaretProfileEdit;
import com.energyxxer.guardian.ui.explorer.base.StandardExplorerItem;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.ui.scrollbar.OverlayScrollPane;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.Lazy;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.xswing.ScalableDimension;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

public class InspectorDialog extends JDialog implements KeyListener, FocusListener {
    private EditorComponent editor;

    private OverlayScrollPane scrollPane;
    private StyledExplorerMaster explorer;

    private ThemeListenerManager tlm = new ThemeListenerManager();

    private Inspector inspector = null;

    private boolean locked = false;
    private boolean forceLocked = false;

    private ArrayList<ExecutableInspectionToken> activeTokens = new ArrayList<>();
    private int activeIndex;

    public InspectorDialog(Inspector inspector) {
        super(GuardianWindow.jframe, false);
        this.setUndecorated(true);
        this.inspector = inspector;
        this.editor = inspector.getEditor();

        this.explorer = new StyledExplorerMaster("EditorHints");

        JPanel contentPane = new JPanel(new BorderLayout());
        scrollPane = new OverlayScrollPane(tlm, explorer);
        contentPane.add(scrollPane);

        this.setContentPane(contentPane);

        tlm.addThemeChangeListener(t -> {
            int thickness = Math.max(t.getInteger(1, "EditorHints.border.thickness"), 0);
            contentPane.setBackground(t.getColor(new Color(200, 200, 200), "EditorHints.header.background"));
            contentPane.setBorder(BorderFactory.createMatteBorder(thickness, thickness, thickness, thickness, t.getColor(new Color(200, 200, 200), "EditorHints.border.color")));
        });

        editor.addKeyListener(this);
        editor.addFocusListener(this);
        this.addKeyListener(this);
        explorer.addKeyListener(this);
    }

    public void showHints(int index) {
        if(this.isVisible()) return;
        activeIndex = index;
        explorer.clear();
        activeTokens.clear();

        boolean any = false;
        boolean anyExpandable = false;

        List<Inspection> inspections = inspector.getInspectionModule().collectInspectionsForIndex(index);
        for(Inspection inspection : inspections) {
            if(inspection instanceof SuggestionInspection) {
                ExecutableInspectionToken token = new ExecutableInspectionToken(this, (SuggestionInspection) inspection);
                StandardExplorerItem item = new StandardExplorerItem(token, explorer, null);
                explorer.addElement(item);
                activeTokens.add(token);
                if (!anyExpandable) {
                    item.setSelected(true);
                    explorer.setSelected(item, null);
                }
                anyExpandable = true;
            }
            any = true;
        }

        if(any) {
            Debug.log("Received " + explorer.getTotalCount() + " hints");
            this.setVisible(true);
            relocate(Math.min(index, editor.getDocument().getLength()));
            editor.requestFocus();
        } else {
            Debug.log("No hints for index " + index);
            this.setVisible(false);
        }
    }

    public void submit(SuggestionInspection inspection) {
        this.setVisible(false);

        int replacementStartIndex = inspection.getReplacementStartIndex();
        int replacementEndIndex = inspection.getReplacementEndIndex();
        String replacementText = inspection.getReplacementText();

        CompoundEdit edit = new CompoundEdit();
        edit.appendEdit(new Lazy<>(() -> new SetCaretProfileEdit(new CaretProfile(replacementStartIndex, replacementEndIndex), editor)));
        if(replacementStartIndex != replacementEndIndex) {
            edit.appendEdit(new Lazy<>(() -> new DeletionEdit(editor)));
        }
        edit.appendEdit(new Lazy<>(() -> new InsertionEdit(replacementText, editor)));
        editor.getEditManager().insertEdit(edit);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        boolean editorNotFocused = GuardianWindow.jframe.getFocusOwner() == null;
        if(editorNotFocused) {
            editor.requestFocus();
            if(!e.isConsumed()) editor.keyTyped(e);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        //Debug.log("Pressed");
        if(!this.isVisible() || activeTokens.isEmpty()) return;
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            this.setVisible(false);
            e.consume();
            return;
        }
        int selectedIndex = explorer.getFirstSelectedIndex();
        if(selectedIndex < 0) selectedIndex = 0;
        if(e.getKeyCode() == KeyEvent.VK_DOWN) {
            selectedIndex++;
            if(selectedIndex >= explorer.getTotalCount()) {
                selectedIndex = 0;
            }
            explorer.setSelectedIndex(selectedIndex);
            e.consume();
        } else if(e.getKeyCode() == KeyEvent.VK_UP) {
            selectedIndex--;
            if(selectedIndex < 0) {
                selectedIndex = explorer.getTotalCount()-1;
            }
            explorer.setSelectedIndex(selectedIndex);
            e.consume();
        } else if(KeyMap.INSPECTION_SELECT.wasPerformedExact(e)) {
            java.util.List<ModuleToken> tokens = explorer.getSelectedTokens();
            if(!tokens.isEmpty()) {
                tokens.get(0).onInteract();
            }
            e.consume();
        }
        boolean editorNotFocused = GuardianWindow.jframe.getFocusOwner() == null;
        if(editorNotFocused) {
            editor.requestFocus();
            if(!e.isConsumed()) editor.keyPressed(e);
        }

        Rectangle rect = explorer.getVisibleRect(selectedIndex);
        rect.y -= scrollPane.getViewport().getViewRect().y;

        scrollPane.getViewport().scrollRectToVisible(rect);
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public void dismiss(boolean force) {
        if(isVisible() && !forceLocked) {
            if (force || !locked) {
                this.setVisible(false);
            }
        }
        locked = false;
    }

    public EditorComponent getEditor() {
        return editor;
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
    }

    private void resize() {
        int shownTokens = activeTokens.size();
        this.setSize(new ScalableDimension(400, Math.min(300, explorer.getRowHeight() * shownTokens + 2)));
    }

    private void relocate(int index) {
        resize();
        try {
            Rectangle rect = editor.modelToView(index);
            if(rect == null) return;
            Point loc = rect.getLocation();
            loc.y += rect.height;
            loc.translate(editor.getLocationOnScreen().x, editor.getLocationOnScreen().y);
            if(loc.y + this.getHeight() >= GuardianWindow.jframe.getLocationOnScreen().y + GuardianWindow.jframe.getHeight()) {
                loc.y -= editor.getLineHeight();
                loc.y -= this.getHeight();
            }
            this.setLocation(loc);
        } catch (BadLocationException x) {
            Debug.log("BadLocationException: " + x.getMessage() + "; index " + x.offsetRequested(), Debug.MessageType.ERROR);
            x.printStackTrace();
        } catch(IllegalComponentStateException ignored) {

        }
    }

    public void relocate() {
        if(editor != null && editor.isVisible() && editor.isShowing()) {
            relocate(activeIndex);
        }
    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        if(e.getOppositeComponent() != null && e.getOppositeComponent() != this && e.getOppositeComponent() != explorer) {
            dismiss(true);
        }
    }

    private boolean disposed = false;

    @Override
    public void dispose() {
        if(!disposed) {
            super.dispose();
            tlm.dispose();
            explorer.dispose();
        }
        disposed = true;
    }
}