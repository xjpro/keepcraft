package keepcraft.listener;

import keepcraft.Keepcraft;
import keepcraft.data.models.User;
import keepcraft.services.UserService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

public class CombatLogoffListener implements Listener {

	private final UserService userService;

	public CombatLogoffListener(UserService userService) {
		this.userService = userService;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		User user = userService.getOnlineUser(player.getName());

		if (!user.isInCombat()) {
			Block skullBlock = player.getWorld().getBlockAt(player.getLocation());
			skullBlock.setType(Material.LEGACY_SKULL);
//			skullBlock.setData((byte) 0x1); // todo doesn't work anymore
			BlockState state = skullBlock.getState();

			if (state instanceof Skull) {
				Skull skull = (Skull) state;
				skull.setSkullType(SkullType.PLAYER);
				skull.setOwner(player.getName());
				skull.update();
			}

			skullBlock.setMetadata("user_logoff", new FixedMetadataValue(Keepcraft.getPlugin(), player.getName()));

			Bukkit.getScheduler().scheduleSyncDelayedTask(Keepcraft.getPlugin(), () -> {
				if (skullBlock.getType() == Material.LEGACY_SKULL) {
					skullBlock.setType(Material.AIR);
					skullBlock.removeMetadata("user_logoff", Keepcraft.getPlugin());
				}
			}, User.InCombatTimeoutSeconds * 20);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBreakPlayerHead(BlockBreakEvent event) {
		if (event.getBlock().getType() == Material.LEGACY_SKULL && event.getBlock().hasMetadata("user_logoff")) {
			event.setCancelled(true);
			event.getBlock().setType(Material.AIR);

			List<MetadataValue> user_logoff = event.getBlock().getMetadata("user_logoff");
			for (MetadataValue value : user_logoff) {
				OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(value.asString());
				offlinePlayer.getPlayer().setHealth(0);
			}
		}
	}
}
