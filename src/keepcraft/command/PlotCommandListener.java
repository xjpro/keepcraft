package keepcraft.command;

import keepcraft.data.models.*;
import keepcraft.services.ChatService;
import keepcraft.services.PlotService;
import keepcraft.services.UserService;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import keepcraft.Keepcraft;
import keepcraft.Privilege;
import keepcraft.data.models.UserTeam;

public class PlotCommandListener extends CommandListener {

	private final UserService userService;
	private final PlotService plotService;

	public PlotCommandListener(UserService userService, PlotService plotService) {
		this.userService = userService;
		this.plotService = plotService;
	}

	@Override
	protected boolean handle(String commandName, CommandSender commandSender, String[] args) {
		User sender = userService.getOnlineUser(commandSender.getName());
		UserPrivilege privilege = sender.getPrivilege();

		Plot currentPlot = sender.getCurrentPlot();

		if (commandName.equalsIgnoreCase("plot")) {
			// Create a plot
			if (args.length > 1 && args[0].equals("create")) {
				// Create plot
				if (privilege == UserPrivilege.ADMIN) {
					Player p = (Player) commandSender;
					Location location = p.getLocation();
					String name = "";
					for (int i = 1; i < args.length; i++) {
						name += args[i] + " ";
					}

					plotService.createAdminPlot(new WorldPoint(location), name.trim(), Plot.DEFAULT_RADIUS);
					commandSender.sendMessage(ChatService.Success + "A new plot has been created");
					return true;
				}
			} // Past this point we should be in a plot, so break out early if not
			else if (currentPlot == null) {
				commandSender.sendMessage(ChatService.Failure + "You are not in a plot");
				return true;
			} // Get info
			else if (args.length == 0) {
				String[] messages = currentPlot.getInfo().split("\n");
				for (String message : messages) {
					commandSender.sendMessage(ChatService.RequestedInfo + message);
				}
				return true;
			} // Reset center
			else if (args[0].equalsIgnoreCase("center")) {
				if (privilege == UserPrivilege.ADMIN) {
					Player p = (Player) commandSender;
					Location loc = p.getLocation();

					currentPlot.setWorldPoint(new WorldPoint(loc));
					plotService.updatePlot(currentPlot);

					commandSender.sendMessage(ChatService.Success + "Plot center set to " + loc);
					return true;
				}
			} // Set base radius
			else if (args[0].equals("radius")) {
				if (args.length == 2) {
					if (privilege == UserPrivilege.ADMIN) {
						int radius;
						try {
							radius = Integer.parseInt(args[1]);
						} catch (Exception e) {
							// invalid input
							return false;
						}
						currentPlot.setRadius(radius);
						plotService.updatePlot(currentPlot);

						commandSender.sendMessage(ChatService.Success + "Plot radius set to " + currentPlot.getRadius());
						return true;
					}
				} else if (args[1].equalsIgnoreCase("keep") && args.length == 3) {
					if (privilege == UserPrivilege.ADMIN) {
						int radius;
						try {
							radius = Integer.parseInt(args[2]);
						} catch (Exception e) {
							Keepcraft.log("Error while changing keep radius: " + e.getMessage());
							return false;
						}
						currentPlot.getProtection().setKeepRadius(radius);
						plotService.updatePlot(currentPlot);

						commandSender.sendMessage(ChatService.Success + "Plot keep radius set to " + currentPlot.getProtection().getKeepRadius());
						return true;
					}
				} else if (args[1].equalsIgnoreCase("admin") && args.length == 3) {
					if (privilege == UserPrivilege.ADMIN) {
						int radius;
						try {
							radius = Integer.parseInt(args[2]);
						} catch (Exception e) {
							Keepcraft.log("Error while changing admin radius: " + e.getMessage());
							return false;
						}
						currentPlot.getProtection().setAdminRadius(radius);
						plotService.updatePlot(currentPlot);

						commandSender.sendMessage(ChatService.Success + "Plot admin radius set to " + currentPlot.getProtection().getAdminRadius());
						return true;
					}
				} else if (args[1].equalsIgnoreCase("trigger") && args.length == 3) {
					if (privilege == UserPrivilege.ADMIN) {
						int radius;
						try {
							radius = Integer.parseInt(args[2]);
						} catch (Exception e) {
							Keepcraft.log("Error while changing trigger radius: " + e.getMessage());
							return false;
						}
						currentPlot.getProtection().setTriggerRadius(radius);
						plotService.updatePlot(currentPlot);

						commandSender.sendMessage(ChatService.Success + "Plot trigger radius set to " + currentPlot.getProtection().getTriggerRadius());
						return true;
					}
				}
			} // end radius
			// Capturable setting
			else if (args[0].equals("capture") && args.length > 1) {
				if (args[1].equalsIgnoreCase("type")) {
					if (Privilege.canModifyPlotData(sender, currentPlot)) {
						boolean capturable;
						try {
							capturable = Boolean.parseBoolean(args[2]);
						} catch (Exception e) {
							// invalid input
							Keepcraft.log("Error while changing plot capturable flag: " + e.getMessage());
							return false;
						}

						currentPlot.getProtection().setCapturable(capturable);
						plotService.updatePlot(currentPlot);

						commandSender.sendMessage(ChatService.Success + "Plot capturable flag set to " + capturable);
						return true;
					}
				} else if (args[1].equalsIgnoreCase("time")) {
					if (Privilege.canModifyPlotData(sender, currentPlot)) {
						int captureSeconds;
						try {
							captureSeconds = Integer.parseInt(args[2]);
						} catch (Exception e) {
							Keepcraft.log("Error while changing plot capture time: " + e.getMessage());
							// invalid input
							return false;
						}

						currentPlot.getProtection().setCaptureTime(captureSeconds);
						plotService.updatePlot(currentPlot);

						commandSender.sendMessage(ChatService.Success + "Plot capture time set to " + captureSeconds + " seconds");
						return true;
					}
				}
			} else if (args[0].equals("type") && args.length > 1) {
				// Set it as protection faction
				if (args[1].equals("faction") && args.length == 3) {
					if (privilege == UserPrivilege.ADMIN) {
						int faction;
						try {
							faction = Integer.parseInt(args[2]);
						} catch (Exception e) {
							// invalid input
							Keepcraft.log("Error while setting plot type to faction: " + e.getMessage());
							return false;
						}
						currentPlot.getProtection().setType(faction);
						plotService.updatePlot(currentPlot);

						commandSender.sendMessage(ChatService.Success + "Plot protection set to faction " + UserTeam.getChatColoredName(faction));
						return true;
					}
				} // Set it as protection admin
				else if (args[1].equals("admin") && args.length == 2) {
					if (privilege == UserPrivilege.ADMIN) {
						currentPlot.getProtection().setType(PlotProtection.ADMIN);
						plotService.updatePlot(currentPlot);

						commandSender.sendMessage(ChatService.Success + "Plot protection set to admin");
						return true;
					}
				} // Set it as protection event
				else if (args[1].equals("event") && args.length == 2) {
					if (privilege == UserPrivilege.ADMIN) {
						currentPlot.getProtection().setType(PlotProtection.EVENT);
						plotService.updatePlot(currentPlot);

						commandSender.sendMessage(ChatService.Success + "Plot protection set to event");
						return true;
					}
				} // Set it as protection public
				else if (args[1].equals("public") && args.length == 2) {
					if (Privilege.canModifyPlotData(sender, currentPlot)) {
						currentPlot.getProtection().setType(PlotProtection.PUBLIC);
						plotService.updatePlot(currentPlot);

						commandSender.sendMessage(ChatService.Success + "Plot protection set to public");
						return true;
					}
				}
			} // Rename plot
			else if (args[0].equals("name") && args.length > 1) {
				if (Privilege.canModifyPlotData(sender, currentPlot)) {
					String name = "";
					for (int i = 1; i < args.length; i++) {
						name += args[i] + " ";
					}

					currentPlot.setName(name.trim());
					plotService.updatePlot(currentPlot);
					commandSender.sendMessage(ChatService.Success + "Plot renamed to " + currentPlot.getName());
					return true;
				}
			} // Set plot's order number
			else if (args[0].equals("order") && args.length == 2) {
				if (Privilege.canModifyPlotData(sender, currentPlot)) {
					int orderNumber;

					// todo check for existing order #
					try {
						orderNumber = Integer.parseInt(args[1]);
					} catch (Exception e) {
						Keepcraft.log("Error while setting plot order number: " + e.getMessage());
						return false;
					}

					currentPlot.setOrderNumber(orderNumber);
					plotService.updatePlot(currentPlot);
					commandSender.sendMessage(ChatService.Success + "Plot order number set to " + currentPlot.getOrderNumber());
					return true;
				}
			} // Delete to default
			else if (args[0].equals("delete")) {
				if (Privilege.canModifyPlotData(sender, currentPlot)) {
					plotService.removePlot(currentPlot);
					commandSender.sendMessage(ChatService.Success + "Plot deleted");
					return true;
				}
			}
		}

		return false;
	}

}
