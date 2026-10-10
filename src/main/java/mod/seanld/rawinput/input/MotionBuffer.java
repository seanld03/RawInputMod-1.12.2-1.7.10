package mod.seanld.rawinput.input;

public final class MotionBuffer {
    private double x;
    private double y;
    private long generation;
    private boolean capturing;
    private boolean discardNext;

    public synchronized long generation() {
        return generation;
    }

    public synchronized void setCapturing(boolean value) {
        if (capturing != value) {
            capturing = value;
            reset();
        }
    }

    public synchronized void reset() {
        x = 0;
        y = 0;
        generation++;
        discardNext = capturing;
    }

    public synchronized void add(long expectedGeneration, double dx, double dy) {
        if (!capturing || expectedGeneration != generation) {
            return;
        }
        if (discardNext) {
            discardNext = false;
            return;
        }
        if (Double.isFinite(dx) && Double.isFinite(dy)) {
            x += dx;
            y += dy;
        }
    }

    public synchronized void consume(int[] motion) {
        motion[0] = (int) x;
        motion[1] = (int) y;
        x -= motion[0];
        y -= motion[1];
    }
}
