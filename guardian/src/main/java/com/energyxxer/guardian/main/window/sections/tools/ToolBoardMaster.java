package com.energyxxer.guardian.main.window.sections.tools;

import com.energyxxer.guardian.ui.ToolbarButton;
import com.energyxxer.guardian.ui.navbar.NavigatorMaster;
import com.energyxxer.guardian.ui.styledcomponents.Padding;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.guardian.ui.styledcomponents.StyledLabel;
import com.energyxxer.xswing.ComponentResizer;
import com.energyxxer.xswing.OverlayBorderPanel;
import com.energyxxer.xswing.ScalableDimension;
import com.energyxxer.xswing.hints.Hint;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

public class ToolBoardMaster extends OverlayBorderPanel {

    private ThemeListenerManager tlm = new ThemeListenerManager();

    private JPanel header = new JPanel(new BorderLayout());
    private StyledLabel headerLabel = new StyledLabel("Sample Text","ToolBoard.header", tlm);

    private NavigatorMaster navbar = new NavigatorMaster(NavigatorMaster.VERTICAL);

    private ToolBoard lastOpenedBoard = null;
    private boolean open = false;

    private ComponentResizer resizer;

    public ToolBoardMaster() {
        super(new BorderLayout(), new Insets(ComponentResizer.DIST, 0, 0, 0));
        this.setOpaque(false);
        this.setBorder(new EmptyBorder(ComponentResizer.DIST, 0, 0, 0));

        tlm.addThemeChangeListener(t -> {
            header.setPreferredSize(new ScalableDimension(0, Math.max(5, t.getInteger(29, "ToolBoard.header.height"))));
            header.setBackground(t.getColor(Color.WHITE, "ToolBoard.header.background"));
            header.setBorder(BorderFactory.createMatteBorder(Math.max(t.getInteger(1, "ToolBoard.header.border.top.thickness", "ToolBoard.header.border.thickness"),0), 0, Math.max(t.getInteger(1, "ToolBoard.header.border.bottom.thickness", "ToolBoard.header.border.thickness"),0), 0, t.getColor(new Color(200, 200, 200), "ToolBoard.header.border.color")));
        });

        JPanel labelWrapper = new JPanel(new BorderLayout());
        labelWrapper.setOpaque(false);
        labelWrapper.add(new Padding(15,tlm, "ToolBoard.header.label.indentation"), BorderLayout.WEST);
        labelWrapper.add(headerLabel, BorderLayout.CENTER);
        header.add(labelWrapper, BorderLayout.WEST);

        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if((e.getClickCount() & 1) == 0) close();
            }
        });

        headerLabel.setTextThemeDriven(false);

        JPanel buttonWrapper0 = new JPanel(new GridBagLayout());
        buttonWrapper0.setOpaque(false);
        JPanel buttonWrapper1 = new JPanel(new FlowLayout(FlowLayout.RIGHT,5,0));
        buttonWrapper1.setOpaque(false);
        {
            ToolbarButton hide = new ToolbarButton("arrow_down", tlm);
            hide.setHintText("Hide Board");
            hide.setPreferredHintPos(Hint.LEFT);

            hide.addActionListener(e -> close());
            buttonWrapper1.add(hide);
        }
        buttonWrapper0.add(buttonWrapper1);
        header.add(buttonWrapper0, BorderLayout.EAST);

        navbar.getHint().setPreferredPos(Hint.RIGHT);
        navbar.getHint().setOutDelay(5);

        final HashMap<String, Color> colors = navbar.getColorMap();
        tlm.addThemeChangeListener(t -> {
            navbar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, Math.max(t.getInteger(1, "ToolBoard.navbar.border.thickness"),0), t.getColor(new Color(200, 200, 200), "ToolBoard.navbar.border.color")));

            colors.put("background",t.getColor(Color.WHITE, "ToolBoard.navbar.background","Navigator.background"));
            colors.put("item.background",t.getColor(new Color(0,0,0,0), "ToolBoard.navbar.item.background","Navigator.item.background"));
            colors.put("item.selected.background",t.getColor(Color.BLUE, "ToolBoard.navbar.item.selected.background","Navigator.item.selected.background","ToolBoard.navbar.item.background","Navigator.item.background"));
            colors.put("item.rollover.background",t.getColor(new Color(0,0,0,0), "ToolBoard.navbar.item.hover.background","Navigator.item.hover.background","ToolBoard.navbar.item.background","Navigator.item.background"));

            navbar.setSelectionStyle(t.getString("ToolBoard.navbar.item.selectionStyle","Navigator.item.selectionStyle","default:FULL"));
            navbar.setSelectionLineThickness(Math.max(t.getInteger(2,"ToolBoard.navbar.item.selectionLineThickness","Navigator.item.selectionLineThickness"), 0));
            navbar.repaint();
        });

        resizer = new ComponentResizer(this);
        resizer.setResizable(true, false, false, false);
        resizer.setEnabled(false);
    }

    public void toggle() {
        if(!open) open(); else close();
    }

    public void open() {
        if(lastOpenedBoard != null) open(lastOpenedBoard);
    }

    public void open(ToolBoard board) {
        this.removeAll();
        this.add(header, BorderLayout.NORTH);
        headerLabel.setText(board.getName());
        headerLabel.setIconName(board.getIconName());
        this.add(navbar, BorderLayout.WEST);
        this.add(board, BorderLayout.CENTER);
        navbar.setSelected(board.navbarItem);
        lastOpenedBoard = board;
        this.revalidate();
        this.repaint();
        navbar.revalidate();
        navbar.repaint();
        open = true;
        resizer.setEnabled(true);
    }

    public void close() {
        this.removeAll();
        this.revalidate();
        this.repaint();
        open = false;
        resizer.setEnabled(false);
        this.setPreferredSize(null);
    }

    public ToolBoard getLastOpenedBoard() {
        return lastOpenedBoard;
    }

    public void setLastOpenedBoard(ToolBoard lastOpenedBoard) {
        this.lastOpenedBoard = lastOpenedBoard;
    }

    public NavigatorMaster getNavbar() {
        return navbar;
    }
}
