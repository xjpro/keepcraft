package keepcraft.listener;

import keepcraft.data.models.Plot;
import keepcraft.data.models.User;
import keepcraft.data.models.UserPrivilege;
import keepcraft.services.ChatService;
import keepcraft.services.PlotService;
import keepcraft.services.UserService;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlotProtectionListener implements Listener {

	private final UserService userService;
	private final PlotService plotService;
	private final ChatService chatService;

	public PlotProtectionListener(UserService userService, PlotService plotService, ChatService chatService) {
		this.userService = userService;
		this.plotService = plotService;
		this.chatService = chatService;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) return;

		Player player = event.getPlayer();
		User user = userService.getOnlineUser(player.getName());

		if (user.isAdmin()) {
			return;
		}

		Block block = event.getBlock();

		// Blocks not allowed except by admin placement
		if (block.getType() == Material.ENCHANTING_TABLE) {
			handleDisallowedBlockPlacement(event);
			return;
		}

		Plot plot = plotService.getIntersectedPlot(block.getLocation());

		// Allow pistons only in friendly territory
		if (block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON || block.getType() == Material.MOVING_PISTON) {
			handlePistonPlacement(event, plot, user);
			return;
		}

		if (plot == null || plot.getProtection() == null) {
			return; // Nothing to protect
		}

		Material blockType = event.getBlock().getType();

		// Allow Fire on TNT
		if (blockType == Material.FIRE && event.getBlockAgainst().getType() == Material.TNT) {
			return;
		}
		// Allow TNT & Magma (attack blocks) under certain conditions
		if (blockType == Material.TNT || blockType == Material.LAVA) {
			handleAttackBlockPlacement(event, plot, user);
			return;
		}
		// Finally, handle all other block types by checking if user can modify this area
		if (!plot.canModify(user, block.getLocation())) {
			event.setCancelled(true);
			event.setBuild(false);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) return;

		Player player = event.getPlayer();

		Block block = event.getBlock();
		Material blockType = block.getType();
//		if ((blockType == Material.LEGACY_ENDER_STONE || blockType == Material.LEGACY_END_BRICKS) && !userService.getOnlineUser(player.getName()).isAdmin()) {
//			// Only admin may remove ender blocks
//			event.setCancelled(true);
//			return;
//		}

		switch (blockType) {
			// Put materials here that can be broken no matter what
//			case LEGACY_CROPS:
//			case LEGACY_MELON_BLOCK:
			case PUMPKIN:
//			case LEGACY_SUGAR_CANE_BLOCK:
			case TNT:
			case MELON_STEM:
			case RED_MUSHROOM:
			case BROWN_MUSHROOM:
			case VINE:
			case POTATO:
				return;
		}

		Plot plot = plotService.getIntersectedPlot(block.getLocation());
		if (plot == null || plot.getProtection() == null) {
			return;
		}
		if (!plot.canModify(userService.getOnlineUser(player.getName()), block.getLocation())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPistonExtend(BlockPistonExtendEvent event) {
		Plot plot = plotService.getIntersectedPlot(event.getBlock().getLocation());
		// Piston itself would need to be near center to affect center so we're fine checking the piston block
		if (plot == null || !plot.isTeamProtected()) return;

		event.getBlocks().forEach(block -> {
			if (plot.isUnderCenter(block.getLocation())) {
				// Don't allow piston to move any center blocks
				event.setCancelled(true);
			}
		});
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockDispense(BlockDispenseEvent event) {
		Material material = event.getItem().getType();
		if (material == Material.TNT) {
			// Do not allow TNT to dispense in protected plots
			// This could only really happen if a team member was dispensing TNT into their own plot

			Plot plot = plotService.getIntersectedPlot(event.getBlock().getLocation());
			if (plot != null && plot.isTeamProtected()) {
				event.setCancelled(true);
			}
		} else if (material == Material.LAVA_BUCKET) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onGraylistEntityDamage(EntityDamageByEntityEvent event) {
		if (event.isCancelled() || event.getEntity() instanceof Player)
			return; // cancelled or entity being hit is a player

		Player damager = null;
		if (event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK) && event.getDamager() instanceof Player) {
			damager = (Player) event.getDamager();
		} else if (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE) && event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getDamager();
			if (arrow.getShooter() instanceof Player) {
				damager = (Player) arrow.getShooter();
			}
		}

		if (damager == null) return;

		Plot plot = plotService.getIntersectedPlot(event.getEntity().getLocation());
		if (plot == null) return;
		User user = userService.getOnlineUser(damager.getName());
		if (user.getPrivilege() == UserPrivilege.MEMBER_START && plot.isTeamProtected(user.getTeam())) {
			// New members can't damage entities in friendly territory
			event.setCancelled(true);
		}
	}

	private void handleDisallowedBlockPlacement(BlockPlaceEvent event) {
		event.setCancelled(true);
		event.setBuild(false);
	}

	private void handlePistonPlacement(BlockPlaceEvent event, Plot plot, User user) {
		boolean cancelPlacement = false;
		if (plot == null || !plot.isTeamProtected(user.getTeam())) {
			// Not in a plot or plot is not a friendly plot
			chatService.sendFailureMessage(user, "Pistons can only be placed in your team protected area");
			cancelPlacement = true;
		} else if (!plot.canModify(user, event.getBlock().getLocation())) {
			// In friendly plot but cannot interact due to typical rules (admin radius probably)
			cancelPlacement = true;
		}

		if (cancelPlacement) {
			event.setCancelled(true);
			event.setBuild(false);
		}
	}

	private void handleAttackBlockPlacement(BlockPlaceEvent event, Plot plot, User user) {
		//Block block = event.getBlock();
		boolean cancelPlacement = false;

		// Placement in own plot...
		if (plot.isTeamProtected(user.getTeam())) {
			chatService.sendFailureMessage(user, "Cannot place attack blocks in your own base");
			cancelPlacement = true;
		}
		// Admin protected
		else if (plot.isAdminProtected()/* || plot.isInAdminProtectedRadius(block.getLocation()) || plot.isEventProtected()*/) {
			chatService.sendFailureMessage(user, "This area is protected");
			cancelPlacement = true;
		}
		// Plot is immune to attack
		else if (plot.isImmuneToAttack()) {
			chatService.sendFailureMessage(user, "Area can only be attacked from 8pm to 11pm CST");
			cancelPlacement = true;
		}

		if (cancelPlacement) {
			event.setCancelled(true);
			event.setBuild(false);
		}
	}
}
