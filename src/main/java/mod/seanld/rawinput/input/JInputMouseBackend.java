package mod.seanld.rawinput.input;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.java.games.input.Controller;
import net.java.games.input.DirectAndRawInputEnvironmentPlugin;
import net.java.games.input.Mouse;

public final class JInputMouseBackend implements MouseBackend {
    private final List<Mouse> mice = new ArrayList<>();
    private Mouse selected;

    public JInputMouseBackend() throws IOException {
        DirectAndRawInputEnvironmentPlugin environment = new DirectAndRawInputEnvironmentPlugin();
        if (!environment.isSupported()) {
            throw new IOException("Raw mouse input is not supported on this platform");
        }
        for (Controller controller : environment.getControllers()) {
            if (controller instanceof Mouse) {
                Mouse mouse = (Mouse) controller;
                if (mouse.getX() != null && mouse.getY() != null) {
                    mice.add(mouse);
                }
            }
        }
        if (mice.isEmpty()) {
            throw new IOException("No raw mouse devices found");
        }
    }

    @Override
    public void poll(double[] motion) throws IOException {
        motion[0] = 0;
        motion[1] = 0;
        if (selected != null) {
            if (!selected.poll()) {
                throw new IOException("Raw mouse device disconnected");
            }
            motion[0] = selected.getX().getPollData();
            motion[1] = selected.getY().getPollData();
            return;
        }
        boolean available = false;
        for (Mouse mouse : mice) {
            if (!mouse.poll()) {
                continue;
            }
            available = true;
            double x = mouse.getX().getPollData();
            double y = mouse.getY().getPollData();
            if (selected == null && (Math.abs(x) > 0.1 || Math.abs(y) > 0.1)) {
                selected = mouse;
                motion[0] = x;
                motion[1] = y;
            }
        }
        if (!available) {
            throw new IOException("No raw mouse devices available");
        }
    }

    @Override
    public void close() {
        selected = null;
    }
}
