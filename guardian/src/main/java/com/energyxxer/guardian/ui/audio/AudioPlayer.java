package com.energyxxer.guardian.ui.audio;

import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.display.DisplayModule;
import com.energyxxer.guardian.ui.floatingcanvas.*;
import com.energyxxer.guardian.ui.modules.FileModuleToken;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.guardian.util.ConcurrencyUtil;
import com.energyxxer.util.Disposable;
import com.energyxxer.util.logger.Debug;
import de.ralleytn.simple.audio.Audio;
import de.ralleytn.simple.audio.AudioEvent;
import de.ralleytn.simple.audio.AudioException;
import de.ralleytn.simple.audio.BufferedAudio;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;

import static com.energyxxer.guardian.ui.floatingcanvas.Alignment.*;
import static com.energyxxer.guardian.ui.floatingcanvas.DynamicVector.Unit.RELATIVE;

public class AudioPlayer extends FloatingCanvas implements DisplayModule, Disposable, KeyListener {

    private File file;
    private Audio audio;
    private boolean opened = false;
    private boolean closing = false;

    private ThemeListenerManager tlm = new ThemeListenerManager();

    private HashMap<String, Color> colors = new HashMap<>();
    protected HashMap<String, Integer> styleNumbers = new HashMap<>();


    FloatingLabel.Fixed titleLabel;
    FloatingLabel progressLabel;
    FloatingLabel.Fixed totalLabel;
    ProgressBar progressBar;

    private float progressBarWidth = 0.6f;
    private float progressBarY = 0.8f;

    private boolean loop = false;

    public AudioPlayer(File file) {
        this.file = file;

        Runnable playSound = () -> {
            try {
                audio = new BufferedAudio(file.toURI());
                audio.addAudioListener(l -> {
                    if(closing) return;
                    if(l.getType() == AudioEvent.Type.OPENED) {
                        opened = true;
                        totalLabel.setText(formatTime(audio.getLength()));
                        displayCaretInfo();
                        repaint();
                    } else if(l.getType() == AudioEvent.Type.REACHED_END) {
                        if(loop) {
                            audio.setPosition(0);
                            audio.play();
                        }
                    }
                });
                Debug.log("Opening");
                audio.open();
                audio.play();
            } catch (AudioException e) {
                e.printStackTrace();
            }
        };

        ConcurrencyUtil.runAsync(playSound);

        this.file = file;
        this.addKeyListener(this);

        FloatingObject content = new FloatingPanel.Aligned(new DynamicVector(0.6f, RELATIVE, 0.5f, RELATIVE), MIDDLE, MIDDLE);

        this.add(content);

        content.add(progressBar = new ProgressBar());

        progressLabel = new FloatingLabel.Dynamic(() -> opened ? formatTime(audio.getPosition()) : "--:--", true);
        progressLabel.getAlignment().setAlignmentX((1-progressBarWidth)/2, RIGHT + 0.2f);
        progressLabel.getAlignment().setAlignmentY(progressBarY, MIDDLE);
        content.add(progressLabel);

        totalLabel = new FloatingLabel.Fixed("--:--", true);
        totalLabel.getAlignment().setAlignmentX(progressBarWidth + (1-progressBarWidth)/2, LEFT - 0.2f);
        totalLabel.getAlignment().setAlignmentY(progressBarY, MIDDLE);
        content.add(totalLabel);

        titleLabel = new FloatingLabel.Fixed(file.getName(), true);
        titleLabel.getAlignment().setAlignmentX(MIDDLE);
        titleLabel.getAlignment().setAlignmentY(0.25f, MIDDLE);
        content.add(titleLabel);

        tlm.addThemeChangeListener(t -> {
            this.setBackground(t.getColor(Color.WHITE, "AudioPlayer.background"));
            this.setForeground(t.getColor(Color.BLACK, "AudioPlayer.foreground"));

            content.setBackground(t.getColor(Color.GRAY, "AudioPlayer.content.background"));

            progressBar.setBackground(t.getColor(Color.WHITE, "AudioPlayer.progressBar.background"));
            progressBar.setForeground(t.getColor(Color.WHITE, "AudioPlayer.progressBar.foreground"));
            progressBar.getSizeVector().setY(t.getInteger(8, "AudioPlayer.progressBar.thickness"));

            progressBar.setRolloverBackground(t.getColor(Color.WHITE, "AudioPlayer.progressBar.hover.background"));
            progressBar.setRolloverForeground(t.getColor(Color.WHITE, "AudioPlayer.progressBar.hover.foreground"));

            styleNumbers.put("progressBar.thickness", t.getInteger(10, "AudioPlayer.progressBar.thickness"));
            styleNumbers.put("progressBar.hover.thickness", t.getInteger(10, "AudioPlayer.progressBar.hover.thickness"));

            Font font = t.getFont("AudioPlayer", "General");
            progressLabel.setFont(font);
            progressLabel.setForeground(this.getForeground());
            totalLabel.setFont(font);
            totalLabel.setForeground(this.getForeground());

            titleLabel.setFont(t.getFont("AudioPlayer.title", "AudioPlayer", "General"));
            titleLabel.setForeground(t.getColor(Color.BLACK, "AudioPlayer.title.foreground", "AudioPlayer.foreground"));
        });
    }

