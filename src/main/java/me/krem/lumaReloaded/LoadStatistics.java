package me.krem.lumaReloaded;

public class LoadStatistics {
    private static final RollingAverage FRAME_ADVANCE_NANOS;
    private static final RollingAverage CANVAS_DRAW_NANOS;
    private static final RollingAverage CUMULATIVE_FPS;
    private static long frameCounter;
    private static long canvasDrawNanosCounter;
    private static long frameAdvanceNanosCounter;
    public static int taskId;

    public LoadStatistics() {
    }

    public static void updateFrameAdvanceNanos(long nanos) {
        frameAdvanceNanosCounter += nanos;
    }

    public static long averageFrameAdvanceNanos() {
        return FRAME_ADVANCE_NANOS.getAverage();
    }

    public static void updateCanvasDrawNanos(long nanos) {
        canvasDrawNanosCounter += nanos;
    }

    public static long averageCanvasDrawNanos() {
        return CANVAS_DRAW_NANOS.getAverage();
    }

    public static void countFrame() {
        ++frameCounter;
    }

    public static long averageCumulativeFPS() {
        return CUMULATIVE_FPS.getSum() * 20L / (long)CUMULATIVE_FPS.getRingSize();
    }

    public static void tick() {
        CUMULATIVE_FPS.update(frameCounter);
        FRAME_ADVANCE_NANOS.update(frameAdvanceNanosCounter);
        CANVAS_DRAW_NANOS.update(canvasDrawNanosCounter);
        frameCounter = 0L;
        frameAdvanceNanosCounter = 0L;
        canvasDrawNanosCounter = 0L;
    }

    static {
        FRAME_ADVANCE_NANOS = new RollingAverage(Settings.STATISTICS_AVERAGE_TIME);
        CANVAS_DRAW_NANOS = new RollingAverage(Settings.STATISTICS_AVERAGE_TIME);
        CUMULATIVE_FPS = new RollingAverage(Settings.STATISTICS_AVERAGE_TIME);
        frameCounter = 0L;
        canvasDrawNanosCounter = 0L;
        frameAdvanceNanosCounter = 0L;
        taskId = 0;
    }

    private static class RollingAverage {
        private int ringSize = 0;
        private final long[] ringBuffer;
        private int ringIndex = 0;

        public RollingAverage(int historyLength) {
            this.ringBuffer = new long[historyLength];
        }

        public void update(long dataPoint) {
            this.ringBuffer[this.ringIndex] = dataPoint;
            this.ringIndex = (this.ringIndex + 1) % this.ringBuffer.length;
            if (this.ringSize < this.ringBuffer.length) {
                ++this.ringSize;
            }

        }

        public long getAverage() {
            return this.getSum() / (long)this.ringSize;
        }

        public long getSum() {
            long sum = 0L;

            for(int i = 0; i < this.ringBuffer.length; ++i) {
                sum += this.ringBuffer[i];
            }

            return sum;
        }

        public int getRingSize() {
            return this.ringSize;
        }
    }
}
