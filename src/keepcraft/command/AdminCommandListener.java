package keepcraft.command;

import keepcraft.Keepcraft;
import keepcraft.Privilege;
import keepcraft.data.models.Plot;
import keepcraft.data.models.User;
import keepcraft.data.models.UserFaction;
import keepcraft.services.ChatService;
import keepcraft.services.PlotService;
import keepcraft.services.UserService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class AdminCommandListener extends CommandListener {

	private final UserService userService;
	private final PlotService plotService;

	public AdminCommandListener(UserService userService, PlotService plotService) {
		this.userService = userService;
		this.plotService = plotService;
	}

	@Override
	protected boolean handle(String commandName, CommandSender commandSender, String[] args) {

		User userSender = null;
		if (!Objects.equals(commandSender.getName(), "CONSOLE")) {
			userSender = userService.getOnlineUser(commandSender.getName());
		}

		if (!commandSender.isOp() && !Privilege.isAdmin(userSender)) {
			return false;
		}

		// Promote
		if (commandName.equals("promote")) {

			User target;
			if (args.length > 0) {
				String targetName = args[0];
				target = userService.getOnlineUser(targetName);
				if (target == null) {
					commandSender.sendMessage(ChatService.Failure + "Requested user '" + targetName + "' does not exist");
					return true;
				}
			} else if (userSender != null) {
				target = userSender;
			} else {
				commandSender.sendMessage(ChatService.Failure + "Cannot promote console");
				return true;
			}

			target.setPrivilege(target.getPrivilege().getNext());
			userService.updateUser(target);

			commandSender.sendMessage(ChatService.Success + "Promoted " + target.getName() + " to " + userSender.getPrivilege());
			if (target != userSender) {
				commandSender.getServer().getPlayer(target.getName()).sendMessage(ChatService.Change + "You were promoted to "
						+ userSender.getPrivilege() + " status");
			}
			return true;
		} // Demote
		else if (commandName.equals("demote")) {
			User target;
			if (args.length > 0) {
				String targetName = args[0];
				target = userService.getOnlineUser(targetName);
				if (target == null) {
					commandSender.sendMessage(ChatService.Failure + "Requested user '" + targetName + "' does not exist");
					return true;
				}
			} else if (userSender != null) {
				target = userSender;
			} else {
				commandSender.sendMessage(ChatService.Failure + "Cannot demote console");
				return true;
			}

			target.setPrivilege(target.getPrivilege().getPrevious());
			userService.updateUser(target);

			commandSender.sendMessage(ChatService.Success + "Demoted " + target.getName() + " to " + userSender.getPrivilege());
			if (target != userSender) {
				commandSender.getServer().getPlayer(target.getName()).sendMessage(ChatService.Change + "You were demoted to "
						+ userSender.getPrivilege() + " status");
			}
			return true;
		}
		// Set team
		else if (commandName.equals("setfaction") && args.length == 2) {
			String targetName = args[0];
			User target = userService.getOnlineUser(targetName);

			if (target == null) {
				commandSender.sendMessage(ChatService.Failure + "Requested user '" + targetName + "' does not exist");
				return true;
			}

			int faction;
			try {
				faction = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				// invalid input
				commandSender.sendMessage(ChatService.Failure + "Options for factions are 100 or 200");
				return false;
			}

			target.setFaction(faction);
			userService.updateUser(target);

			commandSender.sendMessage(ChatService.Success + "Set " + targetName + " to faction " + UserFaction.asString(faction));
			commandSender.getServer().getPlayer(targetName).sendMessage(ChatService.Change + "Your faction was changed to " + UserFaction.asString(faction));
			return true;
		} // Delete a user's record
		else if (commandName.equals("delete") && args.length == 1) {
			String targetName = args[0];

			User deleted = new User(targetName.trim());
			boolean success = userService.removeUser(deleted);

			if (success) {
				commandSender.sendMessage(ChatService.Success + "Deleted " + targetName);
			} else {
				commandSender.sendMessage(ChatService.Failure + "Requested user '" + targetName + "' does not exist (case matters)");
			}

			return true;
		} // teleport to a plot
		else if (commandName.equals("ptp") && args.length > 0) {
			Plot plot = null;
			try {
				int orderNumber = Integer.parseInt(args[0]);
				for (Plot possiblePlot : plotService.getPlots()) {
					if (possiblePlot.getOrderNumber() == orderNumber) {// match
						plot = possiblePlot;
						break;
					}
				}
			} catch (NumberFormatException e) // not an int, do name
			{
				String name = "";
				for (String arg : args) {
					name += arg + " ";
				}
				plot = plotService.getPlot(name.trim());
			}

			if (plot == null) {
				commandSender.sendMessage(ChatService.Failure + "Plot not found");
			} else {
				if (userSender != null) {
					((Player) commandSender).teleport(plot.getLocation());
				}
			}
			return true;
		} // Make it dawn
		else if (commandName.equals("dawn") && args.length == 0) {
			Keepcraft.getWorld().setTime(0);
			return true;
		} // Make it noon
		else if (commandName.equals("noon") && args.length == 0) {
			Keepcraft.getWorld().setTime(5000);
			return true;
		} // Make it dusk
		else if (commandName.equals("dusk") && args.length == 0) {
			Keepcraft.getWorld().setTime(10000);
			return true;
		}

		return false; // Unknown command
	}
}
