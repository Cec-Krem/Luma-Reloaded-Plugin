package me.krem.lumaReloaded;

import java.util.Iterator;
import java.util.List;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursorCollection;

public class LumaCanvas {
    private int width;
    private int height;
    private int frames;
    private int delay;
    private byte[] backBuffer;
    private int frameIndex = 0;
    private CanvasState state;
    private final MapCursorCollection mcc;
    private final List<LumaMap> maps;
    private final String name;
    private final int baseId;
    private long lastUseTime;

    public LumaCanvas(String name, int baseId, int width, int height, int delay, List<LumaMap> maps) {
        this.state = LumaCanvas.CanvasState.DORMANT;
        this.mcc = new MapCursorCollection();
        this.name = name;
        this.width = width;
        this.height = height;
        this.baseId = baseId;
        this.delay = delay;
        this.maps = maps;

        while(this.mcc.size() != 0) {
            this.mcc.getCursor(0);
        }

        this.lastUseTime = System.currentTimeMillis();
    }

    public void drawTile(int x, int y, MapCanvas output) {
        this.lastUseTime = System.currentTimeMillis();
        output.setCursors(this.mcc);
        if (this.state == LumaCanvas.CanvasState.DORMANT) {
            LumaReloaded.lazyFileLoader.addCanvasToLoad(this);
        }

        if (this.backBuffer != null && x >= 0 && y >= 0 && x < this.width && y < this.height) {
            int bufferOffset = 16384 * (this.width * (this.height * this.frameIndex + y) + x);

            for(int i = 0; i < 16384; ++i) {
                output.setPixel(i % 128, i / 128, this.backBuffer[bufferOffset++]);
            }
        }
    }

    public void setData(int frames, byte[] data) {
        this.backBuffer = data;
        this.frames = frames;
        this.frameIndex %= frames;
        Iterator var3 = this.maps.iterator();

        while(var3.hasNext()) {
            LumaMap map = (LumaMap)var3.next();
            map.forceRedraw();
        }

        this.state = LumaCanvas.CanvasState.LOADED;
    }

    public void setNullData() {
        this.backBuffer = null;
        this.frameIndex = 0;
        Iterator var1 = this.maps.iterator();

        while(var1.hasNext()) {
            LumaMap map = (LumaMap)var1.next();
            map.forceRedraw();
        }

        this.state = LumaCanvas.CanvasState.LOADED;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void advanceFrame() {
        if (this.state != LumaCanvas.CanvasState.DORMANT) {
            if (System.currentTimeMillis() - this.lastUseTime > (long)Settings.IMAGE_IDLE_UNLOAD_TIME) {
                this.backBuffer = null;
                this.state = LumaCanvas.CanvasState.DORMANT;
            }

            if (this.frames != 0) {
                this.frameIndex = (this.frameIndex + 1) % this.frames;
                if (this.delay < 5) {
                    Iterator var1 = this.maps.iterator();

                    while(var1.hasNext()) {
                        LumaMap map = (LumaMap)var1.next();
                        map.forceDirty();
                    }
                }
            }

        }
    }

    public String getName() {
        return this.name;
    }

    public int getBaseId() {
        return this.baseId;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getFrames() {
        return this.frames;
    }

    public int getFrameIndex() {
        return this.frameIndex;
    }

    public int getDelay() {
        return this.delay;
    }

    public boolean isLoaded() {
        return this.state != LumaCanvas.CanvasState.DORMANT;
    }

    public List<LumaMap> getMaps() {
        return this.maps;
    }

    private static enum CanvasState {
        DORMANT,
        LOADING,
        LOADED;

        private CanvasState() {
        }
    }
}
