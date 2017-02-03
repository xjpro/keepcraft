package keepcraft.listener;

import keepcraft.Keepcraft;
import keepcraft.data.models.Plot;
import keepcraft.services.PlotService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.io.File;
import java.io.IOException;

public class ExplosionListener implements Listener {

	private final PlotService plotService;

	public ExplosionListener(PlotService plotService) {
		this.plotService = plotService;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityExplode(EntityExplodeEvent event) {
		if (event.isCancelled()) return;

		Plot plot = plotService.getIntersectedPlot(event.getLocation());

		if (plot != null) {
			if (plot.isImmuneToAttack()) {
				event.setCancelled(true);
			} else if (plot.isFactionProtected() && plot.isUnderCenter(event.getLocation())) {

				Keepcraft.log("Beacon core blown up!");

				File file = new File("/root/minecraft/reset-map.flag");
				file.getParentFile().mkdirs();
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
