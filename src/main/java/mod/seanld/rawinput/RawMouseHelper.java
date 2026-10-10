package mod.seanld.rawinput;

import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;

public class RawMouseHelper extends MouseHelper {
    private final int[] motion = new int[2];

    @Override
    public void mouseXYChange() {
        RawInputHandler.updateCapture();
        if (RawInputHandler.isRawInputActive()) {
            Mouse.getDX();
            Mouse.getDY();
            RawInputHandler.consumeMotion(motion);
            deltaX = motion[0];
            deltaY = -motion[1];
        } else {
            super.mouseXYChange();
        }
    }

    @Override
    public void grabMouseCursor() {
        super.grabMouseCursor();
        RawInputHandler.resetMotion();
        RawInputHandler.updateCapture();
    }

    @Override
    public void ungrabMouseCursor() {
        RawInputHandler.stopCapture();
        super.ungrabMouseCursor();
    }
}
