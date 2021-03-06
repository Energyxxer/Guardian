package com.energyxxer.guardian.ui.explorer;

import com.energyxxer.enxlex.report.StackTrace;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.explorer.base.ExplorerFlag;
import com.energyxxer.guardian.ui.explorer.base.elements.ExplorerElement;
import com.energyxxer.guardian.ui.modules.FileModuleToken;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.util.StringBounds;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;

public class NoticeStackTraceItem extends ExplorerElement {
    private NoticeItem parent;
    private StackTrace.StackTraceElement traceElement;

    private int x;
    private String message;
    private int lineCount;

    public NoticeStackTraceItem(NoticeItem parent, StackTrace.StackTraceElement traceElement) {
        super(parent.getMaster());
        this.parent = parent;
        this.traceElement = traceElement;

        this.x = (parent.x) + master.getIndentPerLevel();
        this.message = traceElement.toString();
        this.lineCount = message.split("\n",-1).length;
    }

    @Override
    public void render(Graphics g) {
        g.setFont(master.getFont());
        int y = master.getOffsetY();
        master.addToFlatList(this);

        int x = this.x + 23;

        g.setColor((this.rollover || parent.isRollover() || this.selected) ? master.getColors().get("item.rollover.background") : master.getColors().get("item.background"));
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

        //Icon (blank)
        x += 25;

        //Message

        if(this.selected) {
            g.setColor(master.getColors().get("item.selected.foreground"));
        } else if(this.rollover) {
            g.setColor(master.getColors().get("item.rollover.foreground"));
        } else {
            g.setColor(master.getColors().get("item.foreground"));
        }
        FontMetrics metrics = g.getFontMetrics(g.getFont());

        int extraLength = 0;

        for(String line : message.split("\n")) {
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
        if(e.getButton() == MouseEvent.BUTTON1 && !AdvancedEditor.isPlatformControlDown(e) && e.getClickCount() % 2 == 0) {
            interact();
        }
    }

    boolean canOpen() {
        return traceElement.getFileMessage() == null;
    }

    @Override
    public void interact() {
        if(traceElement.getPattern() != null && traceElement.getPattern().getSource() != null) {
            File file = traceElement.getPattern().getSource().getExactFile();
            if(file != null) {
                StringBounds bounds = traceElement.getPattern().getStringBounds();
                int start = bounds.start.index;
                int length = bounds.end.index - bounds.start.index;
                GuardianWindow.tabManager.openTab(new FileModuleToken(file), start, length);
            } else {
                GuardianWindow.showPopupMessage("This file is either inside a zip or\nbuilt-in, and cannot be opened");
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        master.setSelected(this, e);
    }
}
