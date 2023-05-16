package mod.seanld.rawinput.keybinds;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import mod.seanld.rawinput.RawInputHandler;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class KeybindHandler {

    public static KeyBinding toggleKey;
    public static KeyBinding rescanKey;

    public static void init() {
        toggleKey = new KeyBinding("Toggle Raw Input", Keyboard.CHAR_NONE, "Raw Input");
        ClientRegistry.registerKeyBinding(toggleKey);

        rescanKey = new KeyBinding("Rescan Key", Keyboard.CHAR_NONE, "Raw Input");
        ClientRegistry.registerKeyBinding(rescanKey);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (toggleKey.isPressed()) {
            RawInputHandler.toggleRawInput();
        }
        if (rescanKey.isPressed()) {
            RawInputHandler.getMouse();
        }
    }
}
