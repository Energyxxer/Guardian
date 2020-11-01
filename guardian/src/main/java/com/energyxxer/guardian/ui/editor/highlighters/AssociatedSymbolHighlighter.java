package com.energyxxer.guardian.ui.editor.highlighters;

import com.energyxxer.enxlex.lexical_analysis.token.TokenSource;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.main.window.sections.tools.find.FileOccurrence;
import com.energyxxer.guardian.main.window.sections.tools.find.FindResults;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.editor.behavior.caret.EditorCaret;
import com.energyxxer.guardian.ui.editor.behavior.caret.EditorSelectionPainter;
import com.energyxxer.guardian.ui.editor.completion.SuggestionDialog;
import com.energyxxer.guardian.ui.modules.FileModuleToken;
import com.energyxxer.prismarine.summaries.PrismarineSummaryModule;
import com.energyxxer.prismarine.summaries.SummarySymbol;
import com.energyxxer.util.StringBounds;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.ArrayList;

public class AssociatedSymbolHighlighter implements Highlighter.HighlightPainter, MouseMotionListener, MouseListener {

    private final AdvancedEditor editor;

    private boolean shouldRender = false;
    private ArrayList<Rectangle> rectangles = new ArrayList<>();
    private Rectangle hoveringRectangle = null;

    private int prevSeenDot = -1;

    public AssociatedSymbolHighlighter(AdvancedEditor editor, EditorCaret caret) {
        this.editor = editor;

        caret.addCaretPaintListener(() -> {
            int dot = editor.getCaret().getDot();
            if(prevSeenDot != dot) {
                prevSeenDot = dot;
                updateRectangles();
                if(prevSeenDot != -1) editor.repaint();
            }
        });

        editor.addMouseListener(this);
        editor.addMouseMotionListener(this);
    }

