package keepcraft.listener;

import keepcraft.data.models.Plot;
import keepcraft.data.models.PlotProtection;
import keepcraft.data.models.User;
import keepcraft.data.models.UserPrivilege;
import keepcraft.services.ChatService;
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

public class ActionListener implements Listener {

	private final UserService userService;
	private final PlotService plotService;

	public ActionListener(UserService userService, PlotService plotService) {
		this.userService = userService;
		this.plotService = plotService;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerUseItem(PlayerInteractEvent event) {
		// This event handler only concerned with right clicking with an item in hand
		Player player = event.getPlayer();

		// Disable ender pearls
		if (event.getMaterial().equals(Material.ENDER_PEARL)) {
			player.sendMessage(ChatService.Failure + "Ender pearl teleporting disabled");
			event.setCancelled(true);
		}
		// Prevent enemy players from creating boats in protected plots
		else if (isBoat(event.getMaterial())) {
			Block clicked = event.getClickedBlock();
			Plot plot = plotService.getIntersectedPlot(clicked != null ? clicked.getLocation() : player.getLocation());
			User user = userService.getOnlineUser(player.getName());
			if (plot != null && !plot.isTeamProtected(user.getTeam())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		User user = userService.getOnlineUser(player.getName());

		if (user.isAdmin()) return;

		Block clicked = event.getClickedBlock();
		Plot plot = plotService.getIntersectedPlot(clicked != null ? clicked.getLocation() : player.getLocation());
		if (plot == null || plot.getProtection() == null) {
			return;
		}

		if (clicked == null || !event.hasBlock()) {
			return; // Air
		}

		if (!plot.canInteract(user, clicked)) {
			event.setCancelled(true);

			// However, if it's a switch near a door, destroy it so TNT can be placed, plates excepted from this
			if (user.getPrivilege() != UserPrivilege.MEMBER_START && nearDoor(clicked) && !isPlate(clicked.getType())) {
				// Get rid of it, it's blocking TNT placement near a door
				clicked.setType(Material.AIR);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		if (event.getBlockClicked().getType() == Material.LEGACY_STATIONARY_LAVA) {
			// Don't allow people to scoop lava
			event.setCancelled(true);
			return;
		}

		User user = userService.getOnlineUser(event.getPlayer().getName());
		Plot plot = plotService.getIntersectedPlot(event.getBlockClicked().getLocation());

		if (plot != null && !plot.canModify(user, event.getBlockClicked().getLocation())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		if (event.getBucket() == Material.LAVA_BUCKET) {
			event.setCancelled(true);
			return;
		}

		Player player = event.getPlayer();
		User user = userService.getOnlineUser(player.getName());
		Plot plot = plotService.getIntersectedPlot(event.getBlockClicked().getLocation());

		if (plot != null && !plot.canModify(user, event.getBlockClicked().getLocation())) {
			event.setCancelled(true);
		}
	}

	private boolean nearDoor(Block target) {
		for (BlockFace face : BlockFace.values()) {
			if (target.getRelative(face).getType().equals(Material.LEGACY_IRON_DOOR_BLOCK)) {
				return true;
			}
		}
		return false;
	}

	private boolean isBoat(Material material) {
		switch (material) {
			case LEGACY_BOAT:
			case LEGACY_BOAT_ACACIA:
			case LEGACY_BOAT_BIRCH:
			case LEGACY_BOAT_DARK_OAK:
			case LEGACY_BOAT_JUNGLE:
			case LEGACY_BOAT_SPRUCE:
				return true;
			default:
				return false;
		}
	}

	private boolean isPlate(Material material) {
		switch (material) {
			case LEGACY_WOOD_PLATE:
			case LEGACY_STONE_PLATE:
			case LEGACY_IRON_PLATE:
			case LEGACY_GOLD_PLATE:
				return true;
			default:
				return false;
		}
	}

}
