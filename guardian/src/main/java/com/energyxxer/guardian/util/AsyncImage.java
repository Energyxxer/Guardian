package com.energyxxer.guardian.util;

import com.energyxxer.guardian.global.Commons;
import sun.awt.image.ImageRepresentation;
import sun.awt.image.ToolkitImage;

import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Executor;

public class AsyncImage extends ToolkitImage {
    public static Executor executor = new AsyncExecutorQueue();

    private final AsyncImage parent;
    private ScaleKey scaleKey;
    private Image image;
    private HashMap<ScaleKey, AsyncImage> scaledInstances;
    private int modCount = 0;

    public AsyncImage(Image image) {
        this.parent = null;
        this.scaledInstances = new HashMap<>();
        setImage(image);
    }
    protected AsyncImage(AsyncImage parent, ScaleKey scaleKey) {
        this.parent = parent;
        this.scaleKey = scaleKey;
        this.image = Commons.getScaledIcon("image", scaleKey.width, scaleKey.height);
    }

    public void setImage(Image image) {
        if(parent != null) {
            throw new IllegalStateException("Cannot set image on a sub-DynamicImage");
        }
        if(this.image != image) {
            this.image = image;
            modCount++;
            for(AsyncImage subImg : scaledInstances.values()) {
                subImg.refresh();
            }
        }
    }

    private AsyncImage getRoot() {
        if(parent == null) return this;
        return parent.getRoot();
    }

    protected void refresh() {
        if(parent == null) return;
        if(SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Should not call refresh() on the Event Dispatch Thread");
        }
        Image rootImage = getRoot().image;
//        String description = "(from " + rootImage.getWidth(null) + " x " + rootImage.getHeight(null) + " to " + scaleKey.width + " x " + scaleKey.height + ")";

        Image scaledImage = rootImage.getScaledInstance(scaleKey.width, scaleKey.height, scaleKey.hints);
        if(scaledImage instanceof ToolkitImage) {
            ((ToolkitImage) scaledImage).preload(null);
        }

        image = scaledImage;
    }

    protected void refreshAsync() {
        if(parent == null) return;
        final int expectedModCount = getRoot().modCount;
        executor.execute(() -> {
            Image rootImage = getRoot().image;
//            String description = "(from " + rootImage.getWidth(null) + " x " + rootImage.getHeight(null) + " to " + scaleKey.width + " x " + scaleKey.height + " in thread " + Thread.currentThread().getName() + ")";
            if(expectedModCount == getRoot().modCount) {
                Image scaledImage = rootImage.getScaledInstance(scaleKey.width, scaleKey.height, scaleKey.hints);
                if(scaledImage instanceof ToolkitImage) {
                    ((ToolkitImage) scaledImage).preload(null);
                }
                if(expectedModCount == getRoot().modCount) {
                    image = scaledImage;
                }
            }
        });
    }

    @Override
    public Image getScaledInstance(int width, int height, int hints) {
        if(parent != null) return parent.getScaledInstance(width, height, hints);

        ScaleKey key = new ScaleKey(width, height, hints);
        if(scaledInstances.containsKey(key)) {
            return scaledInstances.get(key);
        }

        AsyncImage scaled = new AsyncImage(this, key);
        scaled.refreshAsync();
        scaledInstances.put(key, scaled);

        return scaled;
    }

    @Override
    public int getWidth(ImageObserver observer) {
        if(scaleKey != null) return scaleKey.width;
        return image.getWidth(observer);
    }

    @Override
    public int getHeight(ImageObserver observer) {
        if(scaleKey != null) return scaleKey.height;
        return image.getHeight(observer);
    }

    @Override
    public ImageProducer getSource() {
        return image.getSource();
    }

    @Override
    public Graphics getGraphics() {
        return image.getGraphics();
    }

    @Override
    public synchronized ImageRepresentation getImageRep() {
        if(image instanceof ToolkitImage) {
            return ((ToolkitImage) image).getImageRep();
        }
        return super.getImageRep();
    }

    @Override
    public int getWidth() {
        return getWidth(null);
    }

    @Override
    public int getHeight() {
        return getHeight(null);
    }



    @Override
    public Object getProperty(String name, ImageObserver observer) {
        return image.getProperty(name, observer);
    }

    private static class ScaleKey {
        public int width;
        public int height;
        public int hints;

        public ScaleKey(int width, int height, int hints) {
            this.width = width;
            this.height = height;
            this.hints = hints;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ScaleKey scaleKey = (ScaleKey) o;
            return width == scaleKey.width &&
                    height == scaleKey.height &&
                    hints == scaleKey.hints;
        }

        @Override
        public int hashCode() {
            return Objects.hash(width, height, hints);
        }
    }
}
