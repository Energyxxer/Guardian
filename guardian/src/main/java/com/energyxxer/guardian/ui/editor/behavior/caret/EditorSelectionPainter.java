package com.energyxxer.guardian.ui.editor.behavior.caret;

import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.util.StringBounds;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.List;

import static com.energyxxer.guardian.ui.editor.behavior.caret.DragSelectMode.CHAR;
import static com.energyxxer.guardian.ui.editor.behavior.caret.DragSelectMode.RECTANGLE;

/**
 * Created by User on 1/9/2017.
 */
public class EditorSelectionPainter implements Highlighter.HighlightPainter {

    private EditorCaret caret;

    public EditorSelectionPainter(EditorCaret caret) {
        this.caret = caret;
    }

    private static Rectangle absWidth(Rectangle rect) {
        if(rect.width < 0) {
            rect.x += rect.width;
            rect.width *= -1;
        }
        rect.width = Math.abs(rect.width);

        return rect;
    }

    private static Rectangle[] tempRectangles = new Rectangle[3];

    public static Rectangle[] getRectanglesForBounds(AdvancedEditor editor, StringBounds bounds) throws BadLocationException {
        tempRectangles[0] = tempRectangles[1] = tempRectangles[2] = null;

        Rectangle firstRect = editor.modelToView(bounds.start.index);
        Rectangle midRect = new Rectangle();
        Rectangle lastRect = editor.modelToView(bounds.end.index);

        if(firstRect.y == lastRect.y) {
            // one rectangle, just connect the first and last
            midRect.x = Math.min(firstRect.x, lastRect.x);
            midRect.width = Math.abs(firstRect.x - lastRect.x);
            midRect.y = firstRect.y;
            midRect.height = firstRect.height;

            absWidth(midRect);
            tempRectangles[0] = midRect;
        } else {
            // two or three rectangles
            // Finish the first line
            firstRect.width = editor.getWidth() - firstRect.x;

            absWidth(firstRect);
            tempRectangles[0] = firstRect;

            // Set middle rectangle's top to the bottom of the first pseudo-line
            midRect.y = firstRect.y + firstRect.height;

            // Finish the last line
            lastRect.width = lastRect.x - editor.modelToView(0).x;
            lastRect.x = editor.modelToView(0).x; //0;

            absWidth(lastRect);
            tempRectangles[1] = lastRect;

            // Set middle rectangle's height to that between the top of the last pseudo-line and the bottom of the first
            midRect.height = lastRect.y - midRect.y;

            // Set middle rectangle's height to the width of the editor
            midRect.x = lastRect.x;
            midRect.width = editor.getWidth() - midRect.x;

            // Add middle rectangle if not empty
            if(midRect.height != 0) {
                absWidth(midRect);
                tempRectangles[2] = midRect;
            }
        }
        return tempRectangles;
    }

    @Override
    public void paint(Graphics g, int p0, int p1, Shape graphicBounds, JTextComponent c) {
        AdvancedEditor editor = (AdvancedEditor) c;
        g.setColor(editor.hasFocus() ? editor.getSelectionColor() : editor.getSelectionUnfocusedColor());

        List<Dot> dots = caret.getDots();

        int dotIndex = 0;
        for(Dot dot : dots) {
            boolean shouldPaint = !(caret.dragSelectMode == RECTANGLE && dot == caret.bufferedDot) && !(caret.dragSelectMode == CHAR && dotIndex >= caret.rectangleDotsStartIndex);
            if(shouldPaint) try {
                StringBounds bounds = dot.getBounds();

                for(Rectangle rectangle : getRectanglesForBounds(editor, bounds)) {
                    if(rectangle != null) {
                        g.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
                    }
                }

                dotIndex++;
            } catch (BadLocationException e) {
                //Can't render
            }
        }
    }
}
