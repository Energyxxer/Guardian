package com.energyxxer.guardian.ui.dialogs;

import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.styledcomponents.StyledButton;
import com.energyxxer.guardian.ui.styledcomponents.StyledLabel;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.Padding;
import com.energyxxer.xswing.ScalableDimension;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Created by User on 2/11/2017.
 */
public class ConfirmDialog {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 110;

    public boolean result = false;

    private ThemeListenerManager tlm = new ThemeListenerManager();

    public ConfirmDialog(String title, String query) {
        JDialog dialog = new JDialog(GuardianWindow.jframe);

        JPanel pane = new JPanel(new BorderLayout());

        tlm.addThemeChangeListener(t -> pane.setBackground(t.getColor(new Color(235, 235, 235), "ConfirmDialog.background")));

        pane.add(new Padding(10), BorderLayout.NORTH);
        pane.add(new Padding(25), BorderLayout.WEST);
        pane.add(new Padding(25), BorderLayout.EAST);
        pane.add(new Padding(10), BorderLayout.SOUTH);

        {
            JPanel content = new JPanel(new BorderLayout());
            content.setOpaque(false);

            JTextPane textPane = new JTextPane();
            textPane.setEditable(false);
            textPane.setOpaque(false);

            StyledLabel label = new StyledLabel("", "ConfirmDialog", tlm); //tlm

            textPane.setFont(label.getFont());
            textPane.setBackground(label.getBackground());
            textPane.setForeground(label.getForeground());

            textPane.setText(query);

            JPanel labelWrapper = new JPanel(new BorderLayout());
            labelWrapper.setOpaque(false);
            labelWrapper.add(new Padding(25), BorderLayout.NORTH);
            labelWrapper.add(textPane, BorderLayout.CENTER);
            labelWrapper.add(new Padding(25), BorderLayout.SOUTH);
            content.add(labelWrapper, BorderLayout.CENTER);

            {
                JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                buttons.setOpaque(false);
                buttons.setMaximumSize(new ScalableDimension(Integer.MAX_VALUE, 30));

                StyledButton okButton = new StyledButton("Yes", tlm);
                okButton.addActionListener(e -> {
                    result = true;
                    dialog.setVisible(false);
                });
                buttons.add(okButton);

                StyledButton cancelButton = new StyledButton("No", tlm);
                cancelButton.addActionListener(e -> {
                    result = false;
                    dialog.setVisible(false);
                });

                buttons.add(cancelButton);
                content.add(buttons, BorderLayout.SOUTH);
            }

            pane.add(content, BorderLayout.CENTER);
        }

        pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submit");
        pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        pane.getActionMap().put("submit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = true;
                dialog.setVisible(false);
                tlm.dispose();
                dialog.dispose();
            }
        });
        pane.getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = false;
                dialog.setVisible(false);
                tlm.dispose();
                dialog.dispose();
            }
        });

        dialog.setContentPane(pane);

        pane.setPreferredSize(new ScalableDimension(Math.max(pane.getPreferredSize().width, WIDTH), Math.max(pane.getPreferredSize().height, HEIGHT)));

        dialog.pack();

        dialog.setTitle(title);

        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        center.x -= dialog.getWidth()/2;
        center.y -= dialog.getHeight()/2;

        dialog.setLocation(center);

        dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);

        dialog.setVisible(true);
    }
}
