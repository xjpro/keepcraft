package keepcraft.listener;

import keepcraft.Privilege;
import keepcraft.data.models.Plot;
import keepcraft.data.models.PlotProtection;
import keepcraft.data.models.User;
import keepcraft.services.PlotService;
import keepcraft.services.UserService;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class ActionListener implements Listener {

	private final UserService userService;
	private final PlotService plotService;

	public ActionListener(UserService userService, PlotService plotService) {
		this.userService = userService;
		this.plotService = plotService;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.hasBlock()) {
			return; // Air
		}
		Block clicked = event.getClickedBlock();

		Plot plot = plotService.getIntersectedPlot(clicked.getLocation());
		if (plot == null || plot.getProtection() == null) {
			return;
		}

		Player p = event.getPlayer();
		User user = userService.getOnlineUser(p.getName());
		Material blockType = clicked.getType();

		switch (blockType) {
			// Put the things we need to check against in here. Which are switches. Don't need
			// to check for any block types that don't have secondary functions because WorldListener
			// will check those.
			case STONE_BUTTON:
			case STONE_PLATE:
			case IRON_PLATE:
			case GOLD_PLATE:
			case LEVER:
			case TORCH:
			case REDSTONE_TORCH_ON:
			case REDSTONE_TORCH_OFF:
			case PAINTING:
			case SIGN:
				if (!Privilege.canInteract(user, clicked.getLocation(), plot)) {
					event.setCancelled(true);

					// However, if it's a switch near a door, destroy it so TNT can be placed
					if (nearDoor(clicked) && !blockType.equals(Material.STONE_PLATE)) {
						// Get rid of it, it's blocking TNT placement near a door
						clicked.setType(Material.AIR);
					}
				}
				break;
			case DISPENSER:
				// Protect dispenser in event plots
				if (plot.getProtection().getType() == PlotProtection.EVENT && !user.isAdmin()) {
					event.setCancelled(true);
				}
				break;
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		if (event.getBlockClicked().getType() == Material.STATIONARY_LAVA) {
			// Don't allow people to scoop lava
			event.setCancelled(true);
			return;
		}

		User user = userService.getOnlineUser(event.getPlayer().getName());
		Plot plot = plotService.getIntersectedPlot(event.getBlockClicked().getLocation());

		if (!Privilege.canInteract(user, event.getBlockClicked().getLocation(), plot)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		if (event.getBucket() == Material.LAVA_BUCKET) {
			event.setCancelled(true);
			return;
		}

		Player p = event.getPlayer();
		User user = userService.getOnlineUser(p.getName());
		Plot plot = plotService.getIntersectedPlot(event.getBlockClicked().getLocation());
		if (!Privilege.canInteract(user, event.getBlockClicked().getLocation(), plot)) {
			event.setCancelled(true);
		}
	}

	private boolean nearDoor(Block target) {
		for (BlockFace face : BlockFace.values()) {
			if (target.getRelative(face).getType().equals(Material.IRON_DOOR_BLOCK)) {
				return true;
			}
		}
		return false;
	}

}
