package keepcraft.services;

import keepcraft.Keepcraft;
import keepcraft.data.models.Plot;
import keepcraft.data.models.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RallyService {

	public static int RallyTimeoutSeconds = 5;

	private final ChatService chatService;

	public RallyService(ChatService chatService) {
		this.chatService = chatService;
	}

	public void rallyToPlot(User user, Player player, Plot rallyingTo) {
		if (user.getRallyingTo() != null) {
			chatService.sendFailureMessage(user, String.format("You are already rallying to %s", user.getRallyingTo().getLocation()));
			return;
		}

		String startRallyErrorMessage = canRally(user, player, rallyingTo);
		if (startRallyErrorMessage != null) {
			chatService.sendFailureMessage(user, startRallyErrorMessage);
			return;
		}

		// Rally start successful
		user.setRallyingTo(rallyingTo);
		chatService.sendSuccessMessage(user, String.format("Rallying to %s in %s seconds", rallyingTo.getName(), RallyTimeoutSeconds));

		Bukkit.getScheduler().scheduleSyncDelayedTask(Keepcraft.getPlugin(), () -> {
			String finishRallyErrorMessage = canRally(user, player, rallyingTo);
			if (finishRallyErrorMessage != null) {
				chatService.sendFailureMessage(user, finishRallyErrorMessage);
			} else {
				player.teleport(rallyingTo.getLocation());
			}

			user.setRallyingTo(null); // either way, clear out rallying to point
		}, 20 * RallyTimeoutSeconds);
	}

	private String canRally(User user, Player player, Plot rallyingTo) {
		if (user.isInCombat()) {
			return "You cannot rally while in combat";
		}

		Plot rallyingFrom = user.getCurrentPlot();
		if (rallyingFrom == null || !rallyingFrom.canBeRalliedTo(user)) {
			return "You can only rally from a secured rally point";
		} else if (!rallyingTo.canBeRalliedTo(user)) {
			return "That rally point has not been secured";
		} else if (rallyingFrom == rallyingTo) {
			return "You are already at that rally point";
		} else if (!rallyingFrom.isInTriggerRadius(player.getLocation())) {
			return "Move closer to center of point to rally";
		}

		return null;
	}
}