    @Override
    public void dispose() {
        closing = true;
        ConcurrencyUtil.runAsync(() -> {
            Debug.log("Closing");
            audio.close();
        });
        tlm.dispose();
    }

    @Override
    public void displayCaretInfo() {
        if(!opened) {
            GuardianWindow.statusBar.setSelectionInfo("");
            GuardianWindow.statusBar.setCaretInfo("Loading Audio...");
        } else {
            GuardianWindow.statusBar.setSelectionInfo("Sample Rate: " + (int)audio.getAudioFormat().getSampleRate() + " Hz");
            GuardianWindow.statusBar.setCaretInfo(audio.getAudioFormat().getChannels() == 2 ? "Stereo" : "Mono");
        }
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public boolean canSave() {
        return false;
    }

    @Override
    public Object save() {
        return null;
    }

    @Override
    public void focus() {
        this.requestFocus();
    }

    @Override
    public boolean moduleHasFocus() {
        return this.hasFocus();
    }

    @Override
    public boolean transform(ModuleToken newToken) {
        if(newToken instanceof FileModuleToken) {
            this.file = ((FileModuleToken) newToken).getFile();
        }
        return true;
    }

    private static String formatTime(long time) {
        int milliseconds = (int) (time % 1000);
        time /= 1000;
        int seconds = (int) (time % 60);
        time /= 60;
        int minutes = (int) time;

        return "" + leftPad(""+minutes, 2, '0') +
                ':' + leftPad(""+seconds, 2, '0') +
                '.' + leftPad(""+milliseconds, 3, '0');
    }

    private static String leftPad(String str, int minLength, char padChar) {
        int padAmount = minLength - str.length();
        if(padAmount < 0) return str;
        char[] pad = new char[padAmount];
        for(int i = 0; i < pad.length; i++) {
            pad[i] = padChar;
        }
        return new String(pad) + str;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private class ProgressBar extends FloatingPanel.Aligned {
        public ProgressBar() {
            super(new DynamicVector(0, 8), new Alignment(MIDDLE, MIDDLE, progressBarY, MIDDLE));
            this.getSizeVector().setX(progressBarWidth, RELATIVE);
        }

        @Override
        public void paint(Graphics2D g) {
            Rectangle rect = getBounds();

            g.setColor(isPressed() ? this.getPressedBackground() : isRollover() ? this.getRolloverBackground() : this.getBackground());
            g.fillRect(rect.x, rect.y, rect.width, rect.height);

            g.setColor(isPressed() ? this.getPressedForeground() : isRollover() ? this.getRolloverForeground() : this.getForeground());
            g.fillRect(rect.x, rect.y, (int) (rect.width * (opened ? (double)audio.getPosition() / audio.getLength() : 0)), rect.height);

            if(this.children != null) {
                for(FloatingObject obj : this.children) {
                    obj.paint(g);
                }
            }

            getRootCanvas().repaint();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            this.getSizeVector().setY(styleNumbers.get("progressBar.hover.thickness"));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            this.getSizeVector().setY(styleNumbers.get("progressBar.thickness"));
        }

        @Override
        public void mousePressed(MouseEvent e) {
            Rectangle bounds = getBounds();
            float percent = Math.min(Math.max((float)(e.getX() - bounds.x) / bounds.width, 0), 1);
            audio.setPosition((long) (audio.getLength()*percent));
            audio.pause();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Rectangle bounds = getBounds();
            float percent = Math.min(Math.max((float)(e.getX() - bounds.x) / bounds.width, 0), 1);
            audio.setPosition((long) (audio.getLength()*percent));
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            audio.resume();
        }
    }
}