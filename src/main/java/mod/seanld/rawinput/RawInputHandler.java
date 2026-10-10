package mod.seanld.rawinput;

import java.io.IOException;
import java.util.Locale;
import mod.seanld.rawinput.input.JInputMouseBackend;
import mod.seanld.rawinput.input.LinuxMouseBackend;
import mod.seanld.rawinput.input.MotionBuffer;
import mod.seanld.rawinput.input.MouseBackend;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

public class RawInputHandler {
    private static final MotionBuffer motion = new MotionBuffer();
    private static volatile boolean enabled = true;
    private static volatile boolean backendReady;
    private static volatile boolean rescanRequested = true;
    private static Thread inputThread;

    public static synchronized void init() {
        if (inputThread != null) {
            return;
        }
        inputThread = new Thread(RawInputHandler::runInput, "inputThread");
        inputThread.setDaemon(true);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            inputThread.interrupt();
            try {
                inputThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "rawInputShutdown"));
        inputThread.start();
    }

    private static void runInput() {
        MouseBackend backend = null;
        double[] delta = new double[2];
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (rescanRequested) {
                        rescanRequested = false;
                        backendReady = false;
                        motion.reset();
                        if (backend != null) {
                            backend.close();
                            backend = null;
                        }
                        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
                        if (os.contains("linux")) {
                            backend = new LinuxMouseBackend();
                        } else if (os.startsWith("windows")) {
                            backend = new JInputMouseBackend();
                        } else {
                            throw new IOException("Raw mouse input is not supported on this platform");
                        }
                        backendReady = true;
                    }
                    if (backend != null) {
                        long generation = motion.generation();
                        backend.poll(delta);
                        motion.add(generation, delta[0], delta[1]);
                    }
                } catch (IOException | RuntimeException | LinkageError e) {
                    backendReady = false;
                    motion.setCapturing(false);
                    motion.reset();
                    if (backend != null) {
                        backend.close();
                        backend = null;
                    }
                    RawInput.LOGGER.warn("Raw input unavailable; using standard mouse input", e);
                }
                Thread.sleep(backend == null ? 100 : 1);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            backendReady = false;
            motion.setCapturing(false);
            if (backend != null) {
                backend.close();
            }
        }
    }

    public static void getMouse() {
        backendReady = false;
        motion.setCapturing(false);
        motion.reset();
        rescanRequested = true;
    }

    public static boolean isRawInputActive() {
        return enabled && backendReady;
    }

    public static void updateCapture() {
        Minecraft minecraft = Minecraft.getMinecraft();
        motion.setCapturing(isRawInputActive() && minecraft.player != null
                && minecraft.currentScreen == null && minecraft.inGameHasFocus
                && Display.isCreated() && Display.isActive()
                && Mouse.isCreated() && Mouse.isGrabbed());
    }

    public static void stopCapture() {
        motion.setCapturing(false);
    }

    public static void resetMotion() {
        motion.reset();
    }

    public static void consumeMotion(int[] delta) {
        motion.consume(delta);
    }

    public static void toggleRawInput() {
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayerSP player = minecraft.player;
        enabled = !enabled;
        motion.setCapturing(false);
        motion.reset();
        if (player != null) {
            float saveYaw = player.rotationYaw;
            float savePitch = player.rotationPitch;
            if (minecraft.inGameHasFocus) {
                minecraft.mouseHelper.grabMouseCursor();
            }
            player.sendMessage(new TextComponentString(enabled ? "Toggled ON" : "Toggled OFF"));
            player.rotationYaw = saveYaw;
            player.rotationPitch = savePitch;
        }
        updateCapture();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            updateCapture();
        }
    }
}
