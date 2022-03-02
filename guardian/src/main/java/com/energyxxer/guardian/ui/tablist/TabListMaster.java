package com.energyxxer.guardian.ui.tablist;

import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.HintStylizer;
import com.energyxxer.guardian.ui.Tab;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.ScalableDimension;
import com.energyxxer.xswing.ScalableGraphics2D;
import com.energyxxer.xswing.hints.TextHint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;

import static com.energyxxer.xswing.ScalableDimension.descaleEvent;

public class TabListMaster extends JComponent implements MouseListener, MouseMotionListener {
    final ArrayList<TabListElement> children = new ArrayList<>();
    private int x = 0;

    private TabListElement rolloverElement = null;
    private TabListElement selectedElement = null;

    private HashMap<String, Color> colors = new HashMap<>();
    private String selectionStyle = "FULL";
    private int selectionLineThickness = 2;

    Point dragPoint = null;
    float dragPivot = -1;
    TabListElement draggedElement = null;
    int height = 5;

    private TextHint hint = GuardianWindow.hintManager.createTextHint("a");

    private ThemeListenerManager tlm = new ThemeListenerManager();

    private boolean mayRearrange = true;

    public TabListMaster() {
        this(null);
    }

    public TabListMaster(String namespace) {
        if(namespace != null) {
            tlm.addThemeChangeListener(t -> {
                colors.put("background",t.getColor(Color.WHITE, namespace+".background","TabList.background"));
                colors.put("tab.background",t.getColor(new Color(0,0,0,0), namespace+".tab.background","TabList.tab.background"));
                colors.put("tab.foreground",t.getColor(Color.BLACK, namespace+".tab.foreground","TabList.tab.foreground","General.foreground"));
                colors.put("tab.close.color",t.getColor(Color.DARK_GRAY, namespace+".tab.close.color","TabList.tab.close.color"));
                colors.put("tab.close.rollover.color",t.getColor(Color.LIGHT_GRAY, namespace+".tab.close.hover.color","TabList.tab.close.hover.color"));
                colors.put("tab.selected.background",t.getColor(Color.BLUE, namespace+".tab.selected.background","TabList.tab.selected.background",namespace+".tab.background","TabList.tab.background"));
                colors.put("tab.selected.foreground",t.getColor(Color.BLACK, namespace+".tab.selected.foreground","TabList.tab.selected.foreground",namespace+".tab.hover.foreground","TabList.tab.hover.foreground",namespace+".tab.foreground","TabList.tab.foreground","General.foreground"));
                colors.put("tab.rollover.background",t.getColor(new Color(0,0,0,0), namespace+".tab.hover.background","TabList.tab.hover.background",namespace+".tab.background","TabList.tab.background"));
                colors.put("tab.rollover.foreground",t.getColor(Color.BLACK, namespace+".tab.hover.foreground","TabList.tab.hover.foreground",namespace+".tab.foreground","TabList.tab.foreground","General.foreground"));

                selectionStyle = t.getString(namespace+".tab.selectionStyle","TabList.tab.selectionStyle","default:FULL");
                selectionLineThickness = Math.max(t.getInteger(2,namespace+".tab.selectionLineThickness","TabList.tab.selectionLineThickness"), 0);
                height = Math.max(t.getInteger(5,namespace+".height","TabList.height"),5);
                this.setMinimumSize(new ScalableDimension(1, height));
                this.setPreferredSize(new ScalableDimension(1, height));

                this.setFont(t.getFont(namespace+".tab","TabList.tab","General"));

                children.forEach(e -> e.themeChanged(t));
            });
        } else {
            tlm.addThemeChangeListener(t -> {
                colors.put("background",t.getColor(Color.WHITE, "TabList.background"));
                colors.put("tab.background",t.getColor(new Color(0,0,0,0), "TabList.tab.background"));
                colors.put("tab.foreground",t.getColor(Color.BLACK, "TabList.tab.foreground","General.foreground"));
                colors.put("tab.close.color",t.getColor(Color.DARK_GRAY, "TabList.tab.close.color"));
                colors.put("tab.close.rollover.color",t.getColor(Color.LIGHT_GRAY, "TabList.tab.close.hover.color"));
                colors.put("tab.selected.background",t.getColor(Color.BLUE, "TabList.tab.selected.background","TabList.tab.background"));
                colors.put("tab.selected.foreground",t.getColor(Color.BLACK, "TabList.tab.selected.foreground","TabList.tab.hover.foreground","TabList.tab.foreground","General.foreground"));
                colors.put("tab.rollover.background",t.getColor(new Color(0,0,0,0), "TabList.tab.hover.background","TabList.tab.background"));
                colors.put("tab.rollover.foreground",t.getColor(Color.BLACK, "TabList.tab.hover.foreground","TabList.tab.foreground","General.foreground"));

                selectionStyle = t.getString("TabList.tab.selectionStyle","default:FULL");
                selectionLineThickness = Math.max(t.getInteger(2,"TabList.tab.selectionLineThickness"), 0);
                height = Math.max(t.getInteger(5,"TabList.height"),5);
                this.setMinimumSize(new ScalableDimension(1, height));
                this.setPreferredSize(new ScalableDimension(1, height));

                this.setFont(t.getFont("TabList.tab","General"));

                children.forEach(e -> e.themeChanged(t));
            });
        }

        hint.setOutDelay(1);

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    private final ArrayList<TabListElement> renderingChildren = new ArrayList<>();

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(colors.get("background"));
        g.fillRect(0,0,this.getWidth(), this.getHeight());
        g = new ScalableGraphics2D(g);

        this.x = 0;

        int draggedX = -1;

        renderingChildren.clear();
        renderingChildren.addAll(children);

        for(TabListElement element : renderingChildren) {
            if(element != draggedElement) {
                element.render(g.create());
            } else draggedX = x;
            this.x += element.getWidth();
        }

        Dimension newSize = new Dimension(Math.max((int)Math.ceil(this.x * ScalableGraphics2D.SCALE_FACTOR), 1), (int)Math.ceil(height * ScalableGraphics2D.SCALE_FACTOR));

        if(!newSize.equals(this.getPreferredSize())) {
            this.setPreferredSize(newSize);
            this.getParent().revalidate();
        }

        if(draggedElement != null) {
            this.x = draggedX;
            draggedElement.render(g.create());
        }
    }

