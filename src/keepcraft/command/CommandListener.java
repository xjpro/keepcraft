package keepcraft.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public abstract class CommandListener implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName();
        return handle(commandName.toLowerCase(), sender, args);
    }

    protected abstract boolean handle(String commandName, CommandSender commandSender, String[] args);

}
