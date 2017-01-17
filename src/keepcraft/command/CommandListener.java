package keepcraft.command;

import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public abstract class CommandListener implements CommandExecutor {

    protected static Logger logger = Logger.getLogger("Minecraft");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName();
        //logger.info("KC: Working on command: " + commandName);
        return handle(commandName.toLowerCase(), sender, args);
    }

    protected abstract boolean handle(String commandName, CommandSender commandSender, String[] args);

}
