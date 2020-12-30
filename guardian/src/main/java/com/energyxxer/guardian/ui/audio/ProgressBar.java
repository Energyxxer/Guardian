package com.energyxxer.guardian.ui.audio;

import com.energyxxer.guardian.ui.floatingcanvas.DynamicVector;
import com.energyxxer.guardian.ui.floatingcanvas.FloatingComponent;
import com.energyxxer.guardian.ui.floatingcanvas.FloatingPanel;
import com.energyxxer.guardian.ui.floatingcanvas.styles.IntStyleProperty;
import com.energyxxer.guardian.ui.theme.Theme;

import java.awt.*;
import java.awt.event.MouseEvent;

import static com.energyxxer.guardian.ui.floatingcanvas.Alignment.MIDDLE;
import static com.energyxxer.guardian.ui.floatingcanvas.DynamicVector.Unit.RELATIVE;

class ProgressBar extends FloatingPanel {
    private AudioPlayer audioPlayer;

    private IntStyleProperty thickness = new IntStyleProperty(10, "AudioPlayer.progressBar.*.thickness");
    private boolean wasPlayingBeforePress = false;

    public ProgressBar(AudioPlayer audioPlayer) {
        super(new DynamicVector(0, 8));
        this.audioPlayer = audioPlayer;

        getAlignment().setAlignmentX(MIDDLE);
        getAlignment().setAlignmentY(audioPlayer.progressBarY, MIDDLE);
        this.getSizeVector().setX(audioPlayer.progressBarWidth, RELATIVE);

        background.setKeys("AudioPlayer.progressBar.*.background");
        foreground.setKeys("AudioPlayer.progressBar.*.foreground");
    }

    @Override
    public void paint(Graphics2D g) {
        Rectangle rect = getBounds();

        g.setColor(background.getCurrent(this));
        g.fillRect(rect.x, rect.y, rect.width, rect.height);

        g.setColor(foreground.getCurrent(this));
        g.fillRect(rect.x, rect.y, (int) (rect.width * (audioPlayer.isAudioLoaded() ? (double) audioPlayer.getAudio().getPosition() / audioPlayer.getAudio().getLength() : 0)), rect.height);

        if(this.children != null) {
            for(FloatingComponent obj : this.children) {
                obj.paint(g);
            }
        }

        getRootCanvas().repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        updateHeight();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        updateHeight();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Rectangle bounds = getBounds();
        float percent = Math.min(Math.max((float)(e.getX() - bounds.x) / bounds.width, 0), 1);
        audioPlayer.getAudio().setPosition((long) (audioPlayer.getAudio().getLength()*percent));
        wasPlayingBeforePress = audioPlayer.getAudio().isPlaying();
        audioPlayer.getAudio().pause();

        updateHeight();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Rectangle bounds = getBounds();
        float percent = Math.min(Math.max((float)(e.getX() - bounds.x) / bounds.width, 0), 1);
        audioPlayer.getAudio().setPosition((long) (audioPlayer.getAudio().getLength()*percent));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(wasPlayingBeforePress) audioPlayer.getAudio().resume();
        updateHeight();
    }

    private void updateHeight() {
        this.getSizeVector().setY(thickness.getCurrent(this));
    }

    @Override
    public void themeUpdated(Theme t) {
        super.themeUpdated(t);
        thickness.themeUpdated(t);
        updateHeight();
    }
}
