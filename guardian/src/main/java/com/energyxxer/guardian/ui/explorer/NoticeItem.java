package com.energyxxer.guardian.ui.explorer;

import com.energyxxer.enxlex.report.Notice;
import com.energyxxer.enxlex.report.StackTrace;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.explorer.base.ExplorerFlag;
import com.energyxxer.guardian.ui.explorer.base.ExplorerMaster;
import com.energyxxer.guardian.ui.explorer.base.elements.ExplorerElement;
import com.energyxxer.guardian.ui.modules.FileModuleToken;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.ui.styledcomponents.StyledMenuItem;
import com.energyxxer.guardian.ui.styledcomponents.StyledPopupMenu;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Locale;

import static com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor.isPlatformControlDown;

/**
 * Created by User on 5/16/2017.
 */
public class NoticeItem extends ExplorerElement {
    private Notice notice;

    private int lineCount = 1;
    int x;

    public NoticeItem(ExplorerMaster master, Notice notice) {
        super(master);
        this.notice = notice;

        lineCount = notice.getExtendedMessage().split("\n",-1).length;

        this.x = master.getInitialIndent();

        updateChildren();
    }

    public NoticeItem(NoticeGroupElement parent, Notice notice) {
        super(parent.getMaster());
        this.notice = notice;

        lineCount = notice.getExtendedMessage().split("\n",-1).length;

        this.x = (parent.indentation + 1) * master.getIndentPerLevel() + master.getInitialIndent();

        updateChildren();
    }

    private void updateChildren() {
        children.clear();
        if(notice.getStackTrace() != null) {
            for(StackTrace.StackTraceElement elem : notice.getStackTrace().getElements()) {
                children.add(new NoticeStackTraceItem(this, elem));
            }
        }
    }

    @Override
    public void render(Graphics g) {
        g.setFont(master.getFont());
        int y = master.getOffsetY();
        master.addToFlatList(this);

        int x = this.x + 23;

        g.setColor((this.rollover || this.selected) ? master.getColors().get("item.rollover.background") : master.getColors().get("item.background"));
        g.fillRect(0, master.getOffsetY(), master.getWidth(), master.getRowHeight() * lineCount);
        if(this.selected) {
            g.setColor(master.getColors().get("item.selected.background"));

            switch(master.getSelectionStyle()) {
                case "FULL": {
                    g.fillRect(0, master.getOffsetY(), master.getWidth(), master.getRowHeight() * lineCount);
                    break;
                }
                case "LINE_LEFT": {
                    g.fillRect(0, master.getOffsetY(), master.getSelectionLineThickness(), master.getRowHeight() * lineCount);
                    break;
                }
                case "LINE_RIGHT": {
                    g.fillRect(master.getWidth() - master.getSelectionLineThickness(), master.getOffsetY(), master.getSelectionLineThickness(), master.getRowHeight() * lineCount);
                    break;
                }
                case "LINE_TOP": {
                    g.fillRect(0, master.getOffsetY(), master.getWidth(), master.getSelectionLineThickness());
                    break;
                }
                case "LINE_BOTTOM": {
                    g.fillRect(0, master.getOffsetY() + master.getRowHeight() * lineCount - master.getSelectionLineThickness(), master.getWidth(), master.getSelectionLineThickness());
                    break;
                }
            }
        }

        //File Icon
        {
            int margin = ((master.getRowHeight() - 16) / 2);
            g.drawImage(master.getAssetMap().get(notice.getType().name().toLowerCase(Locale.ENGLISH)),x,y + margin,16, 16,null);
        }
        x += 25;

        //File Name

        if(this.selected) {
            g.setColor(master.getColors().get("item.selected.foreground"));
        } else if(this.rollover) {
            g.setColor(master.getColors().get("item.rollover.foreground"));
        } else {
            g.setColor(master.getColors().get("item.foreground"));
        }
        FontMetrics metrics = g.getFontMetrics(g.getFont());

        int extraLength = 0;

        for(String line : notice.getExtendedMessage().split("\n")) {
            g.drawString(line, x, y + metrics.getAscent() + ((master.getRowHeight() - metrics.getHeight())/2));
            y += master.getRowHeight();

            extraLength = Math.max(extraLength, metrics.stringWidth(line));
        }
        x += extraLength;

        if(master.getFlag(ExplorerFlag.DEBUG_WIDTH)) {
            g.setColor(Color.YELLOW);
            g.fillRect(master.getContentWidth()-2, master.getOffsetY(), 2, master.getRowHeight());
            g.setColor(Color.GREEN);
            g.fillRect(x-2, master.getOffsetY(), 2, master.getRowHeight() * lineCount);
        }

        master.renderOffset(this.getHeight());

        master.setContentWidth(Math.max(master.getContentWidth(), x));

        for(ExplorerElement i : children) {
            i.render(g);
        }
    }

    @Override
    public ModuleToken getToken() {
        return null;
    }

    @Override
    public int getHeight() {
        return master.getRowHeight() * lineCount;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1 && !isPlatformControlDown(e) && e.getClickCount() % 2 == 0) {
            interact();
        }
    }

    @Override
    public void interact() {
        for(ExplorerElement child : children) {
            if(child instanceof NoticeStackTraceItem && ((NoticeStackTraceItem) child).canOpen()) {
                child.interact();
                return;
            }
        }
        if(notice.getSource() != null) {
            File file = notice.getSource().getExactFile();
            if(file != null) {
                GuardianWindow.tabManager.openTab(new FileModuleToken(file), notice.getLocationIndex(), notice.getLocationLength());
            } else {
                GuardianWindow.showPopupMessage("This file is either inside a zip or\nbuilt-in, and cannot be opened");
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        master.setSelected(this, e);
        confirmActivationMenu(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        confirmActivationMenu(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    private void confirmActivationMenu(MouseEvent e) {
        if(e.isPopupTrigger()) {
            if(!this.selected) master.setSelected(this, new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), 0, e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), MouseEvent.BUTTON1));
            StyledPopupMenu menu = new StyledPopupMenu();
            {
                StyledMenuItem copyItem = new StyledMenuItem("Copy to clipboard");
                copyItem.setIconName("copy");
                copyItem.addActionListener(ae -> {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(new StringSelection(notice.getExtendedMessage()), null);
                });
                menu.add(copyItem);
            }
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
