package com.energyxxer.guardian.ui.explorer.base;

import com.energyxxer.guardian.ui.explorer.base.elements.ExplorerElement;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.ui.theme.Theme;
import com.energyxxer.guardian.ui.orderlist.CompoundActionModuleToken;
import com.energyxxer.guardian.ui.orderlist.ItemAction;
import com.energyxxer.guardian.ui.orderlist.ItemActionHost;
import com.energyxxer.guardian.util.ImageUtil;
import com.energyxxer.util.StringUtil;
import com.energyxxer.xswing.ScalableGraphics2D;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class ActionHostExplorerItem extends ExplorerElement implements ItemActionHost {

    @NotNull
    private final CompoundActionModuleToken token;

    private Image icon = null;
    private String name = null;
    private boolean autoUpdateTitle = false;

    private int y = 0;
    private int height = 20;

    private int actionRolloverIndex = -1;
    private int pressedStart = -1;
    private Point toolTipLocation = new Point();

    private List<ItemAction> actions;

    public ActionHostExplorerItem(ExplorerMaster master, @NotNull CompoundActionModuleToken token) {
        super(master);
        this.token = token;
        this.updateName();
        this.updateIcon();
        this.actions = token.getActions();
    }

    private void updateName() {
        this.name = token.getTitle();
        if(this.name != null) this.name = StringUtil.ellipsis(this.name,100);
    }

    private void updateIcon() {
        this.icon = token.getIcon();
        if(this.icon != null) {
            this.icon = ImageUtil.fitToSize(this.icon, 16, 16);
        }
    }

    @Override
    public void render(Graphics g) {
        g.setFont(master.getFont());
        FontMetrics fm = g.getFontMetrics();
        master.flatList.add(this);

        int x = master.getIndentation() * master.getIndentPerLevel() + master.getInitialIndent();
        this.y = master.getOffsetY();
        this.lastRecordedOffset = y;
        int w = (int)(master.getWidth() / ScalableGraphics2D.SCALE_FACTOR);
        int h = master.getRowHeight();

        this.height = h;
        toolTipLocation.y = (int) (height * 0.8);

        int offsetY = y;

        g.setColor((this.rollover || this.selected) ? master.getColors().get("item.rollover.background") : master.getColors().get("item.background"));
        g.fillRect(0, offsetY, w, h);
        if(this.selected) {
            g.setColor(master.getColors().get("item.selected.background"));

            switch(master.getSelectionStyle()) {
                case "FULL": {
                    g.fillRect(0, offsetY, w, h);
                    break;
                }
                case "LINE_LEFT": {
                    g.fillRect(0, offsetY, master.getSelectionLineThickness(), h);
                    break;
                }
                case "LINE_RIGHT": {
                    g.fillRect(w - master.getSelectionLineThickness(), offsetY, master.getSelectionLineThickness(), h);
                    break;
                }
                case "LINE_TOP": {
                    g.fillRect(0, offsetY, w, master.getSelectionLineThickness());
                    break;
                }
                case "LINE_BOTTOM": {
                    g.fillRect(0, offsetY + h - master.getSelectionLineThickness(), w, master.getSelectionLineThickness());
                    break;
                }
            }
        }

        int leftX = 6 + x;
        int rightX = w - 6;
        {

            if(master.getSelectionStyle().equals("LINE_LEFT")) {
                leftX += master.getSelectionLineThickness();
            } else if(master.getSelectionStyle().equals("LINE_RIGHT")) {
                rightX -= master.getSelectionLineThickness();
            }

            int actionIndex = 0;
            for(ItemAction action : actions) {
                action.render(g, this, (action.isLeftAligned() ? leftX : rightX), offsetY, w, h, (actionIndex == actionRolloverIndex) ? (pressedStart == actionIndex ? 2 : 1) : 0, isActionEnabled(actionIndex));
                int actionOffset = action.getRenderedWidth();

                if(action.isLeftAligned()) leftX += actionOffset;
                else rightX -= actionOffset;

                actionIndex++;
            }

            x = leftX;
        }

        int margin = ((h - 16) / 2);
        x += 10;

        //File Icon
        if (icon != null) {
            g.drawImage(this.icon, x + 8 - 16 / 2, offsetY + margin + 8 - 16 / 2, 16, 16, null);
            x += 24;
        }

        //File Name

        if(this.selected) {
            g.setColor(master.getColors().get("item.selected.foreground"));
        } else if(this.rollover) {
            g.setColor(master.getColors().get("item.rollover.foreground"));
        } else {
            g.setColor(master.getColors().get("item.foreground"));
        }

        if(autoUpdateTitle) {
            updateName();
        }
        if(name != null) {
            int stringWidth = fm.stringWidth(name);
            if(x + stringWidth > rightX) {
                float estimatedCharacterWidth = (float)stringWidth/name.length();
                int overlap = x + stringWidth - rightX;

                int stripAmount = (int) Math.ceil((overlap / estimatedCharacterWidth) + 6);
                String stripped;
                if(stripAmount >= name.length()) {
                    stripped = "";
                } else if(token.ellipsisFromLeft()) {
                    stripped = "..." + name.substring(stripAmount);
                } else {
                    stripped = name.substring(0, name.length() - stripAmount) + "...";
                }
                g.drawString(stripped, x, offsetY + fm.getAscent() + ((h - fm.getHeight())/2));
                x += fm.stringWidth(stripped);
            } else {
                g.drawString(name, x, offsetY + fm.getAscent() + ((h - fm.getHeight())/2));
                x += fm.stringWidth(name);
            }
            String subTitle = token.getSubTitle();
            if(subTitle != null) {
                g.setColor(new Color(g.getColor().getRed(), g.getColor().getGreen(), g.getColor().getBlue(), (int)(g.getColor().getAlpha() * 0.75)));
                x += 8;
                g.drawString(subTitle, x, master.getOffsetY() + fm.getAscent() + ((master.getRowHeight() - fm.getHeight())/2));
                x += fm.stringWidth(subTitle);
            }
        }

        master.renderOffset(this.getHeight());
    }

    private boolean isActionEnabled(int index) {
        ItemAction action = actions.get(index);
        return action.getActionCode() == -1;
    }

    private int getActionRolloverIndex(MouseEvent e) {
        return getActionRolloverIndex(e, false);
    }

    private int getActionRolloverIndex(MouseEvent e, boolean update) {
        int w = (int)(master.getWidth() / ScalableGraphics2D.SCALE_FACTOR);
        if(update) {
            toolTipLocation.x = master.getWidth() / 2;
            actionRolloverIndex = -1;
        }

        int leftX = 6;
        int rightX = w - 6;

        if(master.getSelectionStyle().equals("LINE_LEFT")) {
            leftX += master.getSelectionLineThickness();
        } else if(master.getSelectionStyle().equals("LINE_RIGHT")) {
            rightX -= master.getSelectionLineThickness();
        }

        for(int i = 0; i < actions.size(); i++) {
            ItemAction action = actions.get(i);
            if(action.intersects(new Point(action.isLeftAligned() ? (e.getX() - leftX) : (rightX - e.getX()), e.getY() - lastRecordedOffset), w, getHeight())) {
                if(isActionEnabled(i)) {
                    if(update) {
                        actionRolloverIndex = i;
                        toolTipLocation.x = (int)(((action.isLeftAligned() ? leftX : (rightX - action.getRenderedWidth())) + action.getHintOffset()) * ScalableGraphics2D.SCALE_FACTOR);
                    }
                    return i;
                } else {
                    return -1;
                }
            }
            int actionOffset = action.getRenderedWidth();
            if(action.isLeftAligned()) leftX += actionOffset;
            else rightX -= actionOffset;
        }

        return -1;
    }

    @Override
    public boolean select(MouseEvent e) {
        return getActionRolloverIndex(e) < 0;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public String getToolTipText() {
        if(actionRolloverIndex < 0) return token.getHint();
        return actions.get(actionRolloverIndex).getHint();
    }

    @Override
    public Point getToolTipLocation() {
        return toolTipLocation;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        ItemAction action = getActionForMouseEvent(e);
        if(action != null) action.mouseClicked(e, this);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(select(e)) {
            master.setSelected(this, e);
        }
        int index = getActionRolloverIndex(e);
        if(index >= 0) {
            pressedStart = index;
            actions.get(index).mousePressed(e, this);
        } else {
            pressedStart = -1;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(!e.isPopupTrigger() && e.getButton() == MouseEvent.BUTTON1) master.setSelected(this, e);
        int index = getActionRolloverIndex(e);
        if(pressedStart >= 0 && pressedStart == index) {
            actions.get(pressedStart).mouseReleased(e, this);
        }
        pressedStart = -1;
        if(!e.isConsumed() && e.isPopupTrigger()) {
            JPopupMenu menu = this.generatePopup();
            if(menu != null) menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private ItemAction getActionForMouseEvent(MouseEvent e) {
        int index = getActionRolloverIndex(e);
        return index >= 0 ? actions.get(index) : null;
    }

    public void performOperation(int code) {
        //No codes for this explorer item
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {
        actionRolloverIndex = -1;
        pressedStart = -1;
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void setSelected(boolean selected) {
        if(selected && !this.selected) {
            token.onInteract();
        }
        super.setSelected(selected);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        getActionRolloverIndex(e, true);
    }

    public Image getIcon() {
        return icon;
    }

    private JPopupMenu generatePopup() {
        return token.generateMenu(ModuleToken.TokenContext.EXPLORER);
    }

    @Override
    public void themeChanged(Theme t) {
        this.updateIcon();
    }

    @NotNull
    public CompoundActionModuleToken getToken() {
        return token;
    }

    @Override
    public JComponent getComponent() {
        return master;
    }

    @Override
    public StyleProvider getStyleProvider() {
        return master;
    }

    public boolean isAutoUpdateTitle() {
        return autoUpdateTitle;
    }

    public void setAutoUpdateTitle(boolean autoUpdateTitle) {
        this.autoUpdateTitle = autoUpdateTitle;
    }
}
