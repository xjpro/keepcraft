package keepcraft.listener;

import keepcraft.data.models.Armor;
import keepcraft.data.models.User;
import keepcraft.services.ChatService;
import keepcraft.services.TeamService;
import keepcraft.services.UserService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class SneakListener implements Listener {

	private final UserService userService;
	private final TeamService teamService;
	private final ChatService chatService;

	public SneakListener(UserService userService, TeamService teamService, ChatService chatService) {
		this.userService = userService;
		this.teamService = teamService;
		this.chatService = chatService;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		User user = userService.getOnlineUser(player.getName());

		if (user.isHiding() && !Armor.isWearingFullLeatherArmor(player)) {
			user.setHiding(false);
			teamService.removeStealth(user, player);
			chatService.sendFailureMessage(user, "Your name is now visible");
		}

		if (Armor.getDefensePoints(player) > Armor.FULL_IRON_ARMOR && player.isSneaking()) {
			//player.setSneaking(false);
		}
	}

	//@EventHandler(priority = EventPriority.NORMAL)
	// Some experimental shit to toggle sneak on and off
	public void onPlayerSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		User user = userService.getOnlineUser(player.getName());

		if (!user.getToggleSneak()) return;

		if (event.isSneaking()) { // requesting sneak
			if (!user.getPersistentSneak()) {
				user.setPersistentSneak(true);
				player.setWalkSpeed(0.1f);
			} else {
				user.setPersistentSneak(false);
				player.setWalkSpeed(0.2f);
			}
		} else if (user.getPersistentSneak()) {
			event.setCancelled(true);
		}
	}
}
