package mod.seanld.rawinput;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mod.seanld.rawinput.commands.RescanCommand;
import mod.seanld.rawinput.commands.ToggleCommand;
import mod.seanld.rawinput.keybinds.KeybindHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = RawInput.MODID, version = RawInput.VERSION, name = "Raw Input", useMetadata = true)
public class RawInput {
    public static final String MODID = "rawinput";
    public static final String VERSION = "1.4.2";

    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new RescanCommand());
        ClientCommandHandler.instance.registerCommand(new ToggleCommand());
        Minecraft.getMinecraft().mouseHelper = new RawMouseHelper();
        FMLCommonHandler.instance().bus().register(new KeybindHandler());
        MinecraftForge.EVENT_BUS.register(new RawInputHandler());

        RawInputHandler.init();
    }

    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        KeybindHandler.init();
    }
}
