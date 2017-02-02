package keepcraft.listener;

import keepcraft.Keepcraft;
import keepcraft.Privilege;
import keepcraft.data.models.Plot;
import keepcraft.data.models.User;
import keepcraft.services.ChatService;
import keepcraft.services.PlotService;
import keepcraft.services.UserService;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

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
		if (block.getType() == Material.ENCHANTMENT_TABLE || block.getType() == Material.BREWING_STAND) {
			handleDisallowedBlockPlacement(event);
			return;
		}

		Plot plot = plotService.getIntersectedPlot(block.getLocation());

		// Allow pistons only in friendly territory
		if (block.getType() == Material.PISTON_BASE || block.getType() == Material.PISTON_STICKY_BASE) {
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
		if (blockType == Material.TNT || blockType == Material.MAGMA) {
			handleAttackBlockPlacement(event, plot, user);
			return;
		}
		// Finally, handle all other block types by checking if user can modify this area
		if (!Privilege.canInteract(user, block.getLocation(), plot)) {
			event.setCancelled(true);
			event.setBuild(false);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) return;

		Player player = event.getPlayer();
		if (player == null) return;

		Block block = event.getBlock();
		if (block.getType() == Material.ENDER_STONE && !userService.getOnlineUser(player.getName()).isAdmin()) {
			// Only admin may remove ender blocks
			event.setCancelled(true);
			return;
		}

		switch (block.getType()) {
			// Put materials here that can be broken no matter what
			case CROPS:
			case MELON_BLOCK:
			case PUMPKIN:
			case SUGAR_CANE_BLOCK:
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
		if (!Privilege.canInteract(userService.getOnlineUser(player.getName()), block.getLocation(), plot)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if (event.isCancelled()) return;

		Block block = event.getBlock();
		// TODO block gravity turns block to air before sending us this event
		if (event.getBlock().getType() == Material.BEACON && event.getTo() == Material.AIR &&
				(event.getEntityType() == EntityType.PRIMED_TNT || event.getEntityType() == EntityType.FALLING_BLOCK)) {
			Plot plot = plotService.getIntersectedPlot(block.getLocation());
			if (plot.isFactionProtected() && plot.isInAdminProtectedRadius(block.getLocation())) {
				// A beacon block was blown up by TNT in the admin protected radius of a team plot
				Keepcraft.log("Beacon blown up!");
			}
		}
	}

	private void handleDisallowedBlockPlacement(BlockPlaceEvent event) {
		event.setCancelled(true);
		event.setBuild(false);
	}

	private void handlePistonPlacement(BlockPlaceEvent event, Plot plot, User user) {
		boolean cancelPlacement = false;
		if (plot == null || !plot.isFactionProtected(user.getFaction())) {
			// Not in a plot or plot is not a friendly plot
			chatService.sendFailureMessage(user, "Pistons can only be placed in your team protected area");
			cancelPlacement = true;
		} else if (!Privilege.canInteract(user, event.getBlock().getLocation(), plot)) {
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
		if (plot.isFactionProtected(user.getFaction())) {
			chatService.sendFailureMessage(user, "Cannot place attack blocks in your own base");
			cancelPlacement = true;
		}
		// Plot is immune to attack
		else if (plot.isImmuneToAttack()) {
			chatService.sendFailureMessage(user, "Area can only be attacked from 8pm to 11pm CST");
			cancelPlacement = true;
		}
		// Admin protected
		else if (plot.isAdminProtected()/* || plot.isInAdminProtectedRadius(block.getLocation()) || plot.isEventProtected()*/) {
			chatService.sendFailureMessage(user, "This area is protected");
			cancelPlacement = true;
		}

		if (cancelPlacement) {
			event.setCancelled(true);
			event.setBuild(false);
		}
	}
}
