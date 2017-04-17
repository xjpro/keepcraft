package keepcraft.command;

import keepcraft.Keepcraft;
import keepcraft.data.models.*;
import keepcraft.services.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommandListener extends CommandListener {

	private final UserService userService;
	private final PlotService plotService;
	private final TeamService teamService;
	private final ContainerService containerService;
	private final ChatService chatService;

	public AdminCommandListener(UserService userService, PlotService plotService, TeamService teamService, ContainerService containerService, ChatService chatService) {
		this.userService = userService;
		this.plotService = plotService;
		this.teamService = teamService;
		this.containerService = containerService;
		this.chatService = chatService;
	}

	@Override
	protected boolean handle(String commandName, CommandSender commandSender, String[] args) {

		ChatParticipant sender;
		if (commandSender instanceof Player) {
			sender = userService.getOnlineUser(commandSender.getName());
		} else {
			sender = new ConsoleUser();
		}

		if (!commandSender.isOp() && !sender.isAdmin()) return false;

		// Promote
		if (commandName.equals("promote")) {

			User target;
			if (args.length > 0) {
				String targetName = args[0];
				target = userService.getOnlineUser(targetName);
				if (target == null) {
					chatService.sendFailureMessage(sender, "Requested user '" + targetName + "' does not exist");
					return true;
				}
			} else if (commandSender instanceof Player) {
				target = (User) sender;
			} else {
				chatService.sendFailureMessage(sender, "Cannot promote console");
				return true;
			}

			target.setPrivilege(target.getPrivilege().getNext());
			userService.updateUser(target);

			chatService.sendSuccessMessage(sender, "Promoted " + target.getName() + " to " + target.getPrivilege());
			if (target != sender) {
				chatService.sendChangeMessage(target, "You were promoted to " + target.getPrivilege() + " status");
			}
			return true;
		} // Demote
		else if (commandName.equals("demote")) {
			User target;
			if (args.length > 0) {
				String targetName = args[0];
				target = userService.getOnlineUser(targetName);
				if (target == null) {
					chatService.sendFailureMessage(sender, "Requested user '" + targetName + "' does not exist");
					return true;
				}
			} else if (commandSender instanceof Player) {
				target = (User) sender;
			} else {
				chatService.sendFailureMessage(sender, "Cannot demote console");
				return true;
			}

			target.setPrivilege(target.getPrivilege().getPrevious());
			userService.updateUser(target);

			chatService.sendSuccessMessage(sender, "Demoted " + target.getName() + " to " + target.getPrivilege());
			if (target != sender) {
				chatService.sendChangeMessage(target, "You were demoted to " + target.getPrivilege() + " status");
			}
			return true;
		}
		// Set team
		else if (commandName.equals("setteam") && args.length == 2) {
			String targetName = args[0];
			User target = userService.getUser(targetName);

			if (target == null) {
				chatService.sendFailureMessage(sender, String.format("'%s' is not a known user", targetName));
				return true;
			}

			UserTeam userTeam = null;
			try {
				userTeam = UserTeam.getTeam(Integer.parseInt(args[1]));
			} catch (NumberFormatException e) {
				// invalid input
			}

			if (userTeam == null) {
				chatService.sendFailureMessage(sender, "Options for teams are 100 (Red) or 200 (Blue)");
				return true;
			}

			target.setTeam(userTeam);
			userService.updateUser(target);
			chatService.sendSuccessMessage(sender, String.format("Set %s to team %s", target.getName(), target.getTeam().getChatColoredNamed()));

			Player player = Bukkit.getPlayer(targetName);
			if (player != null) {
				teamService.addPlayerToTeam(userTeam, player);
				chatService.sendChangeMessage(target, String.format("Your team was changed to %s", target.getTeam().getChatColoredNamed()));
			}

			return true;
		} // Delete a user's record
		else if (commandName.equals("delete") && args.length == 1) {
			String targetName = args[0];

			User deleted = new User(targetName.trim());
			boolean success = userService.removeUser(deleted);

			if (success) {
				chatService.sendSuccessMessage(sender, "Deleted " + targetName);
			} else {
				chatService.sendFailureMessage(sender, "Requested user '" + targetName + "' does not exist (case matters)");
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
				chatService.sendFailureMessage(sender, "Plot not found");
			} else {
				if (commandSender instanceof Player) {
					((Player) commandSender).teleport(plot.getLocation());
				}
			}
			return true;
		} else if (commandName.equals("dispense")) {
			containerService.dispenseAllContainers();
			chatService.sendSuccessMessage(sender, "All applicable container loot generated");
			return true;
		}
		// Make it dawn
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
