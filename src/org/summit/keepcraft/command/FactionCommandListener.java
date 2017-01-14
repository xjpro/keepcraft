
package org.summit.keepcraft.command;

import java.util.Collection;
import org.bukkit.command.CommandSender;
import org.summit.keepcraft.Chat;
import org.summit.keepcraft.data.DataCache;
import org.summit.keepcraft.data.models.ServerConditions;
import org.summit.keepcraft.data.models.User;
import org.summit.keepcraft.data.models.UserFaction;

public class FactionCommandListener extends CommandListener
{
    @Override
    protected boolean handle(String commandName, CommandSender commandSender, String[] args) 
    {
        User sender = DataCache.retrieve(User.class, commandSender.getName());
        
        if(!sender.isAdmin()) return true;

		if(commandName.equals("faction"))
		{
            if(args.length >= 1)
            {
                int faction;
                try
                {
                    faction = Integer.parseInt(args[0]);
                }
                catch(NumberFormatException e)
                {
                    // invalid input
                    commandSender.sendMessage(Chat.Failure + "Faction number must be an integer");
                    return false;
                }

                if(args.length == 1)
                {
                    // stats
                }
                else if(args.length >= 2)
                {
                    if(args[1].equalsIgnoreCase("name"))
                    {
                        String name = "";
                        for(int i = 2; i < args.length; i++)
                        {
                            name += args[i] + " ";
                        }
                        ServerConditions.setFactionName(faction, name.trim());
                        commandSender.sendMessage(Chat.Success + "Faction " + faction + "'s name set to " + name);
                        return true;
                    }
                }
            }
		}
        // Red team chat
		else if(commandName.equals("1") && sender.isAdmin())
		{
            Collection<User> connectedUsers = DataCache.retrieveAll(User.class);
            String message = "";
            for(int i = 0; i < args.length; i++)
            {
                message += args[i] + " ";
            }
            Chat.sendFactionMessage(sender, connectedUsers, UserFaction.FactionRed, message);
			return true;
		}
		// Blue team chat
		else if(commandName.equals("2") && sender.isAdmin())
		{
			Collection<User> connectedUsers = DataCache.retrieveAll(User.class);
			String message = "";
			for(int i = 0; i < args.length; i++)
			{
				message += args[i] + " ";
			}
			Chat.sendFactionMessage(sender, connectedUsers, UserFaction.FactionBlue, message);
			return true;
		}
        // Green team chat
		else if(commandName.equals("3") && sender.isAdmin())
		{
			Collection<User> connectedUsers = DataCache.retrieveAll(User.class);
			String message = "";
			for(int i = 0; i < args.length; i++)
			{
				message += args[i] + " ";
			}
			Chat.sendFactionMessage(sender, connectedUsers, UserFaction.FactionGreen, message);
			return true;
		}
        
        return false;
    }
}
