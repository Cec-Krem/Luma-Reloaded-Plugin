package me.krem.lumaReloaded;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.zip.Inflater;

public class ThreadAsyncLazyFileLoader extends Thread {
    private final Inflater inflater = new Inflater();
    private final LinkedList<LumaCanvas> canvasQueue = new LinkedList();
    private boolean alive = true;

    public ThreadAsyncLazyFileLoader() {
    }

    public void run() {
        try {
            while(true) {
                LumaCanvas canvas;
                synchronized(this) {
                    while(this.alive && this.canvasQueue.isEmpty()) {
                        this.wait();
                    }

                    if (!this.alive) {
                        return;
                    }

                    canvas = (LumaCanvas)this.canvasQueue.remove();
                }

                this.loadCanvasFromFile(canvas);
            }
        } catch (InterruptedException var5) {
        }
    }

    private void loadCanvasFromFile(LumaCanvas canvas) {
        System.out.println("Lazily loading " + canvas.getName());

        try {
            File file = new File(LumaReloaded.instance.getDataFolder(), "images/" + canvas.getName() + ".bin");
            if (!file.exists()) {
                Synchronizer.add(() -> {
                    canvas.setNullData();
                });
            }

            int size = (int)file.length();
            byte[] data = new byte[size];
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(data);
            dis.close();
            dis = new DataInputStream(new ByteArrayInputStream(data));
            if (dis.readInt() != 1280658753) {
                Synchronizer.add(() -> {
                    canvas.setNullData();
                });
                return;
            }

            int width = dis.readByte();
            int height = dis.readByte();
            if (width != canvas.getWidth() || height != canvas.getHeight()) {
                Synchronizer.add(() -> {
                    canvas.setNullData();
                });
                return;
            }

            int frames = Settings.ANIMATIONS ? dis.readInt() : 1;
            byte[] uncompressedData = new byte[16384 * width * height * frames];
            this.inflater.reset();
            this.inflater.setInput(data, 10, data.length - 10);
            this.inflater.inflate(uncompressedData);
            Synchronizer.add(() -> {
                canvas.setData(frames, uncompressedData);
            });
        } catch (Exception var10) {
            var10.printStackTrace();
            Synchronizer.add(() -> {
                canvas.setNullData();
            });
        }

    }

    public synchronized void shutdown() {
        this.alive = false;
        this.notifyAll();
    }

    public synchronized void addCanvasToLoad(LumaCanvas canvas) {
        this.canvasQueue.add(canvas);
        this.notifyAll();
    }
}
