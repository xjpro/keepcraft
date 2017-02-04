package keepcraft.listener;

import keepcraft.Keepcraft;
import keepcraft.data.models.Plot;
import keepcraft.services.PlotService;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.io.File;
import java.io.IOException;

public class ExplosionListener implements Listener {

	private final PlotService plotService;

	public ExplosionListener(PlotService plotService) {
		this.plotService = plotService;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockChanged(EntityChangeBlockEvent event) {
		if (event.isCancelled()) return;

		Location location = event.getBlock().getLocation();
		Plot plot = plotService.getIntersectedPlot(location);

		if (plot != null) {
			if (plot.isImmuneToAttack()) {
				event.setCancelled(true);
			} else if (plot.isUnderCenter(location) && plot.isFactionProtected()) {

				Keepcraft.log("Beacon core blown up!");

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
}
