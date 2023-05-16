package mod.seanld.rawinput;

import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;

public class RawMouseHelper extends MouseHelper {
    @Override
    public void mouseXYChange() {
        this.deltaX = RawInputHandler.dx;
        RawInputHandler.dx = 0;
        this.deltaY = -RawInputHandler.dy;
        RawInputHandler.dy = 0;
    }

    @Override
    public void grabMouseCursor() {
        if (Boolean.parseBoolean(System.getProperty("fml.noGrab", "false"))) return;
        Mouse.setGrabbed(true);
        this.deltaX = 0;
        RawInputHandler.dx = 0;
        this.deltaY = 0;
        RawInputHandler.dy = 0;
    }
}
