package mod.seanld.rawinput;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.java.games.input.Controller;
import net.java.games.input.DirectAndRawInputEnvironmentPlugin;
import net.java.games.input.Mouse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MouseHelper;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import org.apache.commons.lang3.ArrayUtils;

public class RawInputHandler {
    public static Controller[] controllers;
    public static Controller[] mouseControllers;
    public static Mouse mouse;
    public static int dx = 0;
    public static int dy = 0;
    boolean shouldGetMouse = false;

    private int worldJoinTimer;

    public static void init() {

        startInputThread();
    }

    public static void startInputThread() {
        Thread inputThread = new Thread(() -> {
            while (true) {
                if (mouse != null && Minecraft.getMinecraft().currentScreen == null) {
                    mouse.poll();
                    dx += (int) mouse.getX().getPollData();
                    dy += (int) mouse.getY().getPollData();
                } else if (mouse != null) {
                    mouse.poll();
                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        inputThread.setName("inputThread");
        inputThread.start();
    }

    public static void getMouse() {
        Thread getMouseThread = new Thread(() -> {
            DirectAndRawInputEnvironmentPlugin directEnv = new DirectAndRawInputEnvironmentPlugin();
            controllers = directEnv.getControllers();

            mouseControllers = null;
            mouse = null;

            for (Controller i : controllers) {
                if (i.getType() == Controller.Type.MOUSE) {
                    mouseControllers = ArrayUtils.add(mouseControllers, i);
                }
            }

            // Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Move your mouse"));

            while (mouse == null) {
                for (Controller i : mouseControllers) {
                    i.poll();
                    float mouseX = ((Mouse) i).getX().getPollData();

                    if (mouseX > 0.1f || mouseX < -0.1f) {
                        mouse = ((Mouse) i);
                        // Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Mouse Found"));
                    }
                }
            }
        });
        getMouseThread.setName("getMouseThread");
        getMouseThread.start();
    }

    public static void toggleRawInput() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        float saveYaw = player.rotationYaw;
        float savePitch = player.rotationPitch;

        if (Minecraft.getMinecraft().mouseHelper instanceof RawMouseHelper) {
            Minecraft.getMinecraft().mouseHelper = new MouseHelper();
            Minecraft.getMinecraft().mouseHelper.grabMouseCursor();
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Toggled OFF"));
        } else {
            Minecraft.getMinecraft().mouseHelper = new RawMouseHelper();
            Minecraft.getMinecraft().mouseHelper.grabMouseCursor();
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Toggled ON"));
        }
        player.rotationYaw = saveYaw;
        player.rotationPitch = savePitch;
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.ClientTickEvent event) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("yo"));
        //        if (worldJoinTimer > 0) {
        //            worldJoinTimer--;
        //            shouldGetMouse = true;
        //        }
        //        if (shouldGetMouse && worldJoinTimer == 0) {
        //            getMouse();
        //            shouldGetMouse = false;
        //            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("yo"));
        //        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.entity != null
                && event.entity instanceof EntityPlayer
                && !((EntityPlayer) event.entity).getEntityWorld().isRemote) {
            getMouse();
        }
    }
}
