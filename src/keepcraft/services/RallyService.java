package keepcraft.services;

import keepcraft.Keepcraft;
import keepcraft.data.models.Plot;
import keepcraft.data.models.User;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class RallyService {

	public static int RallyTimeoutSeconds = 5;

	private final ChatService chatService;

	public RallyService(ChatService chatService) {
		this.chatService = chatService;
	}

	public void rallyToPlot(User user, Player player, Plot rallyingTo) {
		if (rallyingTo == null) {
			chatService.sendFailureMessage(user, "Use /map to find number you wish to rally to");
			return;
		}
		if (user.getRallyingTo() != null) {
			chatService.sendFailureMessage(user, String.format("You are already rallying to %s", user.getRallyingTo().getName()));
			return;
		}

		String startRallyErrorMessage = canRally(user, player, rallyingTo);
		if (startRallyErrorMessage != null) {
			chatService.sendFailureMessage(user, startRallyErrorMessage);
			return;
		}

		// Rally start successful
		user.setRallyingTo(rallyingTo);
		Plot rallyingFrom = user.getCurrentPlot();

		// Spawn some portal travelling effects
		player.getWorld().playEffect(rallyingFrom.getLocation(), Effect.PORTAL_TRAVEL, 0);
		player.getWorld().playEffect(rallyingFrom.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
		player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 0)); // Makes view all wobbly

		chatService.sendSuccessMessage(user, String.format("Rallying to %s in %s seconds, stay nearby", rallyingTo.getName(), RallyTimeoutSeconds));

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
			return "Move closer to center of area to rally";
		}

		return null;
	}
}
