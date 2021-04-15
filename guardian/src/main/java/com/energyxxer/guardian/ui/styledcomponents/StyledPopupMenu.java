package com.energyxxer.guardian.ui.styledcomponents;

import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.Disposable;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 12/14/2016.
 */
public class StyledPopupMenu extends JPopupMenu implements Disposable, PopupMenuListener {
    private String namespace = null;

    private ThemeListenerManager tlm = new ThemeListenerManager();

    private final List<Disposable> disposableChildren = new ArrayList<>();
    boolean suppressDispose = false;

    public StyledPopupMenu() {
        this(null,null);
    }

    public StyledPopupMenu(String label) {
        this(label,null);
    }

    public StyledPopupMenu(String label, String namespace) {
        if(label != null) setLabel(label);
        if(namespace != null) this.setNamespace(namespace);

        tlm.addThemeChangeListener(t -> {
            if (this.namespace != null) {
                setBackground(t.getColor(new Color(215, 215, 215), this.namespace + ".menu.background", "General.menu.background"));
                int borderThickness = Math.max(t.getInteger(1,this.namespace + ".menu.border.thickness","General.menu.border.thickness"),0);
                setBorder(BorderFactory.createMatteBorder(borderThickness, borderThickness, borderThickness, borderThickness, t.getColor(new Color(200, 200, 200), this.namespace + ".menu.border.color", "General.menu.border.color")));
            } else {
                setBackground(t.getColor(new Color(215, 215, 215), "General.menu.background"));
                int borderThickness = Math.max(t.getInteger(1,"General.menu.border.thickness"),0);
                setBorder(BorderFactory.createMatteBorder(borderThickness, borderThickness, borderThickness, borderThickness ,t.getColor(new Color(200, 200, 200), "General.menu.border.color")));
            }
        });

        this.addPopupMenuListener(this);
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void addSeparator() {
        this.add(new StyledSeparator(namespace));
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
        if(!suppressDispose) {
//            Debug.log("Dispose");
            tlm.dispose();
            disposableChildren.forEach(Disposable::dispose);
            disposableChildren.clear();
        } else {
//            Debug.log("Dispose (suppressed)");
        }
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        dispose();
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
    }

    @Override
    public String toString() {
        return "StyledPopupMenu{" +
                "namespace='" + namespace + '\'' +
                '}';
    }

//    @Override
//    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
//        if(propertyName.equals("JPopupMenu.firePopupMenuCanceled")) {
//            Debug.log(newValue);
//            if(newValue == Boolean.TRUE) {
//                putClientProperty(propertyName, null);
//
//                if(System.currentTimeMillis() < scrollTime + 100) {
//                    Debug.log("OH NO MENU WAS CANCELED WHATEVER WILL I DO");
//                }
//            }
//        } else {
//            super.firePropertyChange(propertyName, oldValue, newValue);
//        }
//    }
}
