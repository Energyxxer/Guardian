package com.energyxxer.guardian.util;

public class ConcurrencyUtil {
    public static void runAsync(Runnable r) {
        Thread thread = new Thread(r);
        thread.start();
    }

    public static void runAsync(Runnable r, String threadName) {
        Thread thread = new Thread(r, threadName);
        thread.start();
    }
}