    public void addTab(TabListElement elem) {
        this.children.add(elem);
    }

    int getOffsetX() {
        return x;
    }

    void setOffsetX(int x) {
        this.x = x;
    }

    String getSelectionStyle() {
        return selectionStyle;
    }

    int getSelectionLineThickness() {
        return selectionLineThickness;
    }

    HashMap<String, Color> getColors() {
        return colors;
    }

    private TabListElement getElementAtMousePos(MouseEvent e) {
        int x = 0;
        for(TabListElement element : children) {
            x += element.getWidth();
            if(e.getX() < x) return element;
        }
        return null;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        e = descaleEvent(e);
        TabListElement element = getElementAtMousePos(e);
        if(element != null) element.mouseClicked(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        e = descaleEvent(e);
        TabListElement element = getElementAtMousePos(e);

        if(mayRearrange && e.getButton() == MouseEvent.BUTTON1) {
            dragPoint = e.getPoint();
            draggedElement = element;
            dragPivot = -1;
        }
        if(element == null) return;

        int x = 0;
        for(TabListElement elem : children) {
            int w = elem.getWidth();
            if(e.getX() < x + w) {
                dragPivot = (float) (e.getX()-x)/w;
                break;
            }
            x += w;
        }

        if(e.getButton() == MouseEvent.BUTTON1 && element.select(e)) selectTab(element);

        element.mousePressed(e);
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        e = descaleEvent(e);
        dragPoint = null;
        draggedElement = null;
        dragPivot = -1;
        TabListElement element = getElementAtMousePos(e);
        if(element != null) element.mouseReleased(e);
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        e = descaleEvent(e);
        if(rolloverElement != null) {
            rolloverElement.setRollover(false);
            rolloverElement.mouseExited(e);
            rolloverElement = null;
        }
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        e = descaleEvent(e);
        if(draggedElement != null) {
            draggedElement.mouseDragged(e);
            dragPoint = e.getPoint();
            int x = 0;
            for (int i = 0; i < children.size(); i++) {
                TabListElement element = children.get(i);
                int w = element.getWidth();
                int center = (int) (e.getX() + (0.5 - dragPivot * w));
                if (center >= x && center < x + w) {
                    children.remove(draggedElement);
                    if (center <= x + w / 2) {
                        children.add(i, draggedElement);
                    } else {
                        children.add(Math.min(i + 1, children.size()), draggedElement);
                    }
                    break;
                }
                x += w;
            }
        }
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        e = descaleEvent(e);
        TabListElement element = getElementAtMousePos(e);
        if(rolloverElement != null) {
            rolloverElement.setRollover(false);
            if(rolloverElement != element) {
                rolloverElement.mouseExited(e);
            }
        }
        if(element != null) {
            element.setRollover(true);
            if(rolloverElement != element) {
                element.mouseEntered(e);
                String text = element.getToolTipText();
                if(text != null && (rolloverElement != null || !hint.isShowing())) {
                    hint.setText(text);
                    HintStylizer.style(hint);
                    hint.show(new Point((int)(this.getLocationOnScreen().x+(element.getLastRecordedOffset()+element.getWidth()/2)*ScalableGraphics2D.SCALE_FACTOR),this.getLocationOnScreen().y+this.getHeight()), () -> rolloverElement == element);
                }
            }
            element.mouseMoved(e);
        }
        repaint();
        rolloverElement = element;
    }

    public void removeTab(Tab tab) {
        children.remove(tab.getLinkedTabItem());
        if(selectedElement == tab.getLinkedTabItem()) selectedElement = null;
        if(rolloverElement == tab.getLinkedTabItem()) rolloverElement = null;
        repaint();
    }

    public void removeTab(TabItem tabItem) {
        children.remove(tabItem);
        if(selectedElement == tabItem) selectedElement = null;
        if(rolloverElement == tabItem) rolloverElement = null;
        repaint();
    }

    public void removeAllTabs() {
        children.clear();
        selectedElement = null;
        rolloverElement = null;
        repaint();
    }

    public void selectTab(Tab tab) {
        if(tab != null) selectTab(tab.getLinkedTabItem());
        else selectTab((TabListElement) null);
    }

    private void selectTab(TabListElement element) {
        if(selectedElement != null) {
            selectedElement.setSelected(false);
        }
        selectedElement = element;
        if(element != null) {
            element.setSelected(true);
        }
        repaint();
    }

    private final ArrayList<Tab> fallbackAllTabs = new ArrayList<>();

    public Tab getFallbackTab(Tab tab) {
        fallbackAllTabs.clear();
        for(TabListElement element : children) {
            if(element instanceof TabItem && ((TabItem) element).getAssociatedTab() != null) fallbackAllTabs.add(((TabItem) element).getAssociatedTab());
        }
        int index = fallbackAllTabs.indexOf(tab);
        if(index == -1) return null;
        fallbackAllTabs.remove(index);
        if(fallbackAllTabs.size() == 0) return null;

        Tab left = (index >= 1) ? fallbackAllTabs.get(index-1) : null;
        Tab right = (index < fallbackAllTabs.size()) ? fallbackAllTabs.get(index) : null;
        if(left == null) return right;
        if(right == null) return left;

        return (left.openedTimeStamp > right.openedTimeStamp) ? left : right;
    }

    public boolean mayRearrange() {
        return mayRearrange;
    }

    public void setMayRearrange(boolean mayRearrange) {
        this.mayRearrange = mayRearrange;
    }

    public ArrayList<TabListElement> getTabItems() {
        return children;
    }
}
