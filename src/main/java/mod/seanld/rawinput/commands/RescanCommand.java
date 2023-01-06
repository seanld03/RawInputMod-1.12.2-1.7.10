package mod.seanld.rawinput.commands;

import mod.seanld.rawinput.RawInputHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class RescanCommand extends CommandBase {
	@Override
	public String getCommandName() {
		return "rescan";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "Rescans input devices: /rescan";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		RawInputHandler.rescan();
	}
	@Override
	public int getRequiredPermissionLevel() {
		return -1;
	}
}
