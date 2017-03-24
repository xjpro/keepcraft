package keepcraft.listener;

import keepcraft.data.models.Plot;
import keepcraft.data.models.User;
import keepcraft.data.models.WorldPoint;
import keepcraft.services.ChatService;
import keepcraft.services.PlotService;
import keepcraft.services.UserService;
import keepcraft.services.WorldModifierService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class OutpostListener implements Listener {

	private static Material OUTPOST_PLACEMENT_MATERIAL = Material.PURPUR_PILLAR;
	private final UserService userService;
	private final PlotService plotService;
	private final WorldModifierService worldModifierService;
	private final ChatService chatService;

	public OutpostListener(UserService userService, PlotService plotService, WorldModifierService worldModifierService, ChatService chatService) {
		this.userService = userService;
		this.plotService = plotService;
		this.worldModifierService = worldModifierService;
		this.chatService = chatService;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onOutpostPlace(BlockPlaceEvent event) {
		if (event.isCancelled() || event.getBlock().getType() != OUTPOST_PLACEMENT_MATERIAL) return;

		User user = userService.getOnlineUser(event.getPlayer().getName());
		Location location = event.getBlock().getLocation();

		// Check for nearby plots
		for (Plot plot : plotService.getPlots()) {
			if (plot.getLocation().distance(location) < plot.getRadius() + Plot.DEFAULT_OUTPOST_RADIUS) {
				chatService.sendFailureMessage(user, "Outpost territory would overlap with another territory");
				event.setCancelled(true);
				return;
			}
		}

		// Success! Make the new outpost plot
		Plot outpostPlot = plotService.createOutpostPlot(new WorldPoint(location), user);
		chatService.sendGlobalAlertMessage(String.format("%s created %s", user.getColoredName(), outpostPlot.getColoredName()));
		worldModifierService.prepareSpawnArea(location, false);
	}
}
