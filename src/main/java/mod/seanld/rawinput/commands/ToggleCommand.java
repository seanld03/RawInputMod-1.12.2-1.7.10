package mod.seanld.rawinput.commands;

import mod.seanld.rawinput.RawInputHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class ToggleCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "rawinput";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Toggles Raw Input (/rawinput)";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        RawInputHandler.toggleRawInput();
    }
    @Override
    public int getRequiredPermissionLevel() {
        return -1;
    }
}
