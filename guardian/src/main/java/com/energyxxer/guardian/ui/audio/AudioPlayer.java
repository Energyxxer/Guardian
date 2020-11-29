package com.energyxxer.guardian.ui.audio;

import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.display.DisplayModule;
import com.energyxxer.guardian.ui.floatingcanvas.*;
import com.energyxxer.guardian.ui.modules.FileModuleToken;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.guardian.util.ConcurrencyUtil;
import com.energyxxer.util.Disposable;
import de.ralleytn.simple.audio.Audio;
import de.ralleytn.simple.audio.AudioEvent;
import de.ralleytn.simple.audio.AudioException;
import de.ralleytn.simple.audio.BufferedAudio;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.HashMap;

import static com.energyxxer.guardian.ui.floatingcanvas.Alignment.*;
import static com.energyxxer.guardian.ui.floatingcanvas.DynamicVector.Unit.ABSOLUTE;
import static com.energyxxer.guardian.ui.floatingcanvas.DynamicVector.Unit.RELATIVE;

public class AudioPlayer extends FloatingCanvas implements DisplayModule, Disposable, KeyListener {

    private File file;
    private Audio audio;
    private boolean opened = false;
    private boolean closing = false;

    public ThemeListenerManager tlm = new ThemeListenerManager();

    private HashMap<String, Color> colors = new HashMap<>();
    protected HashMap<String, Integer> styleNumbers = new HashMap<>();


    FloatingLabel.Fixed titleLabel;
    FloatingLabel progressLabel;
    FloatingLabel.Fixed totalLabel;
    ProgressBar progressBar;

    ToggleButton loopButton;
    Button playButton;
    Button volumeButton;

    float progressBarWidth = 0.6f;
    float progressBarY = 0.8f;
    float controlsY = 0.6f;

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
                audio.open();
                audio.play();
            } catch (AudioException e) {
                e.printStackTrace();
            }
        };

        ConcurrencyUtil.runAsync(playSound);

        this.file = file;
        this.addKeyListener(this);

        FloatingPanel content = new FloatingPanel(new DynamicVector(0.6f, RELATIVE, 0.5f, RELATIVE));

        this.add(content);

        content.add(progressBar = new ProgressBar(this));

        progressLabel = new FloatingLabel.Dynamic(() -> opened ? formatTime(audio.getPosition()) : "--:--", true);
        progressLabel.getAlignment().setAlignmentX((1-progressBarWidth)/2, RIGHT + 0.2f);
        progressLabel.getAlignment().setAlignmentY(progressBarY, MIDDLE);
        content.add(progressLabel);

        totalLabel = new FloatingLabel.Fixed("--:--", true);
        totalLabel.getAlignment().setAlignmentX(progressBarWidth + (1-progressBarWidth)/2, LEFT - 0.2f);
        totalLabel.getAlignment().setAlignmentY(progressBarY, MIDDLE);
        content.add(totalLabel);

        FloatingPanel separator = new FloatingPanel(new DynamicVector(0.8f, RELATIVE, 4, ABSOLUTE));
        separator.background.setKeys("AudioPlayer.content.*.separator.background");
        separator.getAlignment().setAlignmentY(0.275f, TOP);
        content.add(separator);

        titleLabel = new FloatingLabel.Fixed(file.getName(), true);
        titleLabel.getAlignment().setAlignmentX(MIDDLE);
        titleLabel.getAlignment().setAlignmentY(0.2f, MIDDLE);
        content.add(titleLabel);

        playButton = new PlayButton(this);
        content.add(playButton);

        loopButton = new ToggleButton(tlm, "AudioPlayer.loopButton", "AudioPlayer.toggleButton", "AudioPlayer.button");
        content.add(loopButton);
        loopButton.getAlignment().setAlignmentX(0.375f, Alignment.MIDDLE);
        loopButton.getAlignment().setAlignmentY(controlsY, Alignment.MIDDLE);
        loopButton.addClickEvent(() -> {
            loop = loopButton.isEnabled();
            if(loop && !audio.isPlaying() && audio.getPosition() >= audio.getLength()) {
                audio.setPosition(0);
                audio.play();
            }
        });
        loopButton.setIconName("loop_large");

        volumeButton = new VolumeButton(this);
        content.add(volumeButton);
        volumeButton.getAlignment().setAlignmentX(0.625f, Alignment.MIDDLE);
        volumeButton.getAlignment().setAlignmentY(controlsY, Alignment.MIDDLE);

        content.background.setKeys("AudioPlayer.content.*.background");
        content.borderThickness.setKeys("AudioPlayer.content.*.border.thickness");
        content.borderColor.setKeys("AudioPlayer.content.*.border.color");
        content.cornerRadius.setKeys("AudioPlayer.content.*.cornerRadius");
        progressLabel.foreground.setKeys("AudioPlayer.label.*.foreground", "AudioPlayer.foreground");
        totalLabel.foreground.setKeys("AudioPlayer.label.*.foreground", "AudioPlayer.foreground");
        titleLabel.foreground.setKeys("AudioPlayer.label.*.foreground", "AudioPlayer.foreground");

        tlm.addThemeChangeListener(t -> {
            this.setBackground(t.getColor(Color.WHITE, "AudioPlayer.background"));
            this.setForeground(t.getColor(Color.BLACK, "AudioPlayer.foreground"));

            separator.background.themeUpdated(t);
            content.background.themeUpdated(t);
            content.borderThickness.themeUpdated(t);
            content.borderColor.themeUpdated(t);
            content.cornerRadius.themeUpdated(t);
            progressLabel.foreground.themeUpdated(t);
            totalLabel.foreground.themeUpdated(t);
            titleLabel.foreground.themeUpdated(t);

            Font font = t.getFont("AudioPlayer", "General");
            progressLabel.setFont(font);
            totalLabel.setFont(font);
            titleLabel.setFont(t.getFont("AudioPlayer.title", "AudioPlayer", "General"));
        });
    }

    @Override
    public void dispose() {
        closing = true;
        ConcurrencyUtil.runAsync(() -> audio.close());
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

    public boolean isAudioLoaded() {
        return opened;
    }

    public boolean isAudioClosing() {
        return closing;
    }

    public Audio getAudio() {
        return audio;
    }
}