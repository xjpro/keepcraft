package org.summit.keepcraft.command;

import org.bukkit.command.CommandSender;
import org.summit.keepcraft.Chat;
import org.summit.keepcraft.data.DataCache;
import org.summit.keepcraft.data.models.LootBlock;
import org.summit.keepcraft.data.models.User;

public class LootBlockCommandListener extends CommandListener
{	
    @Override
	protected boolean handle(String commandName, CommandSender commandSender, String[] args) 
	{
		User sender = DataCache.retrieve(User.class, commandSender.getName());
        
        if(!sender.isAdmin()) return true;
        
        LootBlock block = sender.getTargetLootBlock();
        
        if(block == null)
        {
            commandSender.sendMessage(Chat.Failure + "No target - punch a loot block to target it");
            return true;
        }

        if(commandName.equalsIgnoreCase("lootblock") && args.length > 1)
		{
            if(args[0].equalsIgnoreCase("output") && args.length == 2)
            {
                int output;
                try
                {
                    output = Integer.parseInt(args[1]);
                }
                catch(Exception e)
                {
                    // invalid input
                    return false;
                }
                block.stopDispensing();
                block.setOutput(output);
                block.startDispensing();
                DataCache.update(block);
                commandSender.sendMessage(Chat.Success + "Loot block output set to " + output + " per minute");
                return true;
            }
            else if(args[0].equalsIgnoreCase("type") && args.length == 2)
            {
                int type;
                try
                {
                    type = Integer.parseInt(args[1]);
                }
                catch(Exception e)
                {
                    // invalid input
                    return false;
                }
                block.stopDispensing();
                block.setType(type);
                block.startDispensing();
                DataCache.update(block);
                commandSender.sendMessage(Chat.Success + "Loot block type set to " + type);
                return true;
            }
            else if(args[0].equalsIgnoreCase("status") && args.length == 2)
            {
                int status;
                try
                {
                    status = Integer.parseInt(args[1]);
                }
                catch(Exception e)
                {
                    // invalid input
                    return false;
                }
                block.stopDispensing();
                block.setStatus(status);
                block.startDispensing();
                DataCache.update(block);
                commandSender.sendMessage(Chat.Success + "Loot block status set to " + status);
                return true;
            }
        }
		
		return false;
	}

}