    private void updateRectangles() {
        shouldRender = false;
        rectangles.clear();
        if(editor.getCaret().getDots().size() != 1 || !editor.getCaret().getDots().get(0).isPoint()) {
            prevSeenDot = -1;
            return;
        }
        try {
            int dot = editor.getCaret().getDot();

            int wordStart = editor.getWordStart(dot);
            int wordEnd = editor.getWordEnd(dot);
            String word = editor.getText(wordStart, wordEnd-wordStart);
            PrismarineSummaryModule lastSuccessfulSummary = ((SuggestionDialog) editor.getSuggestionInterface()).getLastSuccessfulSummary();
            if(lastSuccessfulSummary != null) {
                SummarySymbol selectedSymbol = lastSuccessfulSummary.getSymbolForName(word, wordStart);
                if(selectedSymbol == null) return;

                for(PrismarineSummaryModule.SymbolUsage usage : lastSuccessfulSummary.getSymbolUsages()) {
                    if(!usage.symbolName.equals(word)) continue; //filter out symbols that aren't the selected symbol's name

                    StringBounds bounds = usage.pattern.getStringBounds();

                    SummarySymbol thisUsageSymbol = lastSuccessfulSummary.getSymbolForName(word, bounds.start.index);

                    if(thisUsageSymbol != selectedSymbol) continue; //filter out identifiers that don't refer to the same symbol

                    if(bounds.start.index == wordStart && bounds.end.index == wordEnd) { //If this usage refers to the identifier that's currently selected...
                        shouldRender = true;
                    }
                    try {
                        rectangles.addAll(EditorSelectionPainter.getRectanglesForBounds(editor, bounds));
                    } catch (BadLocationException ignore) {}
                }
            }

        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
        if(c instanceof AdvancedEditor) {
            g.setColor(editor.getBraceHighlightColor());

            if(shouldRender) {
                for(Rectangle rect : rectangles) {
                    g.fillRect(rect.x, rect.y, rect.width, rect.height);
                }
            }
            if(hoveringRectangle != null) {
                //g.fillRect(hoveringRectangle.x, hoveringRectangle.y, hoveringRectangle.width, hoveringRectangle.height);
                g.setColor(editor.getForeground());
                g.fillRect(hoveringRectangle.x, hoveringRectangle.y + hoveringRectangle.height-2, hoveringRectangle.width, 1);
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    private SummarySymbol selectedSymbol = null;
    private boolean selectedDeclaration = false;

    @Override
    public void mouseMoved(MouseEvent e) {
        selectedSymbol = null;

        if(e.isControlDown()) {
            Point point = e.getPoint();
            int hoveredTextIndex = editor.viewToModel(point);

            try {
                int wordStart = editor.getWordStart(hoveredTextIndex);
                int wordEnd = editor.getWordEnd(hoveredTextIndex);
                String word = editor.getText(wordStart, wordEnd-wordStart);
                PrismarineSummaryModule lastSuccessfulSummary = ((SuggestionDialog) editor.getSuggestionInterface()).getLastSuccessfulSummary();
                if(lastSuccessfulSummary != null && lastSuccessfulSummary.getParentSummary() != null) {
                    lastSuccessfulSummary = lastSuccessfulSummary.getParentSummary().getSummaryForLocation(lastSuccessfulSummary.getFileLocation());
                }
                if(lastSuccessfulSummary != null) {
                    SummarySymbol selectedSymbol = lastSuccessfulSummary.getSymbolForName(word, wordStart);
                    if(selectedSymbol != null) {
                        for(PrismarineSummaryModule.SymbolUsage usage : lastSuccessfulSummary.getSymbolUsages()) {
                            if(!usage.symbolName.equals(word)) continue; //filter out symbols that aren't the selected symbol's name

                            StringBounds bounds = usage.pattern.getStringBounds();

                            SummarySymbol thisUsageSymbol = lastSuccessfulSummary.getSymbolForName(word, bounds.start.index);

                            if(thisUsageSymbol != selectedSymbol) continue; //filter out identifiers that don't refer to the same symbol

                            if(bounds.start.index == wordStart && bounds.end.index == wordEnd && selectedSymbol.getDeclarationPattern() != null) { //If this usage refers to the identifier that's currently selected...
                                this.selectedSymbol = selectedSymbol;
                                selectedDeclaration = bounds.start.index == selectedSymbol.getDeclarationPattern().getStringLocation().index;
                                hoveringRectangle = EditorSelectionPainter.getRectanglesForBounds(editor, usage.pattern.getStringBounds()).stream().findFirst().get();
                                editor.repaint();
                            }
                        }
                    }
                }
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }

        if(hoveringRectangle != null && selectedSymbol == null) {
            hoveringRectangle = null;
            editor.repaint();
        }

        if(selectedSymbol != null) {
            editor.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            editor.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if(e.isControlDown()) {
            mouseMoved(e);
            if(selectedSymbol != null) {
                if(selectedDeclaration) {
                    //Looking at declaration
                    ArrayList<PrismarineSummaryModule.SymbolUsage> selectedSymbolUsages = new ArrayList<>();

                    PrismarineSummaryModule lastSuccessfulSummary = (PrismarineSummaryModule) ((SuggestionDialog) editor.getSuggestionInterface()).getLastSuccessfulSummary();
                    if(lastSuccessfulSummary != null && lastSuccessfulSummary.getParentSummary() != null) {
                        lastSuccessfulSummary = lastSuccessfulSummary.getParentSummary().getSummaryForLocation(lastSuccessfulSummary.getFileLocation());
                    }

                    if(lastSuccessfulSummary.getParentSummary() != null) {
                        for(PrismarineSummaryModule fileSummary : lastSuccessfulSummary.getParentSummary().getAllSummaries()) {
                            collectSelectedSymbolUsages(selectedSymbolUsages, fileSummary);
                        }
                    } else {
                        collectSelectedSymbolUsages(selectedSymbolUsages, lastSuccessfulSummary);
                    }

                    navigateToUsages(selectedSymbolUsages);
                } else {
                    //Looking at usage
                    StringBounds bounds = selectedSymbol.getDeclarationPattern().getStringBounds();
                    TokenSource source = selectedSymbol.getDeclarationPattern().getSource();
                    File exactFile = source.getExactFile();
                    if(exactFile == null) {
                        GuardianWindow.showPopupMessage("Symbol '" + selectedSymbol.getName() + "' is native, or its declaration can't be found in any file");
                    } else {
                        GuardianWindow.tabManager.openTab(new FileModuleToken(exactFile), bounds.start.index, bounds.end.index-bounds.start.index);
                    }
                }
                e.consume();
            }
        }
    }

    private void navigateToUsages(ArrayList<PrismarineSummaryModule.SymbolUsage> selectedSymbolUsages) {
        if(selectedSymbolUsages.size() == 0) {
            GuardianWindow.showPopupMessage("No usages found");
        } else if(selectedSymbolUsages.size() == 1) {
            TokenPattern<?> pattern = selectedSymbolUsages.get(0).pattern;
            StringBounds bounds = pattern.getStringBounds();
            File exactFile = pattern.getSource().getExactFile();
            if(exactFile != null) {
                GuardianWindow.tabManager.openTab(new FileModuleToken(exactFile), bounds.start.index, bounds.end.index-bounds.start.index);
            }
        } else {
            FindResults results = new FindResults();

            for(PrismarineSummaryModule.SymbolUsage usage : selectedSymbolUsages) {
                File file = usage.pattern.getSource().getExactFile();
                if(file != null) {
                    StringBounds bounds = usage.pattern.getStringBounds();
                    FileOccurrence fileOccurrence = new FileOccurrence(file, bounds.start.index, bounds.end.index-bounds.start.index, bounds.start.line, usage.pattern.flatten(false), 0);
                    results.insertResult(fileOccurrence);
                }
            }

            GuardianWindow.toolBoard.open(GuardianWindow.findBoard);
            GuardianWindow.findBoard.showResults(results);
        }
    }

    private void collectSelectedSymbolUsages(ArrayList<PrismarineSummaryModule.SymbolUsage> selectedSymbolUsages, PrismarineSummaryModule fileSummary) {
        for(PrismarineSummaryModule.SymbolUsage usage : fileSummary.getSymbolUsages()) {
            if(!usage.symbolName.equals(selectedSymbol.getName())) continue; //filter out symbols that aren't the selected symbol's name

            StringBounds bounds = usage.pattern.getStringBounds();
            SummarySymbol thisUsageSymbol = fileSummary.getSymbolForName(selectedSymbol.getName(), bounds.start.index);

            if(thisUsageSymbol == selectedSymbol && usage.pattern != thisUsageSymbol.getDeclarationPattern()) {
                selectedSymbolUsages.add(usage);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
