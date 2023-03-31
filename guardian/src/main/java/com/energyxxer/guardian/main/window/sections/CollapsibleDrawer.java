package com.energyxxer.guardian.main.window.sections;

import com.energyxxer.guardian.ui.ToolbarButton;
import com.energyxxer.guardian.ui.styledcomponents.Padding;
import com.energyxxer.guardian.ui.styledcomponents.StyledLabel;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.Disposable;
import com.energyxxer.xswing.ComponentResizer;
import com.energyxxer.xswing.OverlayBorderPanel;
import com.energyxxer.xswing.ScalableDimension;
import com.energyxxer.xswing.hints.Hint;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CollapsibleDrawer extends OverlayBorderPanel implements Disposable {
    private ThemeListenerManager tlm;
    private boolean isOwnTLM = false;
    private JPanel header = new JPanel(new BorderLayout());
    private StyledLabel headerLabel;
    private final JPanel controlsPanel;

    private boolean open = false;
    private ComponentResizer resizer;

    private JPanel lastShownContent;

    public CollapsibleDrawer(String name, String iconName, String styleNamespace) {
        this(name, iconName, styleNamespace, new ThemeListenerManager());
        isOwnTLM = true;
    }

    public CollapsibleDrawer(String name, String iconName, String styleNamespace, ThemeListenerManager tlm) {
        super(new BorderLayout(), new Insets(ComponentResizer.DIST, 0, 0, 0));
        this.tlm = tlm;

        headerLabel = new StyledLabel("Sample Text", styleNamespace + ".header", tlm);

        this.setOpaque(false);
        this.setBorder(new EmptyBorder(ComponentResizer.DIST, 0, 0, 0));

        tlm.addThemeChangeListener(t -> {
            header.setPreferredSize(new ScalableDimension(0, Math.max(5, t.getInteger(29, styleNamespace + ".header.height"))));
            header.setBackground(t.getColor(Color.WHITE, styleNamespace + ".header.background"));
            header.setBorder(BorderFactory.createMatteBorder(Math.max(t.getInteger(1, styleNamespace + ".header.border.top.thickness", styleNamespace + ".header.border.thickness"),0), 0, Math.max(t.getInteger(1, styleNamespace + ".header.border.bottom.thickness", styleNamespace + ".header.border.thickness"),0), 0, t.getColor(new Color(200, 200, 200), styleNamespace + ".header.border.color")));
        });

        JPanel labelWrapper = new JPanel(new BorderLayout());
        labelWrapper.setOpaque(false);
        labelWrapper.add(new Padding(5, tlm, styleNamespace + ".header.label.indentation"), BorderLayout.WEST);
        labelWrapper.add(headerLabel, BorderLayout.CENTER);
        header.add(labelWrapper, BorderLayout.WEST);

        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if((e.getClickCount() & 1) == 0) toggle();
            }
        });

        headerLabel.setTextThemeDriven(false);
        headerLabel.setText(name);
        headerLabel.setIconName(iconName);

        JPanel buttonWrapper0 = new JPanel(new GridBagLayout());
        buttonWrapper0.setOpaque(false);
        controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,5,0));
        controlsPanel.setOpaque(false);
        {
            ToolbarButton hide = new ToolbarButton("toggle", tlm);
            hide.setHintText("Show/Hide");
            hide.setPreferredHintPos(Hint.ABOVE);

            hide.addActionListener(e -> toggle());
            controlsPanel.add(hide);
        }
        buttonWrapper0.add(controlsPanel);
        header.add(buttonWrapper0, BorderLayout.EAST);


        resizer = new ComponentResizer(this);
        resizer.setResizable(true, false, false, false);
        resizer.setEnabled(false);

        this.add(header, BorderLayout.NORTH);
    }

    public void toggle() {
        if(!open) open(); else close();
    }

    public void open() {
        if(lastShownContent != null) open(lastShownContent);
    }

    public void open(JPanel content) {
        if(lastShownContent != null) {
            this.remove(lastShownContent);
        }
        this.add(content, BorderLayout.CENTER);
        lastShownContent = content;
        this.revalidate();
        this.repaint();
        open = true;
        resizer.setEnabled(true);
    }

    public void close() {
        if(lastShownContent != null) {
            this.remove(lastShownContent);
        }
        this.revalidate();
        this.repaint();
        open = false;
        resizer.setEnabled(false);
        this.setPreferredSize(null);
    }

    public void addControl(JComponent component) {
        Component[] oldComponents = controlsPanel.getComponents();
        controlsPanel.removeAll();
        controlsPanel.add(component);
        for(Component oldComponent : oldComponents) {
            controlsPanel.add(oldComponent);
        }
    }

    public void setContent(JPanel content) {
        lastShownContent = content;
    }

    @Override
    public void dispose() {
        if(isOwnTLM) {
            this.tlm.dispose();
        }
        this.tlm = null;
    }
}
