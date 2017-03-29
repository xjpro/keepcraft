package keepcraft.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import keepcraft.data.models.*;
import keepcraft.services.*;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BasicCommandListener extends CommandListener {

	private final UserService userService;
	private final PlotService plotService;
	private final RallyService rallyService;
	private final TeamService teamService;
	private final ChatService chatService;

	public BasicCommandListener(UserService userService, PlotService plotService, RallyService rallyService, TeamService teamService, ChatService chatService) {
		this.userService = userService;
		this.plotService = plotService;
		this.rallyService = rallyService;
		this.teamService = teamService;
		this.chatService = chatService;
	}

	@Override
	protected boolean handle(String commandName, CommandSender commandSender, String[] args) {
		Player player = (Player) commandSender; // todo not always a player
		User sender = userService.getOnlineUser(commandSender.getName());
		UserPrivilege privilege = sender.getPrivilege();

		// Char info
		if ((commandName.equalsIgnoreCase("who")) && args.length == 1) {
			String targetName = args[0];
			User target = userService.getUser(targetName);

			if (target == null) {
				chatService.sendFailureMessage(sender, String.format("'%s' is not a known user", targetName));
				return true;
			}

			if (privilege == UserPrivilege.ADMIN) {
				String[] messages = target.getPrivateInfo().split("\n");
				for (String message : messages) {
					chatService.sendInfoMessage(sender, message); // all info
				}
			} else {
				String[] messages = target.getInfo().split("\n");
				for (String message : messages) {
					chatService.sendInfoMessage(sender, message); // all info
				}
			}

			return true;
		}
		// Full server listing of users online and offline
		else if (commandName.equalsIgnoreCase("who") && args.length == 0) {
			Collection<User> allUsers = userService.getUsers();

			List<User> redUsers = allUsers.stream()
					.filter(user -> user.getTeam() == UserTeam.RED && !user.isAdmin())
					.sorted(Comparator.comparing(User::getName))
					.collect(Collectors.toList());

			List<User> blueUsers = allUsers.stream()
					.filter(user -> user.getTeam() == UserTeam.BLUE && !user.isAdmin())
					.sorted(Comparator.comparing(User::getName))
					.collect(Collectors.toList());

			List<User> greenUsers = allUsers.stream()
					.filter(user -> user.getTeam() == UserTeam.GREEN && !user.isAdmin())
					.sorted(Comparator.comparing(User::getName))
					.collect(Collectors.toList());

			String message = "Teams:\n";
			message += this.getPlayersServerListing(redUsers);
			message += this.getPlayersServerListing(blueUsers);
			message += this.getPlayersServerListing(greenUsers);

			String[] messages = message.split("\n");
			for (String message1 : messages) {
				chatService.sendInfoMessage(sender, message1);
			}
			return true;
		} else if (commandName.equalsIgnoreCase("die") && args.length == 0) {
			player.setHealth(0);
			return true;
		} else if (commandName.equalsIgnoreCase("hide")) {
			if (Armor.isWearingFullLeatherArmor(player)) {
				if (sender.isHiding()) {
					// Unhide
					teamService.removeStealth(sender, player);
					sender.setHiding(false);
					chatService.sendSuccessMessage(sender, "Your name is now visible");
				} else {
					// Hide
					teamService.addStealth(sender, player);
					sender.setHiding(true);
					chatService.sendSuccessMessage(sender, "Your name is now hidden");
				}
			} else {
				chatService.sendFailureMessage(sender, "You must be wearing full leather armor to hide");
			}
			return true;
		} else if (commandName.equalsIgnoreCase("map") || commandName.equalsIgnoreCase("rally")) {
			//if (args.length == 0) {
			ArrayList<Plot> allPlots = new ArrayList(plotService.getPlots());
			allPlots.sort((plot1, plot2) -> {
				if (plot1.getOrderNumber() == plot2.getOrderNumber()) {
					return 0;
				} else if (plot1.getOrderNumber() > plot2.getOrderNumber()) {
					return 1;
				} else {
					return -1;
				}
			});
			// Now sorted by order number

			String message = "Bases and outposts:\n";

			for (Plot plot : allPlots) {
				Location plotLocation = plot.getLocation();
				String status;
				//String orderNumber;
				String locationString = "";
				if (plot.isBasePlot()) {
					status = plot.isAttackInProgress() ? "Under attack" : "Base";
					//orderNumber = "B";
				} else if (plot.getProtection().isCapturable()) {
					status = plot.isAttackInProgress() ? "Under attack" : "Secured";
					//orderNumber = Integer.toString(plot.getOrderNumber());
					if (plot.getProtection().getType() == sender.getTeam().getId()) {
						locationString = String.format("(%s, %s, %s)", plotLocation.getBlockX(), plotLocation.getBlockY(), plotLocation.getBlockZ());
					}
				} else {
					continue; // Not part of the map or rallyable
				}

				message += String.format("%s%s%s - %s %s\n", ChatService.RequestedInfo, plot.getColoredName(), ChatService.RequestedInfo, status, locationString);
			}

			chatService.sendInfoMessage(sender, message);
			return true;
			//}
//			else if (args.length > 0) {
//
//				// /rally #?
//				int orderNumber = 0;
//				try {
//					orderNumber = Integer.parseInt(args[0]);
//				} catch (Exception e) {
//					// Leave as 0
//				}
//
//				Plot requestedPlot;
//				// Could not parse into a number, we'll just assume it's a request to base because all points except base require numbers
//				if (orderNumber < 1) {
//					Plot base = plotService.getPlots().stream().filter(plot -> plot.isBasePlot() && plot.isTeamProtected(sender.getTeam())).findFirst().orElse(null);
//					if (base == null) {
//						chatService.sendFailureMessage(sender, "Your team does not have a base to rally to");
//						return true;
//					} else {
//						requestedPlot = base;
//					}
//				} else {
//					// /rally # with a good number, try to find the plot with that order number
//					int finalOrderNumber = orderNumber;
//					requestedPlot = plotService.getPlots().stream().filter(plot -> plot.getOrderNumber() == finalOrderNumber).findFirst().orElse(null);
//				}
//
//				rallyService.rallyToPlot(sender, p, requestedPlot);
//				return true;
//			}
		} else if (commandName.equalsIgnoreCase("global") && args.length == 1) {
			if (args[0].equalsIgnoreCase("on")) {
				sender.setReceiveGlobalMessages(true);
				chatService.sendSuccessMessage(sender, "Global chat enabled");
				return true;
			} else if (args[0].equalsIgnoreCase("off")) {
				sender.setReceiveGlobalMessages(false);
				chatService.sendSuccessMessage(sender, "Global chat disabled");
				return true;
			}
		}

		return false;
	}

	private String getPlayersServerListing(List<User> users) {
		String message = "";
		for (int i = 0; i < users.size(); i++) {
			message += users.get(i).getColoredName();
			if ((i + 1) % 4 == 0) {
				message += "\n";
			} else {
				message += "   ";
			}
		}
		if (message.length() > 0 && message.charAt(message.length() - 1) != '\n') {
			message += "\n";
		}

		return message;
	}

}
