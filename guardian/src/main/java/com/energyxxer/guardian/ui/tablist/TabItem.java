package com.energyxxer.guardian.ui.tablist;

import com.energyxxer.guardian.global.TabManager;
import com.energyxxer.guardian.ui.Tab;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.ui.styledcomponents.StyledMenuItem;
import com.energyxxer.guardian.ui.styledcomponents.StyledPopupMenu;
import com.energyxxer.guardian.ui.theme.Theme;
import com.energyxxer.guardian.util.ImageUtil;
import com.energyxxer.util.StringUtil;
import com.energyxxer.xswing.ScalableGraphics2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.MouseEvent;

public class TabItem extends TabListElement {
    @NotNull
    private final TabManager manager;
    @NotNull
    private ModuleToken token;
    @Nullable
    private final Tab associatedTab;

    private Image icon = null;
    private String name = null;

    private int x = 0;
    private int width = 0;

    private boolean closeRollover = false;

    public TabItem(TabManager manager, Tab associatedTab) {
        super(manager.getTabList());
        this.manager = manager;
        this.token = associatedTab.token;
        this.associatedTab = associatedTab;

        this.updateName();
        this.updateIcon();

        associatedTab.linkTabItem(this);
    }

    public TabItem(TabManager manager, @NotNull ModuleToken token) {
        super(manager.getTabList());
        this.manager = manager;
        this.token = token;
        this.associatedTab = null;
        this.updateName();
        this.updateIcon();
    }

    public void updateName() {
        this.name = token.getTitle(ModuleToken.TokenContext.TAB);
        if(this.name != null) this.name = StringUtil.ellipsis(this.name,32);
    }

    void updateIcon() {
        this.icon = token.getIcon();
        if(this.icon != null) {
            this.icon = ImageUtil.fitToSize(this.icon, 16, 16);
        }
    }

    @Override
    public void render(Graphics g) {
        g.setFont(master.getFont());
        if(associatedTab != null && associatedTab.isTemporary()) {
            g.setFont(g.getFont().deriveFont(Font.ITALIC));
        }
        FontMetrics fm = g.getFontMetrics();

        boolean iconOnly = this.name == null;

        this.x = master.getOffsetX();
        this.lastRecordedOffset = x;
        int h = (int)Math.floor(master.getHeight() / ScalableGraphics2D.SCALE_FACTOR);
        int w = iconOnly ? ((Math.max(32, h)) + (token.isTabCloseable() ? 16 : 0)) : (8 + (icon != null ? 16 + 2 : 0) + fm.stringWidth(this.name) + (token.isTabCloseable() ? 16 : 0) + 15);

        /*
            // 8px margin
            // + 16px icon
            // + 2px
            // + name px
            // + 10px
            // + 6px close button
            // + 12px margin
        }*/

        this.width = w;

        int offsetX = x;
        if(master.draggedElement == this) offsetX = (int) (master.dragPoint.x - (w * master.dragPivot));

        g.setColor((this.rollover || this.selected) ? master.getColors().get("tab.rollover.background") : master.getColors().get("tab.background"));
        g.fillRect(offsetX, 0, w, h);

        if(this.selected) {
            g.setColor(master.getColors().get("tab.selected.background"));

            switch(master.getSelectionStyle()) {
                case "FULL": {
                    g.fillRect(offsetX, 0, w, h);
                    break;
                }
                case "LINE_LEFT": {
                    g.fillRect(offsetX, 0, master.getSelectionLineThickness(), h);
                    break;
                }
                case "LINE_RIGHT": {
                    g.fillRect(offsetX + h - master.getSelectionLineThickness(), 0, master.getSelectionLineThickness(), h);
                    break;
                }
                case "LINE_TOP": {
                    g.fillRect(offsetX, 0, w, master.getSelectionLineThickness());
                    break;
                }
                case "LINE_BOTTOM": {
                    g.fillRect(offsetX, h - master.getSelectionLineThickness(), w, master.getSelectionLineThickness());
                    break;
                }
            }
        }

        Graphics2D g2d = (Graphics2D) g;

        float alpha = token.getAlpha();
        if(alpha != 1) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }

        if(icon != null) g.drawImage(icon, offsetX + (iconOnly ? h/2 : 16) - 16/2, (h-16)/2 + 8 - 16/2, 16, 16, null);
        if(iconOnly) {
            offsetX += 24;
        } else {
            if(icon != null) offsetX += 18;
            offsetX += 11;

            if(this.selected) {
                g.setColor(master.getColors().get("tab.selected.foreground"));
            } else if(this.rollover) {
                g.setColor(master.getColors().get("tab.rollover.foreground"));
            } else {
                g.setColor(master.getColors().get("tab.foreground"));
            }

            g.drawString(this.name, offsetX, (h+fm.getAscent()-fm.getDescent())/2);

            offsetX += fm.stringWidth(this.name);
        }


