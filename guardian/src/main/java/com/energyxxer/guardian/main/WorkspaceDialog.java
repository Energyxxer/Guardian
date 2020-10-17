package com.energyxxer.guardian.main;

import com.energyxxer.guardian.global.Preferences;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.styledcomponents.StyledButton;
import com.energyxxer.guardian.ui.styledcomponents.StyledFileField;
import com.energyxxer.guardian.ui.styledcomponents.StyledLabel;
import com.energyxxer.guardian.ui.theme.change.ThemeChangeListener;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.Padding;
import com.energyxxer.xswing.ScalableDimension;
import com.energyxxer.xswing.XFileField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Allows the user to choose a workspace location for their projects.
 */
public class WorkspaceDialog {
    private static boolean initialized = false;

    private static final int WIDTH = 400;
    private static final int HEIGHT = 140;

    private static JDialog dialog = new JDialog(GuardianWindow.jframe);

    private static JPanel pane = new JPanel(new BorderLayout());
    private static JPanel content = new JPanel(new BorderLayout());
    private static JPanel instructions = new JPanel();
    private static JPanel input = new JPanel(new FlowLayout(FlowLayout.CENTER));
    private static StyledFileField field = new StyledFileField(XFileField.OPEN_DIRECTORY, Guardian.core.getDefaultWorkspace(), "WorkspaceDialog");

    private static final ThemeListenerManager tlm = new ThemeListenerManager();

    private static JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    private static StyledButton okay = new StyledButton("OK","WorkspaceDialog", tlm);
    private static StyledButton cancel = new StyledButton("Cancel","WorkspaceDialog", tlm);

    private static boolean valid = false;

    private static void initialize() {
        pane.setPreferredSize(new ScalableDimension(WIDTH, HEIGHT));
        pane.add(new Padding(10),BorderLayout.NORTH);
        pane.add(new Padding(25),BorderLayout.WEST);
        pane.add(new Padding(25),BorderLayout.EAST);
        pane.add(new Padding(10),BorderLayout.SOUTH);
        pane.add(content, BorderLayout.CENTER);

        content.setOpaque(false);
        instructions.setOpaque(false);
        instructions.add(
                new StyledLabel(
                        "<html>Specify the desired workspace directory.<br>This is where all your projects are going to be saved.</html>","WorkspaceDialog", null),
                FlowLayout.LEFT);
        content.add(instructions, BorderLayout.NORTH);
        ThemeChangeListener.addThemeChangeListener(t ->
                pane.setBackground(t.getColor(new Color(235, 235, 235), "WorkspaceDialog.background","Dialog.background"))
        );

        input.setOpaque(false);
        input.add(field, BorderLayout.CENTER);

        field.getField().getDocument().addUndoableEditListener(e -> validateInput());

        content.add(input, BorderLayout.CENTER);

        buttons.setOpaque(false);
        buttons.add(okay);
        buttons.add(cancel);
        content.add(buttons, BorderLayout.SOUTH);

        okay.addActionListener(e -> submit());

        cancel.addActionListener(e -> dialog.setVisible(false));


        //<editor-fold desc="Enter key event">
        content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submit");
        content.getActionMap().put("submit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submit();
            }
        });
        //</editor-fold>

        dialog.setContentPane(pane);
        dialog.setResizable(false);

        dialog.setTitle("Setup Workspace");

        dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);

        initialized = true;
    }

    private static void submit() {
        if(!valid) return;

        //field.getFile().mkdirs();
        Preferences.put("workspace_dir",field.getFile().getAbsolutePath());
        GuardianWindow.projectExplorer.refresh();
        dialog.setVisible(false);
    }

    private static void validateInput() {
        valid = true;
        okay.setEnabled(valid);
    }

    public static void prompt() {
        SwingUtilities.invokeLater(() -> {
            if (!initialized) {
                initialize();
            }

            field.setFile(Preferences.getWorkspace());
            validateInput();
            dialog.pack();

            Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
            center.x -= dialog.getWidth()/2;
            center.y -= dialog.getHeight()/2;

            dialog.setLocation(center);

            dialog.setVisible(true);
        });
    }
}