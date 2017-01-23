package keepcraft.listener;

import java.util.ArrayList;

import keepcraft.services.PlotService;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import keepcraft.Privilege;
import keepcraft.data.DataCache;
import keepcraft.data.models.Plot;
import keepcraft.data.models.User;

public class BlockProtectionListener implements Listener {

	private PlotService plotService = new PlotService();

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.isCancelled()) return; // No need to process further

		Block block = event.getBlock();

		Player p = event.getPlayer();
		User user = DataCache.retrieve(User.class, p.getName());

		if (user.isAdmin()) {
			return;
		}

		// Not allowed blocks anywhere
		if (block.getType() == Material.ENCHANTMENT_TABLE) {
			event.setCancelled(true);
			event.setBuild(false);
		}

		Plot plot = ListenerHelper.getIntersectedPlot(block.getLocation(), new ArrayList<>(plotService.getPlots()));

		if (plot == null || plot.getProtection() == null) {
			return;
		}

		switch (event.getBlock().getType()) {
			case FIRE:
				if (event.getBlockAgainst().getType() == Material.TNT) {
					return; // allow fire on TNT
				}
				break;
			case TNT:
				if (!plot.isAdminProtected() && !plot.isEventProtected()) {
					return;
				}
				break;
		}

		if (!canModify(user, block)) {
			event.setCancelled(true);
			event.setBuild(false);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
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
				event.setCancelled(false);
				return;
		}

		User user = DataCache.retrieve(User.class, p.getName());
		if (!canModify(user, block)) {
			event.setCancelled(true);
		}
	}

	private boolean canModify(User user, Block targetBlock) {
		Plot plot = ListenerHelper.getIntersectedPlot(targetBlock.getLocation(), new ArrayList<>(plotService.getPlots()));
		return Privilege.canInteract(user, targetBlock.getLocation(), plot);
	}
}
