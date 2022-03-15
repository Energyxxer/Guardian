package com.energyxxer.xswing;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;

public class XList<T> extends JPanel {

	private final ArrayList<T> options = new ArrayList<>();
	
	protected int selected = 0;

	ComponentStyle normalStyle = new ComponentStyle();
	ComponentStyle rolloverStyle = new ComponentStyle(null);
	ComponentStyle selectedStyle = new ComponentStyle(null);
	
	private final ArrayList<ListSelectionListener> listSelectionListeners = new ArrayList<>();

	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	protected XList() {
	}
	
	public XList(T[] options) {
		setOptions(options);
	}

	public void setOptions(T[] options) {
		this.options.clear();
		this.options.addAll(Arrays.asList(options));
		updateChildren();
	}
	
	private void updateChildren() {
		this.removeAll();
		
		for(int i = 0; i < options.size(); i++) {
			T o = options.get(i);
			XListItem option = new XListItem(o.toString(), i, this);
			
			normalStyle.applyStyle(option);
			
			option.setPreferredSize(null);
			option.setMinimumSize(new ScalableDimension(this.getWidth(),option.getPreferredSize().height));
			option.setMaximumSize(new ScalableDimension(this.getWidth(),option.getPreferredSize().height));
			
			this.add(option);
		}
	}

	private int lastPaintedWidth = -1;
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int width = this.getWidth();
		if(width != lastPaintedWidth) {
			for(Component c : this.getComponents()) {
				c.setMinimumSize(new ScalableDimension(width,c.getPreferredSize().height));
				c.setMaximumSize(new ScalableDimension(width,c.getPreferredSize().height));
			}
			revalidate();
			lastPaintedWidth = width;
		}
	}
	
	public void setForeground(Color c) {
		super.setForeground(c);
		setCellForeground(c);
	}

	public void setSelectedOptionIndex(int index) {
		selected = index;
		updateStyle();
	}
	
	private void updateStyle() {
		for(Component c : this.getComponents()) {
			JComponent jc = (JComponent) c;
			normalStyle.applyStyle(jc);
		}
		if(selected >= 0) {
			selectedStyle.applyStyle((JComponent) this.getComponents()[selected]);
		}
		repaint();
	}
	
	public Color getCellBackground() {
		return normalStyle.background;
	}

	public Color getCellForeground() {
		return normalStyle.foreground;
	}

	public Font getCellFont() {
		return normalStyle.font;
	}

	public Border getCellBorder() {
		return normalStyle.border;
	}

	public void setCellBackground(Color cellBackground) {
		this.normalStyle.background = cellBackground;
		updateStyle();
	}

	public void setCellForeground(Color cellForeground) {
		if(this.normalStyle == null) {
			return;
		}
		this.normalStyle.foreground = cellForeground;
		updateStyle();
	}

	public void setCellFont(Font cellFont) {
		this.normalStyle.font = cellFont;
		updateStyle();
	}

	public void setCellBorder(Border cellBorder) {
		this.normalStyle.border = cellBorder;
		updateStyle();
	}

	public Color getRolloverCellBackground() {
		return rolloverStyle.background;
	}

	public Color getRolloverCellForeground() {
		return rolloverStyle.foreground;
	}

	public Font getRolloverCellFont() {
		return rolloverStyle.font;
	}

	public Border getRolloverCellBorder() {
		return rolloverStyle.border;
	}

	public void setRolloverCellBackground(Color rolloverCellBackground) {
		this.rolloverStyle.background = rolloverCellBackground;
		updateStyle();
	}

	public void setRolloverCellForeground(Color rolloverCellForeground) {
		this.rolloverStyle.foreground = rolloverCellForeground;
		updateStyle();
	}

	public void setRolloverCellFont(Font rolloverCellFont) {
		this.rolloverStyle.font = rolloverCellFont;
		updateStyle();
	}

	public void setRolloverCellBorder(Border rolloverCellBorder) {
		this.rolloverStyle.border = rolloverCellBorder;
		updateStyle();
	}

	public Color getSelectedCellBackground() {
		return selectedStyle.background;
	}

	public Color getSelectedCellForeground() {
		return selectedStyle.foreground;
	}

	public Font getSelectedCellFont() {
		return selectedStyle.font;
	}

	public Border getSelectedCellBorder() {
		return selectedStyle.border;
	}

	public void setSelectedCellBackground(Color selectedCellBackground) {
		this.selectedStyle.background = selectedCellBackground;
		updateStyle();
	}

	public void setSelectedCellForeground(Color selectedCellForeground) {
		this.selectedStyle.foreground = selectedCellForeground;
		updateStyle();
	}

	public void setSelectedCellFont(Font selectedCellFont) {
		this.selectedStyle.font = selectedCellFont;
		updateStyle();
	}

	public void setSelectedCellBorder(Border selectedCellBorder) {
		this.selectedStyle.border = selectedCellBorder;
		updateStyle();
	}

	public ComponentStyle getNormalStyle() {
		return normalStyle;
	}

	public ComponentStyle getRolloverStyle() {
		return rolloverStyle;
	}

	public ComponentStyle getSelectedStyle() {
		return selectedStyle;
	}

	public void setNormalStyle(ComponentStyle normalStyle) {
		//System.out.println("SETTING NORMAL STYLE");
		//this.normalStyle = normalStyle;
		updateStyle();
	}

	public void setRolloverStyle(ComponentStyle rolloverStyle) {
		this.rolloverStyle = rolloverStyle;
		updateStyle();
	}

	public void setSelectedStyle(ComponentStyle selectedStyle) {
		this.selectedStyle = selectedStyle;
		updateStyle();
	}
	
	public void addListSelectionListener(ListSelectionListener listener) {
		if(!listSelectionListeners.contains(listener)) listSelectionListeners.add(listener);
	}
	
	public void removeListSelectionListener(ListSelectionListener listener) {
		if(listSelectionListeners.contains(listener)) listSelectionListeners.remove(listener);
	}
	
	protected void registerSelectionChange(int index) {
		if(this.selected != index) {
			for(ListSelectionListener listener : listSelectionListeners) {
				listener.valueChanged(new ListSelectionEvent(options.get(index), index, index, false));
			}
			this.selected = index;
			updateStyle();
			repaint();
		}
	}

	public T getSelected() {
		return options.get(selected);
	}
}

class XListItem extends JPanel implements MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -13039223639668919L;

	JLabel label;
	private int index;
	private XList<?> parent;
	
	XListItem(String labelText, int index, XList<?> parent) {
		super();
		this.index = index;
		this.parent = parent;
		label = new JLabel(labelText);
		label.setVerticalAlignment(SwingConstants.CENTER);
		label.setHorizontalAlignment(SwingConstants.CENTER);

		this.add(label);

		this.addMouseListener(this);
	}
	
	private boolean isSelected() {
		return this.index == parent.selected;
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		parent.registerSelectionChange(index);
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		if(isSelected()) {
			parent.selectedStyle.applyStyle(this);
		} else {
			parent.rolloverStyle.applyStyle(this);
		}
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		if(isSelected()) {
			parent.selectedStyle.applyStyle(this);
		} else {
			parent.normalStyle.applyStyle(this);
		}
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		
	}
	
	
}






