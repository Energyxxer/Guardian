package com.energyxxer.guardian.main.window.sections;

import com.energyxxer.guardian.global.Status;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.main.window.sections.tools.ConsoleBoard;
import com.energyxxer.guardian.ui.styledcomponents.StyledLabel;
import com.energyxxer.guardian.ui.theme.Theme;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.xswing.ScalableDimension;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Locale;
import java.util.TimerTask;

/**
 * Created by User on 12/15/2016.
 */
public class StatusBar extends JPanel implements MouseListener {

    private StyledLabel statusLabel;

    private Status currentStatus = null;

    private ProgressBar progressBar;

    private ExtendedStatusBar extension = new ExtendedStatusBar();

    public ThemeListenerManager tlm = new ThemeListenerManager();

    {
        this.setLayout(new BorderLayout());

        tlm.addThemeChangeListener(t ->
            SwingUtilities.invokeLater(() -> {
                this.setPreferredSize(new ScalableDimension(1, 25));
                this.revalidate();
                this.setBackground(t.getColor(new Color(235, 235, 235), "Status.background"));
                this.setBorder(
                    new CompoundBorder(
                        new MatteBorder(Math.max(t.getInteger("Status.border.thickness"),0), 0, 0, 0, t.getColor(new Color(200, 200, 200), "Status.border.color")),
                        new EmptyBorder(0,5,0,5)
                ));}
            )
        );

        JPanel progressWrapper = new JPanel(new BorderLayout());
        progressWrapper.setOpaque(false);

        statusLabel = new StyledLabel("", tlm);
        statusLabel.setIconName("info");
        this.add(statusLabel,BorderLayout.WEST);
        this.add(progressWrapper, BorderLayout.CENTER);

        progressBar = new ProgressBar();
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new ScalableDimension(200, 5));
        progressWrapper.add(progressBar, BorderLayout.EAST);

        this.add(extension,BorderLayout.EAST);

        this.addMouseListener(this);

        ConsoleBoard.registerCommandHandler("focustrack", new ConsoleBoard.CommandHandler() {
            @Override
            public String getDescription() {
                return "Track the active focused component";
            }

            @Override
            public void printHelp() {
                Debug.log();
                Debug.log("FOCUSTRACK: Tracks the active focused component");
            }

            @Override
            public void handle(String[] args, String rawArgs) {
                Timer timer = new Timer(200, a -> {
                    setStatus("Focused component: " + GuardianWindow.jframe.getFocusOwner());
                });
                timer.start();
            }
        });
    }

    public void setStatus(String text) {
        setStatus(new Status(text));
    }

    public void setStatus(Status status) {
        setStatus(status, -1);
    }

    private java.util.Timer statusTimer = new java.util.Timer();

    public void setStatus(Status status, int timeout) {
        Theme t = GuardianWindow.getTheme();

        statusLabel.setForeground(t.getColor(Color.BLACK, "Status." + status.getType().toLowerCase(Locale.ENGLISH),"General.foreground"));
        statusLabel.setIconName(status.getType().toLowerCase(Locale.ENGLISH));
        statusLabel.setText(status.getMessage());

        setProgress(status.getProgress());

        if(statusTimer != null) {
            statusTimer.cancel();
            statusTimer = null;
        }

        if(timeout > 0) {
            statusTimer = new java.util.Timer();
            statusTimer.schedule(new TimerTask() {
                public void run() {
                    GuardianWindow.dismissStatus(status);
                }
            }, timeout);
        }

        this.currentStatus = status;
    }

    public void setProgress(Float progress) {
        progressBar.setVisible(progress != null);
        if(progress != null) progressBar.setProgress(progress);
    }

    public void dismissStatus(Status status) {
        if(status == currentStatus) {
            statusLabel.setText("");
            statusLabel.setBackground(new Color(0,0,0,0));
            progressBar.setVisible(false);
        }
    }

    public void setCaretInfo(String text) {
        extension.setCaretInfo(text);
    }

    public void setSelectionInfo(String text) {
        extension.setSelectionInfo(text);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if((e.getClickCount() & 1) == 0) {
            GuardianWindow.toolBoard.toggle();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
