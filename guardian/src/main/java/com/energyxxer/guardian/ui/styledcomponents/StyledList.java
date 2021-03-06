package com.energyxxer.guardian.ui.styledcomponents;

import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.XList;

import javax.swing.*;
import java.awt.*;

/**
 * Created by User on 12/14/2016.
 */
public class StyledList<T> extends XList<T> {

    private String namespace = null;

    private ThemeListenerManager tlm = new ThemeListenerManager();

    public StyledList() {
        this(null, null);
    }

    public StyledList(T[] options) {
        this(options, null);
    }

    public StyledList(T[] options, String namespace) {
        if(options != null) setOptions(options);
        if(namespace != null) this.setNamespace(namespace);

        tlm.addThemeChangeListener(t -> {
            if(this.namespace != null) {
                Font font = t.getFont(this.namespace+".list","General.list","General");
                setBackground               (t.getColor(Color.WHITE, this.namespace + ".list.background","General.list.background"));
                setForeground               (t.getColor(Color.BLACK, this.namespace + ".list.cell.foreground","General.list.cell.foreground","General.foreground"));
                setCellFont(font);
                setCellBackground           (t.getColor(new Color(215, 215, 215), this.namespace + ".list.cell.background","General.list.cell.background"));

                setSelectedCellBackground(  t.getColor(new Color(235, 235, 235), this.namespace + ".list.cell.selected.background","General.list.cell.selected.background"));
                setSelectedCellForeground(  t.getColor(Color.BLACK, this.namespace + ".list.cell.selected.foreground",this.namespace + ".list.cell.foreground","General.list.cell.selected.foreground","General.list.cell.foreground","General.foreground"));
                setSelectedCellBorder(
                        BorderFactory.createMatteBorder(
                        0, 0, Math.max(t.getInteger(1,this.namespace + ".list.cell.selected.border.thickness","General.list.cell.selected.border.thickness"),0), 0,
                                            t.getColor(new Color(0, 0, 0, 0), this.namespace + ".list.cell.selected.border.color","General.list.cell.selected.border.color")));
                setSelectedCellFont(font);
                setRolloverCellBackground(  t.getColor(new Color(235, 235, 235), this.namespace + ".list.cell.hover.background","General.list.cell.hover.background"));
                setRolloverCellForeground(  t.getColor(Color.BLACK, this.namespace + ".list.cell.rollover.foreground",this.namespace + ".list.cell.foreground","General.list.cell.rollover.foreground","General.list.cell.foreground","General.foreground"));
                setRolloverCellBorder(
                        BorderFactory.createMatteBorder(
                        0, 0, Math.max(t.getInteger(1,this.namespace + ".list.cell.rollover.border.thickness","General.list.cell.rollover.border.thickness"),0), 0,
                                            t.getColor(new Color(0, 0, 0, 0), this.namespace + ".list.cell.hover.border.color","General.list.cell.hover.border.color")));
                setRolloverCellFont(font);
            } else {
                Font font = t.getFont("General.list","General");
                setBackground(              t.getColor(Color.WHITE, "General.list.background"));
                setForeground(              t.getColor(Color.BLACK, "General.list.cell.foreground","General.foreground"));
                setCellFont(font);
                setCellBackground(          t.getColor(new Color(215, 215, 215), "General.list.cell.background"));

                setSelectedCellBackground(  t.getColor(new Color(235, 235, 235), "General.list.cell.selected.background"));
                setSelectedCellForeground(  t.getColor(Color.BLACK, "General.list.cell.selected.foreground", "General.list.cell.foreground","General.foreground"));
                setSelectedCellBorder(
                        BorderFactory.createMatteBorder(
                        0, 0, Math.max(t.getInteger(1,"General.list.cell.selected.border.thickness"),0), 0,
                                t.getColor(new Color(0, 0, 0, 0), "General.list.cell.selected.border")));
                setSelectedCellFont(font);
                setRolloverCellBackground(  t.getColor(new Color(235, 235, 235), "General.list.cell.hover.background"));
                setRolloverCellForeground(  t.getColor(Color.BLACK, "General.list.cell.rollover.foreground", "General.list.cell.foreground","General.foreground"));
                setRolloverCellBorder(
                        BorderFactory.createMatteBorder(
                        0, 0, Math.max(t.getInteger(1,"General.list.cell.rollover.border.thickness"),0), 0,
                                t.getColor(new Color(0, 0, 0, 0), "General.list.cell.hover.border")));
                setRolloverCellFont(font);
            }
        });
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return this.namespace;
    }
}
