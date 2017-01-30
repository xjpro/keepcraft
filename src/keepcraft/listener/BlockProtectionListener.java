package keepcraft.listener;

import keepcraft.services.ChatService;
import keepcraft.services.PlotService;
import keepcraft.services.UserService;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import keepcraft.Privilege;
import keepcraft.data.models.Plot;
import keepcraft.data.models.User;

public class BlockProtectionListener implements Listener {

	private final UserService userService;
	private final PlotService plotService;
	private final ChatService chatService;

	public BlockProtectionListener(UserService userService, PlotService plotService, ChatService chatService) {
		this.userService = userService;
		this.plotService = plotService;
		this.chatService = chatService;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) return; // No need to process further

		Block block = event.getBlock();
		Player p = event.getPlayer();
		User user = userService.getOnlineUser(p.getName());

		if (user.isAdmin()) {
			return;
		}

		// Not allowed blocks anywhere
		if (block.getType() == Material.ENCHANTMENT_TABLE || block.getType() == Material.BREWING_STAND
				|| block.getType() == Material.PISTON_BASE || block.getType() == Material.PISTON_STICKY_BASE) {
			event.setCancelled(true);
			event.setBuild(false);
			return;
		}

		Plot plot = plotService.getIntersectedPlot(block.getLocation());
		if (plot == null || plot.getProtection() == null) {
			return;
		}

		Material blockType = event.getBlock().getType();

		// Allow Fire on TNT
		if (blockType == Material.FIRE && event.getBlockAgainst().getType() == Material.TNT) {
			return;
		}

		// Allow TNT & Redstone Blocks under certain conditions...
		if (blockType == Material.TNT || blockType == Material.REDSTONE_BLOCK) {
			// Placement in own plot...
			if (blockType == Material.TNT && plot.isFactionProtected(user.getFaction())) {
				chatService.sendFailureMessage(user, "Cannot place TNT in your own team's base");
				event.setCancelled(true);
				event.setBuild(false);
			}
			// Cannot be immune to attack (send special message if this is met)
			else if (plot.isImmuneToAttack()) {
				chatService.sendFailureMessage(user, "Area can only be attacked from 8pm to midnight CST");
				event.setCancelled(true);
				event.setBuild(false);
			}
			// Cannot be admin protected
			else if (plot.isAdminProtected() || plot.isInAdminProtectedRadius(block.getLocation()) || plot.isEventProtected()) {
				chatService.sendFailureMessage(user, "This area is protected");
				event.setCancelled(true);
				event.setBuild(false);
			}
		} else if (!canModify(user, block)) {
			event.setCancelled(true);
			event.setBuild(false);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent event) {
		Player p = event.getPlayer();
		if (p == null) return;
		Block block = event.getBlock();

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

		if (!canModify(userService.getOnlineUser(p.getName()), block)) {
			event.setCancelled(true);
		}
	}

	private boolean canModify(User user, Block targetBlock) {
		Plot plot = plotService.getIntersectedPlot(targetBlock.getLocation());
		return Privilege.canInteract(user, targetBlock.getLocation(), plot);
	}
}
