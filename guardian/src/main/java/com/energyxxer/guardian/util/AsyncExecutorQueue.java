package com.energyxxer.guardian.util;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.concurrent.Executor;

public class AsyncExecutorQueue implements Executor {
    private static int nextId;

    private final Thread thread;
    private int id = nextId++;

    private LinkedList<Runnable> tasks = new LinkedList<>();

    public AsyncExecutorQueue() {
        thread = new Thread(this::run);
        thread.setName("async-executor-thread-" + id);
        thread.start();

        tasks.add(()->{});
    }

    private void run() {
        while(true) {
            if(tasks.size() > 1) {
                Runnable task = tasks.remove(1);
                try {
                    task.run();
                } catch (Exception x) {
                    x.printStackTrace();
                }
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void execute(@NotNull Runnable task) {
        tasks.add(task);
    }
}