        if(token.isTabCloseable()) {
            offsetX += 10;
            if(this.closeRollover) {
                g.setColor(master.getColors().get("tab.close.rollover.color"));
            } else {
                g.setColor(master.getColors().get("tab.close.color"));
            }
            if(associatedTab == null || associatedTab.isSaved()) {
                g.drawLine(offsetX, (h - 6) / 2, offsetX + 6, (h + 6) / 2);
                g.drawLine(offsetX, (h + 6) / 2, offsetX + 6, (h - 6) / 2);
            } else {
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.fillOval(offsetX, (h - 6) / 2, 6, 6);
            }
        }
        g.dispose();
    }

    private boolean isOverCloseButton(MouseEvent e) {
        if(!token.isTabCloseable()) return false;
        int padding = 5;
        return (e.getX() >= this.x + this.width - 12 - 6 - padding && e.getX() <= this.x + this.width - 12 + padding);
    }

    @Override
    public boolean select(MouseEvent e) {
        return associatedTab != null && !isOverCloseButton(e);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public String getToolTipText() {
        return token.getHint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(token.isTabCloseable() && (isOverCloseButton(e) || e.getButton() == MouseEvent.BUTTON2)) {
            manager.closeTab(this.associatedTab);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(!isOverCloseButton(e)) {
            if(this.associatedTab != null) {
                if(e.getButton() == MouseEvent.BUTTON1) {
                    manager.setSelectedTab(this.associatedTab);
                }
            } else {
                selected = true;
            }
            token.onInteract();
            master.repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(this.associatedTab == null && e.getButton() == MouseEvent.BUTTON1) {
            selected = false;
        }
        if(e.isPopupTrigger() && token.isTabCloseable()) {
            StyledPopupMenu menu = this.generatePopup();
            menu.show(e.getComponent(), (int) (e.getX() * ScalableGraphics2D.SCALE_FACTOR), (int) (e.getY() * ScalableGraphics2D.SCALE_FACTOR));
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {
        closeRollover = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        closeRollover = isOverCloseButton(e);
    }

    public Image getIcon() {
        return icon;
    }

    private StyledPopupMenu generatePopup() {
        StyledPopupMenu menu = new StyledPopupMenu();

        {
            StyledMenuItem item = new StyledMenuItem("Close");
            item.addActionListener(e -> manager.closeTab(associatedTab));
            menu.add(item);
        }
        {
            StyledMenuItem item = new StyledMenuItem("Close Others");
            item.addActionListener(e -> {
                for(int i = 0; i < manager.openTabs.size();) {
                    Tab tab = manager.openTabs.get(i);
                    if(tab != associatedTab) {
                        manager.closeTab(tab);
                    } else {
                        i++;
                    }
                }
            });
            menu.add(item);
        }
        {
            StyledMenuItem item = new StyledMenuItem("Close Tabs To The Left");
            item.addActionListener(e -> {
                for(int i = 0; i < master.children.size();) {
                    TabListElement tabListElement = master.children.get(i);
                    if(tabListElement instanceof TabItem) {
                        Tab tab = ((TabItem) tabListElement).associatedTab;
                        if(tab != associatedTab) {
                            manager.closeTab(tab);
                        } else {
                            break;
                        }
                    }
                }
            });
            menu.add(item);
        }
        {
            StyledMenuItem item = new StyledMenuItem("Close Tabs To The Right");
            item.addActionListener(e -> {
                boolean doClose = false;
                for(int i = 0; i < master.children.size();) {
                    TabListElement tabListElement = master.children.get(i);
                    if(tabListElement instanceof TabItem) {
                        Tab tab = ((TabItem) tabListElement).associatedTab;
                        if(tab != associatedTab) {
                            if(doClose) manager.closeTab(tab);
                            else i++;
                        } else {
                            doClose = true;
                            i++;
                        }
                    }
                }
            });
            menu.add(item);
        }
        menu.addSeparator();
        {
            StyledMenuItem item = new StyledMenuItem("Close All");
            item.addActionListener(e -> {
                while(!manager.openTabs.isEmpty()) {
                    manager.closeTab(manager.openTabs.get(0));
                }
            });
            menu.add(item);
        }
        return menu;
    }

    @Nullable
    public Tab getAssociatedTab() {
        return associatedTab;
    }

    @Override
    public void themeChanged(Theme t) {
        this.updateName();
        this.updateIcon();
    }

    public void transform(ModuleToken newToken) {
        this.token = newToken;
        updateName();
        updateIcon();
    }

    public TabManager getManager() {
        return manager;
    }
}
