package com.energyxxer.guardian.ui.audio;

import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.display.DisplayModule;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.util.ConcurrencyUtil;
import com.energyxxer.util.Disposable;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.libraries.LibraryJavaSound;

import javax.swing.*;
import java.io.File;
import java.net.MalformedURLException;

public class AudioPlayer extends JPanel implements DisplayModule, Disposable {

    private static SoundSystem SOUND_SYSTEM;

    private File file;
    private String soundId;

    static {
        GuardianWindow.setStatus("Loading sound system...");
        SwingUtilities.invokeLater(() -> {
            try {
                SoundSystemConfig.addLibrary(LibraryJavaSound.class);
                SoundSystemConfig.setCodec("wav", CodecWav.class);
                SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
                SOUND_SYSTEM = new SoundSystem(LibraryJavaSound.class);
                GuardianWindow.setStatus("Sound system loaded");
            } catch (SoundSystemException e) {
                e.printStackTrace();
            }
        });
    }

    public AudioPlayer(File file) {
        this.file = file;

        Runnable playSound = () -> {
            try {
                soundId = SOUND_SYSTEM.quickPlay(
                        false,
                        file.toURI().toURL(),
                        file.getAbsolutePath(),
                        false,
                        0, 0, 0,
                        SoundSystemConfig.getDefaultAttenuation(),
                        SoundSystemConfig.getDefaultRolloff()
                );
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        };

        if(SOUND_SYSTEM == null) {
            SwingUtilities.invokeLater(playSound);
        } else {
            ConcurrencyUtil.runAsync(playSound);
        }

    }

    @Override
    public void dispose() {
        SOUND_SYSTEM.stop(soundId);
        SOUND_SYSTEM.unloadSound(soundId);
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