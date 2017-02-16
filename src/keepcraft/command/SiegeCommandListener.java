package keepcraft.command;

import keepcraft.Keepcraft;
import keepcraft.data.models.Plot;
import keepcraft.data.models.User;
import keepcraft.services.ChatService;
import keepcraft.services.SiegeService;
import keepcraft.services.UserService;
import keepcraft.tasks.Siege;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SiegeCommandListener extends CommandListener {

	private final UserService userService;
	private final SiegeService siegeService;
	private final ChatService chatService;

	public SiegeCommandListener(UserService userService, SiegeService siegeService, ChatService chatService) {
		this.userService = userService;
		this.siegeService = siegeService;
		this.chatService = chatService;
	}

	@Override
	protected boolean handle(String commandName, CommandSender commandSender, String[] args) {
		User sender = userService.getOnlineUser(commandSender.getName());
		Plot currentPlot = sender.getCurrentPlot();

		if ((commandName.equalsIgnoreCase("cap") || commandName.equalsIgnoreCase("capture")) && args.length == 0) {
			Player player = (Player) commandSender;
			// Attempt to capture a plot

			if (currentPlot == null) {
				chatService.sendFailureMessage(sender, "You are not in a plot");
				return true;
			}

			Siege existingSiege = currentPlot.getSiege();

			if (!currentPlot.getProtection().isCapturable() || currentPlot.getProtection().getTriggerRadius() == 0
					|| currentPlot.isAdminProtected()) {
				chatService.sendFailureMessage(sender, "This area is not capturable");
				return true;
			} else if (currentPlot.isFactionProtected(sender.getFaction()) && existingSiege == null) {
				chatService.sendFailureMessage(sender, "This area is already secured");
				return true;
			} else if (!currentPlot.isInTriggerRadius(player.getLocation())) {
				chatService.sendFailureMessage(sender, "Move closer to the area's center");
				return true;
			} else {
				// We're in a plot, it can be triggered, and we're close to center
				if (existingSiege != null) {
					if (existingSiege.getDefendingFaction() == sender.getFaction()) { // the sender is a defender
						existingSiege.cancel(sender);
						return true;
					} else if (existingSiege.getAttackingFaction() == sender.getFaction()) {
						chatService.sendFailureMessage(sender, "This area is already being captured");
						return true;
					} else { // A third faction is attacking
						existingSiege.cancel();
						chatService.sendSuccessMessage(sender, "You begin capturing " + currentPlot.getName());
						startSiege(sender, currentPlot);
						return true;
					}
				} else {
					// begin siege?
					chatService.sendSuccessMessage(sender, "You begin capturing " + currentPlot.getName());
					startSiege(sender, currentPlot);
					return true;
				}
			}
		}

		return false;
	}

	private void startSiege(User sender, Plot plot) {
		Siege siege = siegeService.startSiege(plot, sender);

		int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Keepcraft.getPlugin(), siege, 0, 600);
		// 20 tick value = 1 second
		// 1200 tick value = 1 minute
		siege.setTaskId(taskId);
	}

}
