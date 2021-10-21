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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by User on 2/11/2017.
 */
public class OptionDialog {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 110;

    public String result = null;

    private ThemeListenerManager tlm = new ThemeListenerManager();

    public OptionDialog(String title, String query, String[] options) {
        JDialog dialog = new JDialog(GuardianWindow.jframe);

        JPanel pane = new JPanel(new BorderLayout());
        //pane.setPreferredSize(new ScalableDimension(WIDTH, HEIGHT));
        tlm.addThemeChangeListener(t ->
                pane.setBackground(t.getColor(new Color(235, 235, 235), "OptionDialog.background"))
        );

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
            label.setStyle(Font.BOLD);

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

                for(String option : options) {
                    StyledButton button = new StyledButton(option,"OptionDialog", tlm);
                    button.addActionListener(e -> {
                        result = option;
                        tlm.dispose();
                        dialog.setVisible(false);
                    });
                    buttons.add(button);
                }

                content.add(buttons, BorderLayout.SOUTH);
            }

            pane.add(content, BorderLayout.CENTER);
        }

        pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submit");
        pane.getActionMap().put("submit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = options[0];
                tlm.dispose();
                dialog.setVisible(false);
            }
        });

        dialog.setContentPane(pane);

        dialog.setTitle(title);

        dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
                center.x -= dialog.getWidth()/2;
                center.y -= dialog.getHeight()/2;

                dialog.setLocation(center);
            }
        });

        dialog.pack();
        dialog.setVisible(true);
    }
}
