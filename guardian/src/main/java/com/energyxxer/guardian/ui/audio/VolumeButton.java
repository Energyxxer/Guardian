package com.energyxxer.guardian.ui.audio;

import com.energyxxer.guardian.ui.floatingcanvas.Alignment;
import com.energyxxer.guardian.ui.floatingcanvas.DynamicVector;
import com.energyxxer.guardian.ui.floatingcanvas.FloatingComponent;
import com.energyxxer.guardian.ui.floatingcanvas.FloatingPanel;
import de.ralleytn.simple.audio.Audio;

import java.awt.*;
import java.awt.event.MouseEvent;

import static com.energyxxer.guardian.ui.floatingcanvas.DynamicVector.Unit.RELATIVE_MIN;

public class VolumeButton extends Button {

    private final AudioPlayer audioPlayer;

    private float volume = 1.0f;
    private VolumeSlider slider;

    public VolumeButton(AudioPlayer audioPlayer) {
        super(audioPlayer.tlm, "AudioPlayer.volumeButton", "AudioPlayer.button");
        this.audioPlayer = audioPlayer;
        this.add(slider = new VolumeSlider());
        this.setIconName("volume_3");

        this.addClickEvent(() -> {
            Audio audio = audioPlayer.getAudio();
            if(audio.isOpen()) {
                audio.setMute(!audio.isMuted());
                updateIcon();
            }
        });
        updateIcon();
    }

    private void updateIcon() {
        if(!audioPlayer.getAudio().isOpen()) return;
        float volume = this.volume;
        boolean muted = audioPlayer.getAudio().isMuted();
        if(muted) volume = 0;
        int volumeIndex = (int) Math.ceil(volume * 3);
        this.setIconName("volume_"+volumeIndex);
    }

    @Override
    public void paint(Graphics2D g) {
        slider.paint(g);
        children.remove(slider);
        super.paint(g);
        children.add(slider);
    }

    @Override
    public boolean isRollover() {
        return super.isRollover() || (slider != null && slider.isRollover());
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        super.mouseEntered(e);
        slider.setOpen(true);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        super.mouseExited(e);
        if(!slider.isRollover()) {
            slider.setOpen(false);
        }
    }

    @Override
    public boolean contains(Point p) {
        return super.contains(p) || (slider != null && slider.isOpen() && slider.contains(p));
    }

    @Override
    public FloatingComponent getObjectAtMousePos(Point p) {
        if(slider != null && slider.isOpen() && slider.contains(p) && !super.contains(p)) return slider;
        return this;
    }

    private void updateVolume() {
        float gain = (float) Math.log10(volume)*20;
        audioPlayer.getAudio().setVolume(gain);
    }

    private class VolumeSlider extends FloatingPanel {

        private float openScale = 0.0f;
        private boolean open = false;
        private long lastRenderedTime = System.currentTimeMillis();

        private static final float SIZE_RELATIVE_TO_BUTTON = 4f;
        private static final float OPEN_TIME = 0.1f;

        public VolumeSlider() {
            super(new DynamicVector(SIZE_RELATIVE_TO_BUTTON, DynamicVector.Unit.RELATIVE_MIN, 1f, DynamicVector.Unit.RELATIVE));
            this.getAlignment().setAlignmentX(Alignment.LEFT);

            this.background.setKeys("AudioPlayer.volumePanel.*.background","AudioPlayer.volumeButton.*.background","AudioPlayer.button.*.background");
            this.foreground.setKeys("AudioPlayer.volumePanel.*.foreground","AudioPlayer.volumeButton.*.foreground","AudioPlayer.button.*.foreground");
            this.borderThickness.setKeys("AudioPlayer.volumePanel.*.border.thickness","AudioPlayer.volumeButton.*.border.thickness","AudioPlayer.button.*.border.thickness");
            this.borderColor.setKeys("AudioPlayer.volumePanel.*.border.color","AudioPlayer.volumeButton.*.border.color","AudioPlayer.button.*.border.color");
            this.cornerRadius.setKeys("AudioPlayer.volumePanel.*.cornerRadius");

            audioPlayer.tlm.addThemeChangeListener(t -> {
                background.themeUpdated(t);
                foreground.themeUpdated(t);
                borderColor.themeUpdated(t);
                borderThickness.themeUpdated(t);
                cornerRadius.themeUpdated(t);
            });
        }

        private boolean isOpen() {
            return open;
        }

        private void setOpen(boolean open) {
            this.open = open;
        }

        @Override
        public void paint(Graphics2D g) {
            long currentTime = System.currentTimeMillis();
            float timeDelta = (currentTime - lastRenderedTime) / 1000f;
            lastRenderedTime = currentTime;

            openScale += timeDelta * (open ? 1 : -1) / OPEN_TIME;
            openScale = Math.max(Math.min(openScale, 1), 0);

            getSizeVector().setX(SIZE_RELATIVE_TO_BUTTON * openScale, DynamicVector.Unit.RELATIVE_MIN);

            if(openScale > 1/SIZE_RELATIVE_TO_BUTTON) {
                super.paint(g);
                int leftPadding = getParentBounds().width;
                int borderThickness = this.borderThickness.getCurrent(this);
                Rectangle bounds = this.getBounds();
                int cornerRadius = RELATIVE_MIN.converter.convert(this.cornerRadius.getCurrent(this), bounds.width, bounds.height);

                g.setColor(this.foreground.getCurrent(this));
                g.fillRoundRect(
                        bounds.x+borderThickness,
                        bounds.y+borderThickness,
                        (int) (leftPadding-(borderThickness*(1-volume)) + (bounds.width-borderThickness*2 - leftPadding)*volume),
                        bounds.height-borderThickness*2,
                        cornerRadius,
                        cornerRadius
                );
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            setOpen(true);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if(!VolumeButton.this.isRollover()) setOpen(false);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            mouseDragged(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
            Rectangle bounds = getBounds();

            int leftPadding = getParentBounds().width;

            volume = Math.min(Math.max((float)(e.getX() - (bounds.x + leftPadding)) / (bounds.width - leftPadding), 0), 1);

            updateVolume();
            updateIcon();
        }
    }
}
