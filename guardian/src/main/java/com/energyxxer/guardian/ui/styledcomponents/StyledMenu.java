package com.energyxxer.guardian.ui.styledcomponents;

import com.energyxxer.guardian.global.Commons;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.Disposable;
import com.energyxxer.xswing.menu.XMenu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Separator that reacts to window theme changes.
 */
public class StyledMenu extends XMenu implements Disposable {

    public static final MouseWheelListener NAVIGATE_WITH_MOUSE_WHEEL = e -> {

        Component thiz = e.getComponent();
        if(!(thiz.getParent() instanceof JPopupMenu)) return;
        MenuElement[] selectedPath = MenuSelectionManager.defaultManager().getSelectedPath();
        for(int i = 0; i < selectedPath.length-1; i++) {
            if(selectedPath[i] instanceof StyledPopupMenu) {
                ((StyledPopupMenu) selectedPath[i]).suppressDispose = true;
            }
        }

        int x = e.getX() + thiz.getLocationOnScreen().x;
        int thisIndex = ((JPopupMenu) thiz.getParent()).getComponentIndex(thiz);
        int delta = (e.getWheelRotation() > 0 ? 1 : -1);
        int nextIndex = thisIndex;
        int componentCount = thiz.getParent().getComponentCount();
        Component nextItem;
        do {
            nextIndex += delta;
            if(nextIndex < 0) {
                nextIndex = componentCount-1;
            } else if(nextIndex >= componentCount) {
                nextIndex = 0;
            }

            nextItem = thiz.getParent().getComponent(nextIndex);

            if(nextIndex == thisIndex) break; //Only one valid item


            if(nextItem instanceof JMenuItem && nextItem.isEnabled()) {
                break;
            }
        } while(true);

        GuardianWindow.robot.mouseMove(x, nextItem.getLocationOnScreen().y + nextItem.getHeight()/2);

        selectedPath[selectedPath.length-1] = (MenuElement) nextItem;

        SwingUtilities.invokeLater(() -> {
            for(int i = 0; i < selectedPath.length; i++) {
                if(selectedPath[i] instanceof StyledPopupMenu) {
                    ((StyledPopupMenu) selectedPath[i]).suppressDispose = false;
                }
            }
            selectedPath[0].getComponent().setVisible(true);
            MenuSelectionManager.defaultManager().setSelectedPath(selectedPath);
        });
    };

    private ThemeListenerManager tlm = new ThemeListenerManager();

    private List<Disposable> disposableChildren = new ArrayList<>();

    static {
        UIManager.put("Menu.submenuPopupOffsetX",0);
        UIManager.put("Menu.submenuPopupOffsetY",0);
    }

    public StyledMenu(String text, String icon) {
        super(text);
        this.setInheritsPopupMenu(true);

        tlm.addThemeChangeListener(t -> {
            //this.setBackground(t.getColor(new Color(215, 215, 215), "General.menu.background"));
            this.setForeground(t.getColor(Color.BLACK, "General.menu.foreground","General.foreground"));
            this.setRolloverBackground(t.getColor(new Color(190, 190, 190), "General.menu.selected.background"));
            this.setFont(t.getFont("General.menu","General"));
            if(icon != null) this.setIcon(new ImageIcon(Commons.getIcon(icon).getScaledInstance(16,16, Image.SCALE_SMOOTH)));

            getPopupMenu().setBackground(t.getColor(new Color(215, 215, 215), "General.menu.background"));
            int borderThickness = Math.max(t.getInteger(1,"General.menu.border.thickness"),0);
            getPopupMenu().setBorder(BorderFactory.createMatteBorder(borderThickness, borderThickness, borderThickness, borderThickness ,t.getColor(new Color(200, 200, 200), "General.menu.border.color")));
        });

        this.addMouseWheelListener(NAVIGATE_WITH_MOUSE_WHEEL);
    }
    public StyledMenu(String text) {
        this(text, null);
    }
    public void addSeparator() {
        this.add(new StyledSeparator());
    }

    @Override
    public Component add(Component comp) {
        filterDisposable(comp);
        return super.add(comp);
    }

    @Override
    public JMenuItem add(JMenuItem menuItem) {
        filterDisposable(menuItem);
        return super.add(menuItem);
    }

    private void filterDisposable(Object obj) {
        if(obj instanceof Disposable) {
            disposableChildren.add((Disposable) obj);
        }
    }

    @Override
    public void dispose() {
        tlm.dispose();
        disposableChildren.forEach(Disposable::dispose);
        disposableChildren.clear();
    }
}