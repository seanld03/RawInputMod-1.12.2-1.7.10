package mod.seanld.rawinput;

import mod.seanld.rawinput.commands.RescanCommand;
import mod.seanld.rawinput.commands.ToggleCommand;
import mod.seanld.rawinput.keybinds.KeybindHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = RawInput.MODID, version = RawInput.VERSION, name = RawInput.NAME, acceptedMinecraftVersions = "[1.12.2]")
public class RawInput
{
    public static final String MODID = "rawinput";
	public static final String NAME = "Raw Mouse Input";
    public static final String VERSION = "1.4.2";
	public static final Logger LOGGER = LogManager.getLogger(NAME);


	@SideOnly(Side.CLIENT)
	@EventHandler
    public void init(FMLInitializationEvent event) {
		ClientCommandHandler.instance.registerCommand(new RescanCommand());
		ClientCommandHandler.instance.registerCommand(new ToggleCommand());
		Minecraft.getMinecraft().mouseHelper = new RawMouseHelper();
		FMLCommonHandler.instance().bus().register(new KeybindHandler());
		FMLCommonHandler.instance().bus().register(new RawInputHandler());

		RawInputHandler.init();
    }

	@SideOnly(Side.CLIENT)
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		KeybindHandler.init();
	}
}
