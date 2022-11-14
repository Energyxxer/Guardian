package com.energyxxer.guardian.util;

import com.energyxxer.guardian.global.Commons;
import com.energyxxer.util.logger.Debug;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ImageCache {
    private static HashMap<File, ImageCache> caches = new HashMap<>();
    private static boolean ASYNC_ENABLED = false;
    static {
        try {
            Class.forName("sun.awt.image.ToolkitImage");
            ASYNC_ENABLED = true;
            Debug.log("Asynchronous image loading is ON");
        } catch(ClassNotFoundException x) {
            ASYNC_ENABLED = false;
            Debug.log("Asynchronous image loading is OFF due to sun.awt.image being inaccessible");
        }
    }

    public final File file;
    public final long readTime;
    private Image image;

    private ImageCache(File file) {
        this.file = file;
        readTime = file.lastModified();
        if(ASYNC_ENABLED) {
            image = new AsyncImage(Commons.getIcon("image"));
            AsyncImage.executor.execute(() -> {
                if(SwingUtilities.isEventDispatchThread()) {
                    throw new IllegalStateException("Should not call new ImageCache(File file) on the Event Dispatch Thread");
                }
                try {
                    ((AsyncImage) image).setImage(ImageIO.read(file));
                } catch (IOException e) {
                    e.printStackTrace();
                    ((AsyncImage) image).setImage(Commons.getIcon("warn"));
                }
            });
        } else {
            try {
                image = ImageIO.read(file);
            } catch (IOException e) {
                image = Commons.getIcon("warn");
            }
        }
    }

    public Image getImage() {
        return image;
    }

    public boolean isUpToDate() {
        return file.exists() && file.isFile() && file.lastModified() <= readTime;
    }

    private static ArrayList<ImageCache> outdatedCaches = new ArrayList<>();

    private static long lastUpdateTime;
    private static void updateCache() {
        if(System.currentTimeMillis() - lastUpdateTime < 1000) return;
        lastUpdateTime = System.currentTimeMillis();
        outdatedCaches.clear();
        for(ImageCache cache : caches.values()) {
            if(!cache.isUpToDate()) {
                outdatedCaches.add(cache);
            }
        }

        for(ImageCache cache : outdatedCaches) {
            Debug.log("Image " + cache.file + " updated!");
            caches.remove(cache.file);
        }
        outdatedCaches.clear();
    }

    public static Image get(File file) {
        updateCache();
        ImageCache cache = null;
        if(caches.containsKey(file)) cache = caches.get(file);
        else {
            cache = new ImageCache(file);
            caches.put(file, cache);
        }
        return cache.getImage();
    }
}
