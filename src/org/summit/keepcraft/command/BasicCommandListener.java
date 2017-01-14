package org.summit.keepcraft.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.summit.keepcraft.Chat;
import org.summit.keepcraft.data.DataCache;
import org.summit.keepcraft.data.models.Plot;
import org.summit.keepcraft.data.models.User;
import org.summit.keepcraft.data.models.UserFaction;
import org.summit.keepcraft.data.models.UserPrivilege;

public class BasicCommandListener extends CommandListener
{
    @Override
	protected boolean handle(String commandName, CommandSender commandSender, String[] args) 
	{
        Player p = (Player) commandSender;
		User sender = DataCache.retrieve(User.class, commandSender.getName());
		int privilege = sender.getPrivilege();
		
		// Char info
		if((commandName.equalsIgnoreCase("who")) && args.length == 1)
		{
			String targetName = args[0];
			User target = DataCache.retrieve(User.class, targetName);
			
			if(target == null)
			{
				commandSender.sendMessage(Chat.Failure + "That user does not exist"); // no user
				return true;
			}
			
			if(privilege == UserPrivilege.ADMIN)
			{
				String[] messages = target.getPrivateInfo().split("\n");
				for(int i = 0; i < messages.length; i++) commandSender.sendMessage(Chat.RequestedInfo + messages[i]); // all info
			}
			else
			{
				String[] messages = target.getInfo().split("\n");
				for(int i = 0; i < messages.length; i++) commandSender.sendMessage(Chat.RequestedInfo + messages[i]); // all info
			}
			
			return true;
		}
		// Full server listing
		else if(commandName.equalsIgnoreCase("who") && args.length == 0)
		{
			Collection<User> allUsers = DataCache.retrieveAll(User.class);
			
			List<User> adminUsers = new ArrayList<User>();
			List<User> redUsers = new ArrayList<User>();
			List<User> blueUsers = new ArrayList<User>();
            List<User> greenUsers = new ArrayList<User>();
			List<User> otherUsers = new ArrayList<User>();
			
			for(User user : allUsers)
			{
				if(user.getPrivilege() == UserPrivilege.ADMIN) 			adminUsers.add(user);
				else if(user.getFaction() == UserFaction.FactionRed) 	redUsers.add(user);
				else if(user.getFaction() == UserFaction.FactionBlue) 	blueUsers.add(user);
                else if(user.getFaction() == UserFaction.FactionGreen) 	greenUsers.add(user);
				else													otherUsers.add(user);
			}
			
			String message = "Online players:\n";
			message += this.getPlayersServerListing(redUsers);
			message += this.getPlayersServerListing(blueUsers);
            message += this.getPlayersServerListing(greenUsers);
			message += this.getPlayersServerListing(adminUsers);
			message += this.getPlayersServerListing(otherUsers);

			String[] messages = message.split("\n");
			for(int i = 0; i < messages.length; i++) commandSender.sendMessage(Chat.RequestedInfo + messages[i]); // all info

			return true;
		}
		else if(commandName.equalsIgnoreCase("die") && args.length == 0)
		{
			p.setHealth(0);
			return true;
		}
        else if(commandName.equalsIgnoreCase("map") || commandName.equalsIgnoreCase("rally"))
        {
            if(args.length == 0)
            {
                List<Plot> allPlots = new ArrayList(DataCache.retrieveAll(Plot.class));                
                Collections.sort(allPlots, new Comparator() {
                    @Override
                    public int compare(Object a, Object b)
                    {
                        Plot plot1 = (Plot) a;
                        Plot plot2 = (Plot) b;
                        if(plot1.getOrderNumber() == plot2.getOrderNumber())        return 0;
                        else if(plot1.getOrderNumber() > plot2.getOrderNumber())    return 1;
                        else                                                        return -1;
                    }
                });
                // Now sorted by order number

                String message = "Outposts:\n";

                for(Plot plot : allPlots) // filter out uncapturable plots
                {
                    if(plot.getOrderNumber() != -1)
                    {
                        String status;
                    
                        if(plot.getProtection().isCapturable())
                        {
                            status = (plot.getProtection().isCaptureInProgress()) ? "Under attack" : "Secured";
                        }    
                        else
                        {
                            status = "Base";
                            if(!plot.isTNTable()) status += " (Immune)";
                        }
                        
                        message += Chat.RequestedInfo + "" + plot.getOrderNumber() + ": " + plot.getColoredName() + Chat.RequestedInfo + " - " + status + "\n";
                    }
                }

                String[] messages = message.split("\n");
                for(int i = 0; i < messages.length; i++) commandSender.sendMessage(Chat.RequestedInfo + messages[i]);

                return true;
            }
            else if(args.length > 0)
            {
                Plot currentPlot = sender.getCurrentPlot();
                if(currentPlot == null || currentPlot.getOrderNumber() == -1 || !currentPlot.intersectsTriggerRadius(p.getLocation())
                        || !currentPlot.isFactionProtected(sender.getFaction()) || currentPlot.getProtection().isCaptureInProgress())
                {
                    commandSender.sendMessage(Chat.Failure + "You can only rally from a secured rally point");
                    return true;
                }
                
                // Teleport to a plot
                int orderNumber;
                try
                {
                    orderNumber = Integer.parseInt(args[0]);
                }
                catch(Exception e)
                {
                    commandSender.sendMessage(Chat.Failure + "Use /map to find the number of the area you wish to rally to");
                    return true;
                }
                
                if(orderNumber < 1) // minimum
                {
                    commandSender.sendMessage(Chat.Failure + "Use /map to find the number of the area you wish to rally to");
                    return true;
                }
                
                Collection<Plot> allPlots = DataCache.retrieveAll(Plot.class);
                for(Plot plot : allPlots)
                {
                    if(plot.getOrderNumber() == orderNumber)
                    {
                        // Found it!
                        if(!plot.isFactionProtected(sender.getFaction()) || plot.getProtection().isCaptureInProgress())
                        {
                            commandSender.sendMessage(Chat.Failure + "That rally point has not been secured");
                            return true;
                        }
                        else if(plot == currentPlot)
                        {
                            commandSender.sendMessage(Chat.Failure + "You are already at that rally point");
                            return true;
                        }
                        p.teleport(plot.getLocation().asBukkitLocation());
                        return true;
                    }
                }
                
                // Fell through? Possibly didn't find whatever they asked for
                commandSender.sendMessage(Chat.Failure + "Use /map to find the number of the area you wish to rally to");
                return true;
            }
        }
        else if(commandName.equalsIgnoreCase("global") && args.length == 1)
        {
            if(args[0].equalsIgnoreCase("on"))
            {
                sender.setReceiveGlobalMessages(true);
                commandSender.sendMessage(Chat.Success + "Global chat enabled");
                return true;
            }
            else if(args[0].equalsIgnoreCase("off"))
            {
                sender.setReceiveGlobalMessages(false);
                commandSender.sendMessage(Chat.Success + "Global chat disabled");
                return true;
            }
        }

		return false;
	}
	
	private String getPlayersServerListing(List<User> users)
	{
		String message = "";
		for(int i = 0; i < users.size(); i++)
		{
			message += users.get(i).getColoredName();
			if((i+1) % 4 == 0)  message += "\n";
			else 				message += "   ";
		}
		if(message.length() > 0 && message.charAt(message.length()-1) != '\n') message += "\n";
		
		return message;
	}
   
}
