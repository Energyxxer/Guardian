package com.energyxxer.guardian.main.window.sections;

import com.energyxxer.guardian.global.Preferences;
import com.energyxxer.guardian.langinterface.ProjectType;
import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.main.window.actions.ActionManager;
import com.energyxxer.guardian.ui.ToolbarButton;
import com.energyxxer.guardian.ui.dialogs.file_dialogs.ProjectDialog;
import com.energyxxer.guardian.ui.dialogs.settings.Settings;
import com.energyxxer.guardian.ui.misc.TipScreen;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class WelcomePane extends JPanel {

    public final TipScreen tipScreen;

    ThemeListenerManager tlm = new ThemeListenerManager();

    private JPanel tipPanel = new JPanel(new BorderLayout());
    private JPanel contentPanel = new JPanel(new BorderLayout());
    private JPanel buttonPanel = new JPanel(new GridLayout(3,2));

    private boolean nextLeft = false;

    public WelcomePane() {
        super(new GridBagLayout());
        this.setOpaque(false);
        contentPanel.setOpaque(false);
        tipPanel.setOpaque(false);
        buttonPanel.setOpaque(false);

        this.add(contentPanel);
        contentPanel.add(tipPanel, BorderLayout.NORTH);
        contentPanel.add(buttonPanel, BorderLayout.CENTER);

        tipScreen = new TipScreen(tlm);
        tipPanel.add(tipScreen, BorderLayout.CENTER);

        tlm.addThemeChangeListener(t -> {
            tipScreen.setForeground(t.getColor("TipScreen.foreground", "General.foreground"));
            tipScreen.setFont(t.getFont("TipScreen","General"));
        });

        int buttonCount = 0;

        {
            for(ProjectType projectType : ProjectType.values()) {
                ToolbarButton button = new ToolbarButton(projectType.getDefaultProjectIconName(), tlm);
                button.setText("New Project");
                button.setHintText("Create a new " + projectType.getName());
                button.addActionListener(e -> ProjectDialog.create(projectType));
                JPanel wrapper = new JPanel(new FlowLayout(getNextButtonAlignment()));
                wrapper.setOpaque(false);
                wrapper.add(button);
                buttonPanel.add(wrapper);
                buttonCount++;
            }
        }
        {
            Collection<JComponent> extraButtons = Guardian.core.createWelcomePaneButtons(tlm);

            if(extraButtons != null) {
                for(JComponent component : extraButtons) {
                    JPanel wrapper = new JPanel(new FlowLayout(getNextButtonAlignment()));
                    wrapper.setOpaque(false);
                    wrapper.add(component);
                    buttonPanel.add(wrapper);
                    buttonCount++;
                }
            }
        }
        {
            ToolbarButton button = new ToolbarButton(null, tlm);
            button.setText("Select Workspace");
            button.setHintText("Choose a location to keep your projects");
            button.addActionListener(e -> Preferences.promptWorkspace());
            JPanel wrapper = new JPanel(new FlowLayout(getNextButtonAlignment()));
            wrapper.setOpaque(false);
            wrapper.add(button);
            buttonPanel.add(wrapper);
            buttonCount++;
        }
        {
            ToolbarButton button = new ToolbarButton("documentation", tlm);
            button.setText("Documentation");
            button.setHintText("Read the language docs");
            button.addActionListener(e -> {
                ActionManager.getAction("DOCUMENTATION").perform();
            });
            JPanel wrapper = new JPanel(new FlowLayout(getNextButtonAlignment()));
            wrapper.setOpaque(false);
            wrapper.add(button);
            buttonPanel.add(wrapper);
            buttonCount++;
        }
        {
            ToolbarButton button = new ToolbarButton("cog", tlm);
            button.setText("Settings");
            button.setHintText("Manage settings");
            button.addActionListener(e -> {
                GuardianWindow.toolbar.hint.dismiss();
                Settings.show();
            });
            JPanel wrapper = new JPanel(new FlowLayout(getNextButtonAlignment()));
            wrapper.setOpaque(false);
            wrapper.add(button);
            buttonPanel.add(wrapper);
            buttonCount++;
        }
        {
            ToolbarButton button = new ToolbarButton("info", tlm);
            button.setText("About");
            button.setHintText("Learn about this build");
            button.addActionListener(e -> AboutPane.INSTANCE.setVisible(true));
            JPanel wrapper = new JPanel(new FlowLayout(getNextButtonAlignment()));
            wrapper.setOpaque(false);
            wrapper.add(button);
            buttonPanel.add(wrapper);
            buttonCount++;
        }

        buttonPanel.setLayout(new GridLayout((buttonCount+1) / 2, 2));
        //buttonPanel.add(new ToolbarButton("cog", tlm));
        //buttonPanel.add(new ToolbarButton("file", tlm));
        //buttonPanel.add(new ToolbarButton("model", tlm));
    }

    private int getNextButtonAlignment() {
        nextLeft = !nextLeft;
        if(!nextLeft) return FlowLayout.LEFT;
        return FlowLayout.RIGHT;
    }
}
