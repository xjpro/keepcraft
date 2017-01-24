package keepcraft.listener;

import keepcraft.services.PlotService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import keepcraft.data.models.Plot;

public class ExplosionListener implements Listener {

    private final PlotService plotService;

    public ExplosionListener(PlotService plotService) {
        this.plotService = plotService;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        Plot plot = plotService.getIntersectedPlot(event.getLocation());
        // No block damage for admin plots and spawn plots
        if (plot != null && plot.isAdminProtected() || plot.isSpawnProtected() || plot.intersectsAdminRadius(event.getLocation())) {
            event.setCancelled(true);
        }
    }
}
