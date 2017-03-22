package keepcraft.listener;

import keepcraft.data.models.Plot;
import keepcraft.data.models.User;
import keepcraft.services.PlotService;
import keepcraft.services.UserService;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class StatsListener implements Listener {

	private final UserService userService;
	private final PlotService plotService;

	public StatsListener(UserService userService, PlotService plotService) {
		this.userService = userService;
		this.plotService = plotService;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		User user = userService.getOnlineUser(event.getPlayer().getName());
		user.getUserStats().playSeconds = user.getPlayedSeconds();

		if (event.getPlayer().isOp()) {
			// Clear tracked playtime for admins
			user.getUserStats().playSeconds = 0;
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) return;
		User user = userService.getOnlineUser(event.getPlayer().getName());
		user.getUserStats().blocksPlaced++;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockRemoved(BlockBreakEvent event) {
		if (event.isCancelled()) return;
		User user = userService.getOnlineUser(event.getPlayer().getName());
		user.getUserStats().blocksRemoved++;
	}

	//@EventHandler(priority = EventPriority.HIGHEST)
//	public void onInventoriedChests(InventoryClickEvent event) {
//		if (event.isCancelled()) return;
//		if (!(event.getWhoClicked() instanceof Player)) return; // Not a player moving things
//
//		Player player = (Player) event.getWhoClicked();
//		User user = userService.getOnlineUser(player.getName());
//
//		//System.out.println(event.getClickedInventory());
//		//System.out.println(event.getSource().getType());
//
//		if (event.getClickedInventory().getType() == InventoryType.CHEST) {
//			if(event.getCurrentItem() != null) {
//				// Moving from player inventory to a chest
//				user.getUserStats().blocksAddedToChests += event.getCurrentItem().getAmount();
//
//				Plot plot = plotService.getIntersectedPlot(event.getAction() == InvegetDestination().getLocation());
//				if (plot != null && plot.isTeamProtected(user.getTeam())) {
//					user.getUserStats().blocksAddedToTeamChests += event.getItem().getAmount();
//				}
//			}
//			else {
//				// Taking from a chest and putting into player inventory
//				user.getUserStats().blocksRemovedFromChests += event.getItem().getAmount();
//
//				Plot plot = plotService.getIntersectedPlot(event.getDestination().getLocation());
//				if (plot != null && plot.isTeamProtected(user.getTeam())) {
//					user.getUserStats().blocksRemovedFromTeamChests += event.getItem().getAmount();
//				}
//			}
//		}
//	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player died = event.getEntity();
		Player killer = died.getKiller();
		if (killer == null) return; // Died by entity or environment, we don't record this

		User diedUser = userService.getOnlineUser(died.getName());
		User killerUser = userService.getOnlineUser(killer.getName());
		Plot plot = plotService.getIntersectedPlot(died.getLocation());

		diedUser.getUserStats().playerDeaths++;
		killerUser.getUserStats().playerKills++;

		if (plot != null) {
			if (plot.isTeamProtected(diedUser.getTeam())) {
				// Died user died in their own territory
				diedUser.getUserStats().defendingDeaths++;
				killerUser.getUserStats().attackingKills++;
			} else if (plot.isTeamProtected(killerUser.getTeam())) {
				// Died user died in killer's territory
				diedUser.getUserStats().attackingDeaths++;
				killerUser.getUserStats().defendingKills++;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerShootArrow(EntityShootBowEvent event) {
		if (event.isCancelled() || !(event.getEntity() instanceof Player)) return;

		Player player = (Player) event.getEntity();
		User user = userService.getOnlineUser(player.getName());
		user.getUserStats().arrowShots++;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDamagePlayer(EntityDamageByEntityEvent event) {
		if (event.isCancelled()) return;
		if (!(event.getEntity() instanceof Player)) return; // Hitting a non-player, don't care

		Material hitWith = null;
		Player player = null;

		if (event.getDamager() instanceof Arrow) {
			// Archery
			Arrow arrow = (Arrow) event.getDamager();
			if (!(arrow.getShooter() instanceof Player)) return; // Shot by a non-player, don't care

			player = (Player) arrow.getShooter();
			hitWith = Material.ARROW;
		} else if (event.getDamager() instanceof Player) {
			player = (Player) event.getDamager();
			ItemStack weapon = player.getEquipment().getItemInMainHand();
			hitWith = weapon.getType();
		}

		if (hitWith == null || player == null) return;

		User damagerUser = userService.getOnlineUser(player.getName());

		if (hitWith == Material.ARROW) {
			damagerUser.getUserStats().arrowHits++;
		} else if (hitWith == Material.WOOD_SWORD || hitWith == Material.STONE_SWORD || hitWith == Material.IRON_SWORD || hitWith == Material.GOLD_SWORD || hitWith == Material.DIAMOND_SWORD) {
			damagerUser.getUserStats().swordHits++;
		} else if (hitWith == Material.WOOD_AXE || hitWith == Material.STONE_AXE || hitWith == Material.IRON_AXE || hitWith == Material.GOLD_AXE || hitWith == Material.DIAMOND_AXE) {
			damagerUser.getUserStats().axeHits++;
		} else {
			damagerUser.getUserStats().otherHits++;
		}
	}

}
