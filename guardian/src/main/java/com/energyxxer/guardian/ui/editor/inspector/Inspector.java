package com.energyxxer.guardian.ui.editor.inspector;

import com.energyxxer.enxlex.lexical_analysis.inspections.Inspection;
import com.energyxxer.enxlex.lexical_analysis.inspections.InspectionModule;
import com.energyxxer.enxlex.lexical_analysis.inspections.InspectionSeverity;
import com.energyxxer.enxlex.report.Notice;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.HintStylizer;
import com.energyxxer.guardian.ui.editor.EditorComponent;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.util.Disposable;
import com.energyxxer.util.StringBounds;
import com.energyxxer.xswing.hints.TextHint;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Locale;
import java.util.function.Function;

/**
 * Created by User on 1/1/2017.
 */
public class Inspector implements Highlighter.HighlightPainter, MouseMotionListener, Disposable {

    private Inspection rolloverItem = null;

    private EditorComponent editor;

    private TextHint hint = GuardianWindow.hintManager.createTextHint("a");

    private InspectionModule inspectionModule;
    private InspectorDialog dialog;


    public Inspector(EditorComponent editor) {
        this.editor = editor;
        editor.addMouseMotionListener(this);

        try
        {
            editor.getHighlighter().addHighlight(0, 0, this);
        }
        catch(BadLocationException ble) {}

        hint.setInteractive(true);

        dialog = new InspectorDialog(this);
    }

    public void clear() {
        editor.repaint();
    }

    @Override
    public void paint(Graphics g, int p0, int p1, Shape graphicBounds, JTextComponent c) {
        try {
            if(inspectionModule != null)
            for (Inspection item : inspectionModule.getInspections()) {
                if(item.getSeverity() == null || item.getSeverity() == InspectionSeverity.HIDDEN) continue;

                g.setColor(GuardianWindow.getTheme().getColor("Inspector." + item.getSeverity().name().toLowerCase()));

                try {
                    StringBounds bounds = new StringBounds(
                            editor.getLocationForOffset(item.getStartIndex()),
                            editor.getLocationForOffset(item.getEndIndex())
                    );

                    for (int l = bounds.start.line; l <= bounds.end.line; l++) {
                        Rectangle rectangle;
                        if (l == bounds.start.line) {
                            rectangle = editor.modelToView(bounds.start.index);
                            if (bounds.start.line == bounds.end.line) {
                                //One line only
                                rectangle.width = editor.modelToView(bounds.end.index).x - rectangle.x;
                            } else {
                                rectangle.width = c.getWidth() - rectangle.x;
                            }
                        } else if (l == bounds.end.line) {
                            rectangle = editor.modelToView(bounds.end.index);
                            rectangle.width = rectangle.x - ((AdvancedEditor) c).modelToView(0).x;
                            rectangle.x = ((AdvancedEditor) c).modelToView(0).x; //0
                        } else {
                            rectangle = editor.modelToView(bounds.start.index);
                            rectangle.x = ((AdvancedEditor) c).modelToView(0).x; //0
                            rectangle.y += rectangle.height * (l - bounds.start.line);
                            rectangle.width = c.getWidth();
                        }

                        if(rectangle.width == 0) {
                            rectangle.width = c.getFont().getSize();
                        }

                        if ("SQUIGGLE".equals(GuardianWindow.getTheme().getString("Inspector." + item.getSeverity().name().toLowerCase() + ".style", "default:HIGHLIGHT").toUpperCase(Locale.ENGLISH))) {
                            for (int x = rectangle.x; x < rectangle.x + rectangle.width; x += 4) {
                                g.drawLine(x, rectangle.y + rectangle.height, x + 2, rectangle.y + rectangle.height - 2);
                                g.drawLine(x + 2, rectangle.y + rectangle.height - 2, x + 4, rectangle.y + rectangle.height);
                            }
                        } else {
                            g.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
                        }
                    }
                } catch (BadLocationException e) {
                    //e.printStackTrace();
                }
            }
        } catch(ConcurrentModificationException e) {}
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int index = editor.viewToModel(e.getPoint());
        if(inspectionModule != null)
        for(Inspection inspection : inspectionModule.getInspections()) {
            if(inspection.getSeverity() != null
                    && inspection.getSeverity() != InspectionSeverity.HIDDEN
                    && index >= inspection.getStartIndex()
                    && (
                    index < inspection.getEndIndex()
                            || (
                            inspection.getStartIndex() == inspection.getEndIndex()
                                    && index <= inspection.getEndIndex()
                    )
            )
            ) {
                if(rolloverItem != inspection) {
                    rolloverItem = inspection;
                    if(!hint.isShowing()) {
                        hint.setText(inspection.getDescription());
                        HintStylizer.style(hint, inspection.getSeverity().name().toLowerCase(Locale.ENGLISH));
                        hint.show(e.getLocationOnScreen(), () -> rolloverItem != null && editor != null && editor.isShowing());
                    }
                } else if(!hint.isShowing()) {
                    hint.updateLocation(e.getLocationOnScreen());
                }
                return;
            }
        }
        rolloverItem = null;
    }

    public void insertNotices(ArrayList<Notice> notices) {
        for(Notice n : notices) {
            insertNotice(n);
        }
    }

    public void insertNotice(Notice n) {
        InspectionSeverity type = InspectionSeverity.SUGGESTION;
        switch(n.getType()) {
            case ERROR: {
                type = InspectionSeverity.ERROR;
                break;
            }
            case WARNING: {
                type = InspectionSeverity.WARNING;
                break;
            }
        }
        Inspection item = new Inspection(n.getMessage());
        item.setBounds(n.getLocationIndex(), n.getLocationIndex() + n.getLocationLength());
        item.setSeverity(type);
        inspectionModule.addInspection(item);
    }

    public void registerCharacterDrift(Function<Integer, Integer> h) {
//        for(InspectionItem item : legacyItems) {
//            item.bounds.start = editor.getLocationForOffset(h.apply(item.bounds.start.index));
//            item.bounds.end = editor.getLocationForOffset(h.apply(item.bounds.end.index));
//        }
        //TODO apply drift to inspection module
    }

    public void setInspectionModule(InspectionModule inspectionModule) {
        this.inspectionModule = inspectionModule;
    }

    public InspectionModule getInspectionModule() {
        return inspectionModule;
    }

    public InspectorDialog getDialog() {
        return dialog;
    }

    public void setDialog(InspectorDialog dialog) {
        this.dialog = dialog;
    }

    public EditorComponent getEditor() {
        return editor;
    }

    public void showHints(int index) {
        dialog.showHints(index);
    }

    @Override
    public void dispose() {
        hint.dispose();
        editor.removeMouseMotionListener(this);
        editor = null;

        inspectionModule = null;
        if(dialog != null) dialog.dispose();
        dialog = null;
    }
}
