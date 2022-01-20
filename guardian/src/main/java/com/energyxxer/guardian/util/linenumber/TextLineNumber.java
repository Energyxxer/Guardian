package com.energyxxer.guardian.util.linenumber;

import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.util.Disposable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

public class TextLineNumber extends JPanel
		implements CaretListener, DocumentListener, AdjustmentListener, Disposable
{
	public static final float LEFT = 0.0f;
	public static final float CENTER = 0.5f;
	public static final float RIGHT = 1.0f;

	private final int HEIGHT = getPreferredSize().height;

	private AdvancedEditor component;
	private JScrollPane scrollPane;

	private Color currentLineForeground = null;
	private Border customBorder = new EmptyBorder(0,0,0,0);
	private float digitAlignment;
	private int minimumDigits;
	private int padding;

	private int lastDigits = 0;
	private int lastHeight = 0;
	private int lastLine = -1;

	private int zeroWidth = 4;

	public TextLineNumber(AdvancedEditor component, JScrollPane scrollPane) {
		this(component, scrollPane, 3);
	}

	public TextLineNumber(AdvancedEditor component, JScrollPane scrollPane, int minimumDigits) {
		this.component = component;
		this.scrollPane = scrollPane;
		this.minimumDigits = minimumDigits;
		digitAlignment = RIGHT;
		setPadding(10);
		component.getDocument().addDocumentListener(this);
		component.addCaretListener( this );
		component.addCaretPaintListener(this::repaint);
		scrollPane.getVerticalScrollBar().addAdjustmentListener(this);
		setOpaque(false);
	}

	public void setPadding(int padding) {
		this.padding = padding;
		updateWidth();
	}

	private int getLineCount() {
		Element root = component.getDocument().getDefaultRootElement();
		return root.getElementCount();
	}

	private void updateWidth() {

		int digits = Math.max(String.valueOf(getLineCount()).length(), minimumDigits);

		if(digits != lastDigits) {
			int width = padding + (zeroWidth * digits) + padding;

			setPreferredSize(new Dimension(width, 0));

			lastDigits = digits;
		}
	}

	public void setCurrentLineForeground(Color currentLineForeground) {
		this.currentLineForeground = currentLineForeground;
	}

	private Color getCurrentLineForeground() {
		return (currentLineForeground != null) ? currentLineForeground : getForeground();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.setColor(getBackground());
		g.fillRect(0,0,this.getWidth(),this.getHeight());

		try {
			g.setFont(component.getFont());
			FontMetrics fontMetrics = g.getFontMetrics();
			int currentZeroWidth = fontMetrics.charWidth('0');
			if(zeroWidth != currentZeroWidth) {
				lastDigits = -1;
				zeroWidth = currentZeroWidth;
				updateWidth();
				revalidate();
				repaint();
				return;
			}
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			Rectangle mtvZero = component.modelToView(0);
			if(mtvZero == null) return;
			int lineHeight = mtvZero.height;
			int availableWidth = getSize().width - (2 * padding);

			Rectangle viewport = scrollPane.getViewport().getViewRect();

			int caretPosition = component.getLocationForOffset(component.getCaretPosition()).line;

			int start = component.viewToModel(new Point(0, viewport.y));
			int maxLength = component.getDocument().getLength();
			int prevIndex = -1;
			int y = -(viewport.y % lineHeight) - fontMetrics.getDescent();
			int prevLine = -1;

			for(int currentIndex = start; currentIndex <= maxLength; currentIndex = component.viewToModel(new Point(0, viewport.y + y + lineHeight))) {
				if(currentIndex == prevIndex) break;

				int line = getLineNumberFor(currentIndex);
				String label = String.valueOf(line);
				int stringWidth = fontMetrics.stringWidth(label);
				int x = getOffsetX(availableWidth, stringWidth) + padding;
				y += lineHeight;

				if(line == caretPosition) {
					g.setColor(getCurrentLineForeground());
				} else {
					g.setColor(getForeground());
				}

				if(line != prevLine) g.drawString(String.valueOf(line), x, y);
				prevLine = line;

				prevIndex = currentIndex;
				if(y >= viewport.height) break;
			}

		} catch(BadLocationException e) {
			e.printStackTrace();
		}
	}

	private boolean isCurrentLine(int rowStartOffset)
	{
		int caretPosition = component.getCaretPosition();
		Element root = component.getDocument().getDefaultRootElement();

		return root.getElementIndex(rowStartOffset) == root.getElementIndex(caretPosition);
	}

	protected int getLineNumberFor(int offset) {
		return component.getLocationForOffset(offset).line;
	}

	private int getOffsetX(int availableWidth, int stringWidth)
	{
		return (int)((availableWidth - stringWidth) * digitAlignment);
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		//  Get the line the caret is positioned on

		int caretPosition = component.getCaretPosition();
		Element root = component.getDocument().getDefaultRootElement();
		int currentLine = root.getElementIndex( caretPosition );

		//  Need to repaint so the correct line number can be highlighted

		if (lastLine != currentLine)
		{
			documentChanged();
			lastLine = currentLine;
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		documentChanged();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		documentChanged();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {

	}

	private void documentChanged()
	{
		//  View of the component has not been updated at the time
		//  the DocumentEvent is fired
		updateWidth();
		repaint();/*
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void start()
			{
				try
				{
					int endPos = component.getDocument().getLength();
					Rectangle rect = component.modelToView(endPos);

					if (rect != null && rect.y != lastHeight)
					{
						updateWidth();
						repaint();
						lastHeight = rect.y;
					}
				}
				catch (BadLocationException ex) {  nothing to do  }
			}
		});*/
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		documentChanged();
	}

	@Override
	public void dispose() {
		component = null;
	}
}