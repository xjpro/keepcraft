package keepcraft.listener;

import keepcraft.Keepcraft;
import keepcraft.data.models.Plot;
import keepcraft.services.PlotService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ExplosionListener implements Listener {

	private final PlotService plotService;

	public ExplosionListener(PlotService plotService) {
		this.plotService = plotService;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityExplode(EntityExplodeEvent event) {
		if (event.isCancelled()) return;

		Plot plot = plotService.getIntersectedPlot(event.getLocation());

		// No block damage for admin plots and spawn plots
		if (plot != null) {
			if (plot.isImmuneToAttack()) {
				event.setCancelled(true);
			} else if (plot.isFactionProtected() && plot.isUnderCenter(event.getLocation())) {
				Keepcraft.log("Beacon core blown up!");
			}
		}
	}
}
