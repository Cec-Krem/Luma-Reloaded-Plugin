package me.krem.lumaReloaded;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Synchronizer implements Runnable {
    private static final ConcurrentLinkedQueue<Runnable> QUEUE = new ConcurrentLinkedQueue();
    private static final Synchronizer INSTANCE = new Synchronizer();
    private static int taskid;

    public Synchronizer() {
    }

    public static void add(Runnable r) {
        QUEUE.add(r);
    }

    public static void setTaskId(int taskId) {
        taskid = taskId;
    }

    public static int getTaskId() {
        return taskid;
    }

    public static Synchronizer instance() {
        return INSTANCE;
    }

    public void run() {
        while(!QUEUE.isEmpty()) {
            ((Runnable)QUEUE.poll()).run();
        }

    }
}
