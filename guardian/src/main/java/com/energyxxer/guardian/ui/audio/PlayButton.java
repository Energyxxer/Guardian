package com.energyxxer.guardian.ui.audio;

import com.energyxxer.guardian.ui.floatingcanvas.Alignment;
import de.ralleytn.simple.audio.Audio;

import java.awt.*;

public class PlayButton extends Button {

    private final AudioPlayer audioPlayer;

    public PlayButton(AudioPlayer audioPlayer) {
        super(audioPlayer.tlm, "AudioPlayer.playButton", "AudioPlayer.button");
        this.audioPlayer = audioPlayer;
        this.getAlignment().setAlignmentY(audioPlayer.controlsY, Alignment.MIDDLE);
        
        this.addClickEvent(() -> {
            Audio audio = audioPlayer.getAudio();
            if(audio.isOpen()) {
                if(audio.isPlaying()) {
                    audio.pause();
                } else {
                    if(audio.getPosition() >= audio.getLength()) {
                        audio.setPosition(0);
                    }
                    audio.play();
                }
            }
        });
    }

    @Override
    protected void drawButtonContents(Graphics2D g, Rectangle bounds) {
        super.drawButtonContents(g, bounds);
        Audio audio = audioPlayer.getAudio();
        if (audio.isOpen()) {
            if(audio.isPlaying()) {
                g.fillRect(
                        (int) Math.floor(bounds.x + 0.3 * bounds.width),
                        (int) Math.floor(bounds.y + 0.3 * bounds.width),
                        (int) Math.ceil(bounds.width * 0.15),
                        (int) Math.ceil(bounds.width * 0.4)
                );
                g.fillRect(
                        (int) Math.floor(bounds.x + (1-0.3-0.15) * bounds.width),
                        (int) Math.floor(bounds.y + 0.3 * bounds.width),
                        (int) Math.ceil(bounds.width * 0.15),
                        (int) Math.ceil(bounds.width * 0.4)
                );
            } else {
                g.fillPolygon(
                        new int[] {
                                (int) (bounds.x + 0.35 * bounds.width),
                                (int) (bounds.x + (1-0.25) * bounds.width),
                                (int) (bounds.x + 0.35 * bounds.width)
                        },
                        new int[] {
                                (int) (bounds.y + 0.3 * bounds.width),
                                (int) (bounds.y + 0.5 * bounds.width),
                                (int) (bounds.y + 0.7 * bounds.width)
                        },
                        3
                );
            }
        }
    }
}
