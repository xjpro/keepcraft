package keepcraft.listener;

import keepcraft.data.models.Plot;
import keepcraft.data.models.User;
import keepcraft.data.models.WorldPoint;
import keepcraft.services.ChatService;
import keepcraft.services.PlotService;
import keepcraft.services.UserService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class OutpostListener implements Listener {

	private final UserService userService;
	private final PlotService plotService;
	private final ChatService chatService;

	public OutpostListener(UserService userService, PlotService plotService, ChatService chatService) {
		this.userService = userService;
		this.plotService = plotService;
		this.chatService = chatService;
	}

	@EventHandler
	public void onOutpostPlace(BlockPlaceEvent event) {
		if (event.isCancelled() || event.getBlock().getType() != Material.PURPUR_BLOCK) return;

		User user = userService.getOnlineUser(event.getPlayer().getName());
		Location location = event.getBlock().getLocation();

		// Check for nearby plots
		for (Plot plot : plotService.getPlots()) {
			if (plot.getLocation().distance(location) < plot.getRadius() + Plot.DEFAULT_OUTPOST_RADIUS) {
				chatService.sendFailureMessage(user, "Outpost territory would overlap with a protected territory");
				event.setCancelled(true);
				return;
			}
		}

		// Success! Make the new outpost plot
		Plot outpostPlot = plotService.createOutpostPlot(new WorldPoint(location), user);
		chatService.sendGlobalAlertMessage(String.format("%s created %s", user.getColoredName(), outpostPlot.getColoredName()));

		// Build a really basic platform
		World world = location.getWorld();
		for (int x = location.getBlockX() - 3; x <= location.getBlockX() + 3; x++) {
			for (int z = location.getBlockZ() - 3; z <= location.getBlockZ() + 3; z++) {
				Block block = world.getBlockAt(x, location.getBlockY(), z);
				if (block.getType() != Material.PURPUR_BLOCK) {
					block.setType(Material.END_BRICKS);
				}
			}
		}
	}

	@EventHandler
	public void onOutpostBreakBlock(BlockBreakEvent event) {
		if (event.isCancelled() || event.getBlock().getType() != Material.PURPUR_BLOCK) return;
		User user = userService.getOnlineUser(event.getPlayer().getName());
		if (!user.isAdmin()) {
			event.setCancelled(true);
		}
	}
}
