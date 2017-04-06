package keepcraft.listener;

import keepcraft.Keepcraft;
import keepcraft.data.models.Container;
import keepcraft.data.models.Plot;
import keepcraft.data.models.PlotProtection;
import keepcraft.data.models.WorldPoint;
import keepcraft.services.ChatService;
import keepcraft.services.ContainerService;
import keepcraft.services.PlotService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.io.File;
import java.io.IOException;

public class ExplosionListener implements Listener {

	private final PlotService plotService;
	private final ContainerService containerService;
	private final ChatService chatService;

	public ExplosionListener(PlotService plotService, ContainerService containerService, ChatService chatService) {
		this.plotService = plotService;
		this.containerService = containerService;
		this.chatService = chatService;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityExplode(EntityExplodeEvent event) {
		if (event.isCancelled()) return;

		Plot plot = plotService.getIntersectedPlot(event.getLocation());

		// No block damage for admin plots and spawn plots
		if (plot != null && plot.isImmuneToAttack()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockChanged(EntityChangeBlockEvent event) {
		if (event.isCancelled()) return;

		Location location = event.getBlock().getLocation();
		Plot plot = plotService.getIntersectedPlot(location);

		if (plot != null && plot.isTeamProtected() && plot.isUnderCenter(location) && event.getBlock().getType() == Material.BEACON) {

			Location plotLocation = plot.getLocation();

			// Remove all core blocks
			for (int y = 0; y < plotLocation.getBlockY(); y++) {
				Block centerBlock = plotLocation.getWorld().getBlockAt(plotLocation.getBlockX(), y, plotLocation.getBlockZ());

				if (centerBlock.getType() == Material.CHEST) {
					centerBlock.breakNaturally();

					// Remove lootbox record, if any
					Container container = containerService.getContainer(new WorldPoint(centerBlock.getLocation()));
					if (container != null) {
						containerService.removeContainer(container);
					}
				}
//				else if (centerBlock.getType() != Material.BEDROCK) {
//					centerBlock.setType(Material.STONE);
//				}
			}

			chatService.sendGlobalAlertMessage(String.format("%s has been destroyed!", plot.getColoredName()));

			Location explosionLocation = plot.getLocation().clone();

			if (plot.isBasePlot()) {

				// Blow some shit up
				explosionLocation.add(0, -16, 0);
				while (explosionLocation.getBlockY() < plot.getLocation().getBlockY() + 16) {
					location.getWorld().createExplosion(explosionLocation.add(0, 4, 0), 8f);
				}

				//Keepcraft.log("Team base core destroyed");
				chatService.sendGlobalAlertMessage("The map will reset tomorrow at 8pm CST");

				// Make plot public
				plot.getProtection().setType(PlotProtection.ADMIN);
				plotService.updatePlot(plot);

				// Create reset flag
				File file = new File("reset-map.flag");
				try {
					// Drop a reset flag file as a signal to outside world to run the reset-map.sh script
					file.createNewFile();
				} catch (IOException e) {
					Keepcraft.error("Error creating reset flag file");
					e.printStackTrace();
				}
			} else {
				// Smaller explosion for outposts
				location.getWorld().createExplosion(explosionLocation, 4f);

				// Remove outpost plot entirely
				plotService.removePlot(plot);
			}
		}
	}
}
