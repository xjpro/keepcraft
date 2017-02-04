package keepcraft.listener;

import keepcraft.Keepcraft;
import keepcraft.data.models.Plot;
import keepcraft.data.models.PlotProtection;
import keepcraft.services.ChatService;
import keepcraft.services.PlotService;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.io.File;
import java.io.IOException;

public class ExplosionListener implements Listener {

	private final PlotService plotService;
	private final ChatService chatService;

	public ExplosionListener(PlotService plotService, ChatService chatService) {
		this.plotService = plotService;
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

		if (plot != null && plot.isUnderCenter(location) && plot.isFactionProtected()) {

			Keepcraft.log("Team plot core destroyed");
			chatService.sendGlobalAlertMessage(String.format("%s has been destroyed!", plot.getName()));

			// Make plot public
			plot.getProtection().setType(PlotProtection.PUBLIC);
			plotService.updatePlot(plot);

			// Blow some shit up
			Location explosionLocation = plot.getLocation().clone();
			explosionLocation.setY(8);
			while(explosionLocation.getBlockY() < plot.getLocation().getBlockY() + 10) {
				location.getWorld().createExplosion(explosionLocation.add(0, 4, 0), 16f);
			}

			// Create reset flag
			File file = new File("reset-map.flag");
			try {
				// Drop a reset flag file as a signal to outside world to run the reset-map.sh script
				file.createNewFile();
			} catch (IOException e) {
				Keepcraft.error("Error creating reset flag file");
				e.printStackTrace();
			}
		}
	}
}
