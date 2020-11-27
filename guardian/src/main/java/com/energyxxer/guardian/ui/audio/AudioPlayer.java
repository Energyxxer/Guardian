package com.energyxxer.guardian.ui.audio;

import com.energyxxer.guardian.ui.display.DisplayModule;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.util.ConcurrencyUtil;
import com.energyxxer.util.Disposable;
import com.energyxxer.util.logger.Debug;
import de.ralleytn.simple.audio.Audio;
import de.ralleytn.simple.audio.AudioEvent;
import de.ralleytn.simple.audio.AudioException;
import de.ralleytn.simple.audio.BufferedAudio;

import javax.swing.*;
import java.io.File;

public class AudioPlayer extends JPanel implements DisplayModule, Disposable {

    private File file;
    private Audio audio;
    private boolean closing = false;

    public AudioPlayer(File file) {
        this.file = file;


        Runnable playSound = () -> {
            try {
                audio = new BufferedAudio(file.toURI());
                audio.addAudioListener(l -> {
                    if(l.getType() == AudioEvent.Type.REACHED_END) {
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

    }

    @Override
    public void dispose() {
        closing = true;
        ConcurrencyUtil.runAsync(() -> {
            Debug.log("Closing");
            audio.close();
        });
    }

    @Override
    public void displayCaretInfo() {

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
        return false;
    }

    @Override
    public boolean transform(ModuleToken newToken) {
        return false;
    }
}