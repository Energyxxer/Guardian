package com.energyxxer.guardian.ui.explorer.base;

import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.HintStylizer;
import com.energyxxer.guardian.ui.explorer.base.elements.ExplorerElement;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.xswing.ScalableDimension;
import com.energyxxer.xswing.ScalableGraphics2D;
import com.energyxxer.xswing.hints.TextHint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;
import java.util.*;

import static com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor.isPlatformControlDown;
import static com.energyxxer.xswing.ScalableDimension.descaleEvent;

/**
 * Created by User on 2/7/2017.
 */
public class ExplorerMaster extends JComponent implements MouseListener, MouseMotionListener, StyleProvider {
    protected HashMap<ExplorerFlag, Boolean> explorerFlags = new HashMap<>();

    protected final List<ExplorerElement> children = Collections.synchronizedList(new ArrayList<>());
    protected final List<ExplorerElement> selectedItems = Collections.synchronizedList(new ArrayList<>());

    protected ExplorerElement rolloverItem = null;

    protected MouseEvent pressedEvent = null;
    protected ExplorerElement dragStart = null;
    protected boolean pressConsumed = false;
    protected boolean transferStarted = false;

    private final ArrayList<ModuleToken> expandedElements = new ArrayList<>();

    protected final ArrayList<ExplorerElement> flatList = new ArrayList<>();
    private int contentWidth = 0;
    private int offsetY = 0;

    protected HashMap<String, Color> colors = new HashMap<>();
    protected HashMap<String, Integer> styleNumbers = new HashMap<>();
    protected HashMap<String, Image> assets = new HashMap<>();
    protected HashMap<String, Font> fonts = new HashMap<>();

    protected int rowHeight = 20;
    protected int indentPerLevel = 20;
    protected int initialIndent = 0;
    protected String selectionStyle = "FULL";
    protected int selectionLineThickness = 2;

    private int indentation;

    private boolean multipleSelectionsEnabled = true;
    private boolean singleClickInteractAllowed = false;

    public ExplorerMaster() {
        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        explorerFlags.put(ExplorerFlag.DYNAMIC_ROW_HEIGHT, false);
        explorerFlags.put(ExplorerFlag.DEBUG_WIDTH, false);

    }

    public void refresh() {}

    private final ArrayList<ExplorerElement> renderingChildren = new ArrayList<>();

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        contentWidth = 0;
        offsetY = 0;
        indentation = 0;
        flatList.clear();
        g.setColor(colors.get("background"));
        g.fillRect(0,0,this.getWidth(), this.getHeight());

        g = new ScalableGraphics2D(g);

        if(this.getParent() instanceof JViewport) {
//            Debug.log(((JViewport) this.getParent()).getViewRect());
            Rectangle viewRect = ((JViewport) this.getParent()).getViewRect();
            g.clipRect(viewRect.x, viewRect.y, viewRect.width, viewRect.height);
        }

        renderingChildren.clear();
        renderingChildren.addAll(children);

        for(ExplorerElement i : renderingChildren) {
            i.render(g);
        }

        Dimension newSize = new ScalableDimension(contentWidth, offsetY);
        if(!newSize.equals(this.getPreferredSize())) {
            this.setPreferredSize(newSize);
            this.getParent().revalidate();
        }

