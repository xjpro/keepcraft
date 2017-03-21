package keepcraft.listener;

import keepcraft.data.models.Armor;
import keepcraft.data.models.User;
import keepcraft.services.UserService;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class SneakListener implements Listener {

	private final UserService userService;

	public SneakListener(UserService userService) {
		this.userService = userService;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		User user = userService.getOnlineUser(player.getName());

		if (Armor.isWearingFullLeatherArmor(player)) {
			user.setStealth(true);
			player.setSneaking(true);
			player.setWalkSpeed(0.2f);
		} else if (user.hasStealth()) {
			user.setStealth(false);
			player.setSneaking(false);
			player.setWalkSpeed(0.2f);
		}

		if (Armor.getDefensePoints(player) > Armor.FULL_IRON_ARMOR && player.isSneaking()) {
			//player.setSneaking(false);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerSneakInteract(PlayerInteractEvent event) {
		if (event.isCancelled() || !event.getPlayer().isSneaking()) return;

		Block block = event.getClickedBlock();
		if (block != null) {
			event.getPlayer().setSneaking(false);
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
