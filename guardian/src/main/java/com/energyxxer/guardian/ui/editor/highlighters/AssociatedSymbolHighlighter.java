package com.energyxxer.guardian.ui.editor.highlighters;

import com.energyxxer.enxlex.lexical_analysis.token.TokenSource;
import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.main.window.sections.tools.find.FileOccurrence;
import com.energyxxer.guardian.main.window.sections.tools.find.FindResults;
import com.energyxxer.guardian.ui.HintStylizer;
import com.energyxxer.guardian.ui.editor.EditorComponent;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.editor.behavior.caret.EditorCaret;
import com.energyxxer.guardian.ui.editor.behavior.caret.EditorSelectionPainter;
import com.energyxxer.guardian.ui.editor.completion.SuggestionDialog;
import com.energyxxer.guardian.ui.modules.FileModuleToken;
import com.energyxxer.guardian.util.ConcurrencyUtil;
import com.energyxxer.prismarine.summaries.PrismarineSummaryModule;
import com.energyxxer.prismarine.summaries.SummarySymbol;
import com.energyxxer.util.StringBounds;
import com.energyxxer.xswing.hints.HTMLHint;
import com.energyxxer.xswing.hints.Hint;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class AssociatedSymbolHighlighter implements Highlighter.HighlightPainter, MouseMotionListener, MouseListener {
    private static final Pattern LINE_ENDING_PATTERN = Pattern.compile("\r?\n");

    private AdvancedEditor editor;

    private boolean shouldRender = false;
    private final ArrayList<Rectangle> rectangles = new ArrayList<>();
    private Rectangle hoveringRectangle = null;

    private int prevSeenDot = -1;

    private HTMLHint docHint = GuardianWindow.hintManager.createHTMLHint("a");

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

        docHint.setPreferredPos(Hint.ABOVE);
        docHint.setInteractive(false);
        docHint.setOutDelay(1);
        docHint.setPadding(6);
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
                PrismarineSummaryModule.SymbolUsage highlightedUsage = lastSuccessfulSummary.getSymbolUsageAtIndex(wordStart);
                if(highlightedUsage == null) return;
                if(!highlightedUsage.symbolName.equals(word)) return;
                SummarySymbol selectedSymbol = highlightedUsage.fetchSymbol(lastSuccessfulSummary);
                if(selectedSymbol == null) return;

                ConcurrencyUtil.runAsync(() -> {
                    ArrayList<StringBounds> boundsToRectangles = new ArrayList<>();
                    for(PrismarineSummaryModule.SymbolUsage usage : lastSuccessfulSummary.getSymbolUsages()) {
                        if(!usage.symbolName.equals(word)) continue; //filter out symbols that aren't the selected symbol's name

                        StringBounds bounds = usage.bounds;

                        SummarySymbol thisUsageSymbol = usage.fetchSymbol(lastSuccessfulSummary);

                        if(thisUsageSymbol != selectedSymbol) continue; //filter out identifiers that don't refer to the same symbol

                        if(bounds.start.index == wordStart && bounds.end.index == wordEnd) { //If this usage refers to the identifier that's currently selected...
                            shouldRender = true;
                        }
                        boundsToRectangles.add(bounds);
                    }
                    SwingUtilities.invokeLater(() -> {
                        for(StringBounds bounds : boundsToRectangles) {
                            try {
                                for(Rectangle rect : EditorSelectionPainter.getRectanglesForBounds(editor, bounds)) {
                                    if(rect != null) rectangles.add(rect);
                                }
                            } catch (BadLocationException ignore) {}
                        }
                        editor.repaint();
                    });
                });
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
        SummarySymbol oldSelectedSymbol = this.selectedSymbol;
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
                    PrismarineSummaryModule.SymbolUsage highlightedUsage = lastSuccessfulSummary.getSymbolUsageAtIndex(wordStart);
                    if(highlightedUsage != null && highlightedUsage.symbolName.equals(word)) {
                        SummarySymbol selectedSymbol = highlightedUsage.fetchSymbol(lastSuccessfulSummary);
                        if(selectedSymbol != null) {
                            if(oldSelectedSymbol != selectedSymbol) {
                                for(PrismarineSummaryModule.SymbolUsage usage : lastSuccessfulSummary.getSymbolUsages()) {
                                    if(!usage.symbolName.equals(word)) continue; //filter out symbols that aren't the selected symbol's name

                                    StringBounds bounds = usage.bounds;

                                    SummarySymbol thisUsageSymbol = usage.fetchSymbol(lastSuccessfulSummary);

                                    if(thisUsageSymbol != selectedSymbol) continue; //filter out identifiers that don't refer to the same symbol

                                    if(bounds.start.index == wordStart && bounds.end.index == wordEnd && selectedSymbol.getStringBounds() != null) { //If this usage refers to the identifier that's currently selected...
                                        this.selectedSymbol = selectedSymbol;
                                        selectedDeclaration = bounds.start.index == selectedSymbol.getStringBounds().start.index;
                                        hoveringRectangle = EditorSelectionPainter.getRectanglesForBounds(editor, usage.bounds)[0];
                                        editor.repaint();
                                        break;
                                    }
                                }

                                String documentation = ((EditorComponent) editor).getParentModule().getLanguage().formatDocumentation(selectedSymbol, lastSuccessfulSummary);

                                if(documentation != null) {
                                    Rectangle highlightedRectangle = EditorSelectionPainter.getRectanglesForBounds(editor, highlightedUsage.bounds)[0];
                                    HintStylizer.style(docHint);
                                    docHint.setText(documentation);
                                    docHint.show(
                                            new Point(
                                                    editor.getLocationOnScreen().x + highlightedRectangle.x + highlightedRectangle.width/2,
                                                    editor.getLocationOnScreen().y + highlightedRectangle.y
                                            ),
                                            () -> editor.isShowing() && this.selectedSymbol == selectedSymbol
                                    );
                                } else {
                                    docHint.dismiss();
                                }
                            } else {
                                this.selectedSymbol = oldSelectedSymbol;
                            }
                        } else {
                            docHint.dismiss();
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

                    PrismarineSummaryModule lastSuccessfulSummary = ((SuggestionDialog) editor.getSuggestionInterface()).getLastSuccessfulSummary();
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
                    StringBounds bounds = selectedSymbol.getStringBounds();
                    TokenSource source = selectedSymbol.getSource();
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

    private HashMap<File, String> cachedFileContents = new HashMap<>();

    private void navigateToUsages(ArrayList<PrismarineSummaryModule.SymbolUsage> selectedSymbolUsages) {
        if(selectedSymbolUsages.size() == 0) {
            GuardianWindow.showPopupMessage("No usages found");
        } else if(selectedSymbolUsages.size() == 1) {
            StringBounds bounds = selectedSymbolUsages.get(0).bounds;
            File exactFile = selectedSymbolUsages.get(0).tokenSource.getExactFile();
            if(exactFile != null) {
                GuardianWindow.tabManager.openTab(new FileModuleToken(exactFile), bounds.start.index, bounds.end.index-bounds.start.index);
            }
        } else {
            FindResults results = new FindResults();

            for(PrismarineSummaryModule.SymbolUsage usage : selectedSymbolUsages) {
                File file = usage.tokenSource.getExactFile();
                if(file != null) {
                    StringBounds bounds = usage.bounds;
                    try {
                        String contents;
                        if(cachedFileContents.containsKey(file)) {
                            contents = cachedFileContents.get(file);
                        } else {
                            contents = new String(Files.readAllBytes(file.toPath()), Guardian.DEFAULT_CHARSET);
                            //remove carriage returns
                            contents = LINE_ENDING_PATTERN.matcher(contents).replaceAll("\n");

                            cachedFileContents.put(file, contents);
                        }

                        int lineStart = bounds.start.index - bounds.start.column;
                        int lineEnd = contents.indexOf('\n', bounds.start.index);
                        if(lineEnd == -1) lineEnd = bounds.end.index;
                        FileOccurrence fileOccurrence = new FileOccurrence(file, bounds.start.index, bounds.end.index-bounds.start.index, bounds.start.line, contents.substring(lineStart, lineEnd), bounds.start.index-lineStart);
                        results.insertResult(fileOccurrence);
                    } catch (IOException x) {
                        FileOccurrence fileOccurrence = new FileOccurrence(file, bounds.start.index, bounds.end.index-bounds.start.index, bounds.start.line, usage.symbolName, 0);
                        results.insertResult(fileOccurrence);
                    }
                }
            }

            GuardianWindow.toolBoard.open(GuardianWindow.findBoard);
            GuardianWindow.findBoard.showResults(results);
        }
        cachedFileContents.clear();
    }

    private void collectSelectedSymbolUsages(ArrayList<PrismarineSummaryModule.SymbolUsage> selectedSymbolUsages, PrismarineSummaryModule fileSummary) {
        for(PrismarineSummaryModule.SymbolUsage usage : fileSummary.getSymbolUsages()) {
            if(!usage.symbolName.equals(selectedSymbol.getName())) continue; //filter out symbols that aren't the selected symbol's name

            SummarySymbol thisUsageSymbol = usage.fetchSymbol(fileSummary);

            if(thisUsageSymbol == selectedSymbol && !(usage.tokenSource == thisUsageSymbol.getSource() && thisUsageSymbol.getStringBounds().equals(usage.bounds))) {
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