        for(Runnable r : scheduledForNextPaint) {
            SwingUtilities.invokeLater(r);
        }
        scheduledForNextPaint.clear();
    }

    @Override
    public void repaint() {
        if(this.getParent() instanceof JViewport && this.getParent().getParent() instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) this.getParent().getParent();
            scrollPane.repaint();
        } else super.repaint();
    }

    @Override
    public void repaint(Rectangle r) {
        if(this.getParent() instanceof JViewport && this.getParent().getParent() instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) this.getParent().getParent();
            if(r != null && r.equals(getVisibleRect())) {
                scrollPane.repaint(scrollPane.getVisibleRect());
            } else {
                scrollPane.repaint();
            }
        } else super.repaint(r);
    }

    protected ExplorerElement getElementAtMousePos(MouseEvent e) {
        if(e == null) return null;
        return getElementAtMousePos(e.getPoint());
    }

    protected ExplorerElement getElementAtMousePos(Point e) {
        if(getFlag(ExplorerFlag.DYNAMIC_ROW_HEIGHT)) {
            int y = 0;
            for(ExplorerElement element : flatList) {
                if(e.y >= y && e.y < y + element.getHeight()) return element;
                y += element.getHeight();
            }
            return null;
        } else {
            int index = e.y / rowHeight;
            if(index >= 0 && index < flatList.size()) {
                return flatList.get(index);
            }
            return null;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        e = descaleEvent(e);
        ExplorerElement element = getElementAtMousePos(e);
        if(element != null) element.mouseClicked(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        e = descaleEvent(e);
        this.requestFocus();
        ExplorerElement element = getElementAtMousePos(e);
        dragStart = element;
        pressedEvent = e;
        if(element != null) element.mousePressed(e);
        else {
            clearSelected();
            repaint();
        }
        transferStarted = false;
        pressConsumed = e.isConsumed();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        e = descaleEvent(e);
        if(dragStart != null) dragStart.mouseReleased(e);
        dragStart = null;
        pressedEvent = null;
        transferStarted = false;
        pressConsumed = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        e = descaleEvent(e);
        if(rolloverItem != null) {
            rolloverItem.setRollover(false);
            repaint();
        }
        rolloverText = null;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        e = descaleEvent(e);
        if(dragStart != null) {
            dragStart.mouseDragged(e);
        }
    }

    private String rolloverText = null;
    private TextHint hint = GuardianWindow.hintManager.createTextHint("a");

    @Override
    public void mouseMoved(MouseEvent e) {
        e = descaleEvent(e);
        ExplorerElement element = getElementAtMousePos(e);
        if(rolloverItem != null) {
            rolloverItem.setRollover(false);
            if(rolloverItem != element) {
                rolloverItem.mouseExited(e);
                repaint();
            }
        }
        if(element != null) {
            element.setRollover(true);
            if(rolloverItem != element) {
                repaint();
                element.mouseEntered(e);
            }
            String text = element.getToolTipText();
            this.rolloverText = text;
            if(text != null && (rolloverItem != null || !hint.isShowing()) && (!Objects.equals(hint.getText(), text) || !hint.isShowing())) {
                hint.setText(text);
                HintStylizer.style(hint);
                hint.show(new Point(this.getLocationOnScreen().x + element.getToolTipLocation().x, this.getLocationOnScreen().y + element.getLastRecordedOffset() + element.getToolTipLocation().y), () -> this.isShowing() && rolloverItem == element && Objects.equals(hint.getText(), rolloverText));
            }
            element.mouseMoved(e);
        } else {
            rolloverText = null;
        }
        GuardianWindow.repaintQueue.queueRepaint(this);
        rolloverItem = element;
    }

    public void clearSelected() {
        for(ExplorerElement item : selectedItems) {
            item.setSelected(false);
        }
        selectedItems.clear();
        selectionUpdated();
        repaint();
    }

    protected void selectionUpdated() {}

    public void addSelected(ExplorerElement item) {
        this.addSelected(item, true);
    }

    public void addSelected(ExplorerElement item, boolean invert) {
        if(multipleSelectionsEnabled) {
            if(!selectedItems.contains(item)) {
                item.setSelected(true);
                selectedItems.add(item);
            } else if(invert) {
                item.setSelected(false);
                selectedItems.remove(item);
            }
        } else {
            clearSelected();
            item.setSelected(true);
            selectedItems.add(item);
        }
        selectionUpdated();
    }

    protected void ensureLast(ExplorerElement item) {
        if(selectedItems.contains(item)) {
            selectedItems.remove(item);
            selectedItems.add(item);
            selectionUpdated();
        }
    }

    public void setSelected(ExplorerElement item, MouseEvent e) {
        ExplorerElement lastItem = null;
        if(this.selectedItems.size() > 0) lastItem = this.selectedItems.get(this.selectedItems.size()-1);

        MouseEvent firstE = e != null && e.getID() == MouseEvent.MOUSE_RELEASED ? pressedEvent : e;
        int id = e != null ? e.getID() : MouseEvent.MOUSE_PRESSED;
        boolean ctrl = firstE != null && isPlatformControlDown(firstE);
        boolean shift = firstE != null && firstE.isShiftDown();

        if(ctrl && !shift && e.getID() == MouseEvent.MOUSE_PRESSED) return;
        if(shift && e.getID() == MouseEvent.MOUSE_RELEASED) return;

        ExplorerElement lookingAt = getElementAtMousePos(firstE);
        boolean lookingAtSelected = lookingAt != null && selectedItems.contains(lookingAt);

        if(!ctrl && (shift || (!lookingAtSelected || id == MouseEvent.MOUSE_RELEASED))) {
            clearSelected();
        }

        if(shift && id == MouseEvent.MOUSE_PRESSED && lastItem != null) {
            int startIndex = flatList.indexOf(lastItem);
            int endIndex = flatList.indexOf(item);

            int start = Math.min(startIndex, endIndex);
            int end = Math.max(startIndex, endIndex);
            for(int i = start; i <= end; i++) {
                addSelected(flatList.get(i), false);
            }
            ensureLast(lastItem);
        } else {
            addSelected(item, ctrl);
            ensureLast(item);
        }
        repaint();
        selectionUpdated();
    }

    private final List<ModuleToken> selectedTokensOut = Collections.synchronizedList(new ArrayList<>());

    public List<ModuleToken> getSelectedTokens() {
        selectedTokensOut.clear();
        selectedItems.forEach(item -> {
            ModuleToken path = item.getToken();
            if(path != null) selectedTokensOut.add(path);
        });
        return selectedTokensOut;
    }

    public List<ExplorerElement> getSelectedItems() {
        return selectedItems;
    }

    public void triggerDragStart(MouseEvent e) {
        if(transferStarted) return;
        transferStarted = true;
        TransferHandler th = this.getTransferHandler();
        if(th != null) {
            th.exportAsDrag(this, e, TransferHandler.MOVE);
        }
    }

    public boolean getFlag(ExplorerFlag flag) {
        return this.explorerFlags.get(flag);
    }

    public void toggleFlag(ExplorerFlag flag) {
        this.explorerFlags.put(flag, !getFlag(flag));
    }

    public ArrayList<ExplorerElement> getFlatList() {
        return flatList;
    }

    public HashMap<String, Color> getColors() {
        return colors;
    }

    @Override
    public Map<String, Integer> getStyleNumbers() {
        return styleNumbers;
    }

    public HashMap<String, Image> getAssetMap() {
        return assets;
    }

    public int getRowHeight() {
        return rowHeight;
    }

    public void setRowHeight(int rowHeight) {
        this.rowHeight = rowHeight;
    }

    public int getIndentPerLevel() {
        return indentPerLevel;
    }

    public void setIndentPerLevel(int indentPerLevel) {
        this.indentPerLevel = indentPerLevel;
    }

    public int getInitialIndent() {
        return initialIndent;
    }

    public void setInitialIndent(int initialIndent) {
        this.initialIndent = initialIndent;
    }

    public String getSelectionStyle() {
        return selectionStyle;
    }

    public void setSelectionStyle(String selectionStyle) {
        this.selectionStyle = selectionStyle;
    }

    public int getSelectionLineThickness() {
        return selectionLineThickness;
    }

    public void setSelectionLineThickness(int selectionLineThickness) {
        this.selectionLineThickness = selectionLineThickness;
    }

    public int getContentWidth() {
        return contentWidth;
    }

    public void setContentWidth(int contentWidth) {
        this.contentWidth = contentWidth;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public ArrayList<ModuleToken> getExpandedElements() {
        return expandedElements;
    }

    public List<ExplorerElement> getChildren() {
        return children;
    }

    public List<ExplorerElement> getAllElements() {
        return children;
    }

    public int getIndentation() {
        return indentation;
    }

    public void setIndentation(int indentation) {
        this.indentation = indentation;
    }

    public void pushIndentation() {
        this.indentation++;
    }

    public void popIndentation() {
        this.indentation--;
    }

    public void addToFlatList(ExplorerElement element) {
        this.flatList.add(element);
    }

    private ArrayList<Runnable> scheduledForNextPaint = new ArrayList<>();

    public void scheduleAfterNextPaint(Runnable r) {
        scheduledForNextPaint.add(r);
    }

    public void renderOffset(int height) {
        offsetY += height;
    }

    public boolean isMultipleSelectionsEnabled() {
        return multipleSelectionsEnabled;
    }

    public void setMultipleSelectionsEnabled(boolean multipleSelectionsEnabled) {
        this.multipleSelectionsEnabled = multipleSelectionsEnabled;
    }

    public boolean isSingleClickInteractAllowed() {
        return singleClickInteractAllowed;
    }

    public void setSingleClickInteractAllowed(boolean singleClickInteractAllowed) {
        this.singleClickInteractAllowed = singleClickInteractAllowed;
    }
}