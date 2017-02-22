package keepcraft.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import keepcraft.services.ChatService;
import keepcraft.services.PlotService;
import keepcraft.services.UserService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import keepcraft.data.models.Plot;
import keepcraft.data.models.User;
import keepcraft.data.models.UserFaction;
import keepcraft.data.models.UserPrivilege;

public class BasicCommandListener extends CommandListener {

	private final UserService userService;
	private final PlotService plotService;
	private final ChatService chatService;

	public BasicCommandListener(UserService userService, PlotService plotService, ChatService chatService) {
		this.userService = userService;
		this.plotService = plotService;
		this.chatService = chatService;
	}

	@Override
	protected boolean handle(String commandName, CommandSender commandSender, String[] args) {
		Player p = (Player) commandSender;
		User sender = userService.getOnlineUser(commandSender.getName());
		UserPrivilege privilege = sender.getPrivilege();

		// Char info
		if ((commandName.equalsIgnoreCase("who")) && args.length == 1) {
			String targetName = args[0];
			User target = userService.getOnlineUser(targetName);

			if (target == null) {
				chatService.sendFailureMessage(sender, "That user does not exist"); // no user
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
		} // Full server listing
		else if (commandName.equalsIgnoreCase("who") && args.length == 0) {
			Collection<User> allUsers = userService.getOnlineUsers();

			List<User> redUsers = new ArrayList<>();
			List<User> blueUsers = new ArrayList<>();
			List<User> greenUsers = new ArrayList<>();
			List<User> otherUsers = new ArrayList<>();

			for (User user : allUsers) {
				if (user.getFaction() == UserFaction.FactionRed) {
					redUsers.add(user);
				} else if (user.getFaction() == UserFaction.FactionBlue) {
					blueUsers.add(user);
				} else if (user.getFaction() == UserFaction.FactionGreen) {
					greenUsers.add(user);
				} else {
					otherUsers.add(user);
				}
			}

			String message = "Online players:\n";
			message += this.getPlayersServerListing(redUsers);
			message += this.getPlayersServerListing(blueUsers);
			message += this.getPlayersServerListing(greenUsers);
			message += this.getPlayersServerListing(otherUsers);

			String[] messages = message.split("\n");
			for (String message1 : messages) {
				chatService.sendInfoMessage(sender, message1); // all info
			}
			return true;
		} else if (commandName.equalsIgnoreCase("die") && args.length == 0) {
			p.setHealth(0);
			return true;
		} else if (commandName.equalsIgnoreCase("map") || commandName.equalsIgnoreCase("rally")) {
			if (args.length == 0) {
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
					String status;
					String orderNumber;
					if (plot.isBasePlot()) {
						status = "Base";
						orderNumber = "B";
//						if (plot.isImmuneToAttack()) {
//							status += " (Not capturable, immune to attack outside of siege hours)";
//						}
					} else if (plot.canBeRalliedTo()) {
						status = plot.getProtection().isCaptureInProgress() ? "Under attack" : "Secured";
						orderNumber = Integer.toString(plot.getOrderNumber());
					} else {
						continue; // Not part of the map or rallyable
					}

					message += ChatService.RequestedInfo + "" + orderNumber + ": " + plot.getColoredName() + ChatService.RequestedInfo + " - " + status + "\n";
				}

				String[] messages = message.split("\n");
				for (String message1 : messages) {
					chatService.sendInfoMessage(sender, message1);
				}

				return true;
			} else if (args.length > 0) {
				Plot currentPlot = sender.getCurrentPlot();

				// Ensure requester is in a plot that can be rallied from
				if (currentPlot == null || !currentPlot.canBeRalliedTo() || !currentPlot.isFactionProtected(sender.getFaction())) {
					chatService.sendFailureMessage(sender, "You can only rally from a secured rally point");
					return true;
				} else if (!currentPlot.isInTriggerRadius(p.getLocation())) {
					chatService.sendFailureMessage(sender, "Move closer to center of point to rally");
					return true;
				}

				// Requester is in the trigger radius of a plot that can be rallied from
				// Parse their destination

				// /rally #?
				int orderNumber = 0;
				try {
					orderNumber = Integer.parseInt(args[0]);
				} catch (Exception e) {
					// Leave as 0
				}

				// Could not parse into a number, we'll just assume it's a request to base because all points except base require numbers
				if (orderNumber < 1) {
					Plot base = plotService.getPlots().stream().filter(plot -> plot.isBasePlot() && plot.isFactionProtected(sender.getFaction())).findFirst().orElse(null);
					if (base == null) {
						chatService.sendFailureMessage(sender, "Your team does not have a base to rally to");
					} else {
						p.teleport(base.getLocation());
					}
					return true;
				}

				// /rally # with a good number, try to find the plot with that order number

				int finalOrderNumber = orderNumber;
				Plot requestedPlot = plotService.getPlots().stream().filter(plot -> plot.getOrderNumber() == finalOrderNumber).findFirst().orElse(null);

				// Doesn't exist
				if (requestedPlot == null) {
					chatService.sendFailureMessage(sender, "Use /map to find the number of the area you wish to rally to");
					return true;
				}
				// Exists but is either not owned by requester's team or is currently being contested
				else if (!requestedPlot.isFactionProtected(sender.getFaction()) || requestedPlot.getProtection().isCaptureInProgress()) {
					chatService.sendFailureMessage(sender, "That rally point has not been secured");
					return true;
				}
				// Exists but requester is already in the plot
				else if (requestedPlot == currentPlot) {
					chatService.sendFailureMessage(sender, "You are already at that rally point");
					return true;
				}

				p.teleport(requestedPlot.getLocation());
				return true;
			}
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
