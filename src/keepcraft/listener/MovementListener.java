package keepcraft.listener;

import keepcraft.Keepcraft;
import keepcraft.data.models.Plot;
import keepcraft.data.models.User;
import keepcraft.services.ChatService;
import keepcraft.services.PlotService;
import keepcraft.services.UserService;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class MovementListener implements Listener {

	private final UserService userService;
	private final PlotService plotService;
	private final ChatService chatService;

	public MovementListener(UserService userService, PlotService plotService, ChatService chatService) {
		this.userService = userService;
		this.plotService = plotService;
		this.chatService = chatService;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		User user = userService.getOnlineUser(player.getName());
		handleMovement(user, event.getTo(), event.getFrom());

		Plot currentPlot = user.getCurrentPlot();
		if (currentPlot != null && !currentPlot.isFactionProtected(user.getFaction()) && player.getGameMode() != GameMode.CREATIVE) {
			// Begin invalid teleportation and float prevention
			Location from = event.getFrom();
			if ((from.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
					&& (player.getFallDistance() == 0.0F && player.getVelocity().getY() <= -0.6D)
					&& (player.getLocation().getY() > 0.0D)) {
				Keepcraft.log(String.format("A float by %s was prevented", user.getName()));

				// Find the ground
				Location groundLocation = from.clone();
				while (groundLocation.getBlock().getType() == Material.AIR && groundLocation.getY() > 0) {
					groundLocation.add(0, -1, 0);
				}
				groundLocation.add(0, 1, 0); // Ground located
				player.teleport(groundLocation);
			}
			// End invalid teleportation and float prevention
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		handleMovement(userService.getOnlineUser(event.getPlayer().getName()), event.getTo(), event.getFrom());
	}

	private void handleMovement(User user, Location to, Location from) {
		// Code for updating player's current plot
		Plot currentPlot = user.getCurrentPlot();
		Plot candidatePlot = plotService.getIntersectedPlot(to);

		if (currentPlot != candidatePlot) {
			if (currentPlot != null && candidatePlot == null) {
				chatService.sendAlertMessage(user, "Leaving " + currentPlot.getColoredName());
				user.setCurrentPlot(null);
			}

			if (candidatePlot != null) {
				if (currentPlot == null) {
					chatService.sendAlertMessage(user, "Entering " + candidatePlot.getColoredName());
				}
				user.setCurrentPlot(candidatePlot);
			}
		} else if (currentPlot != null && !currentPlot.isAdminProtected() && !currentPlot.isEventProtected()
				&& currentPlot.getProtection().getKeepRadius() > 0) // we are in a plot with a keep radius
		{
			// if are going to intersects protected but didn't before
			if (currentPlot.isInTeamProtectedRadius(to) && !currentPlot.isInTeamProtectedRadius(from)) {
				chatService.sendAlertMessage(user, "Entering " + candidatePlot.getColoredName() + " (Keep)");
			} // if we are not going to intersects protected but did before
			else if (!currentPlot.isInTeamProtectedRadius(to) && currentPlot.isInTeamProtectedRadius(from)) {
				chatService.sendAlertMessage(user, "Leaving " + candidatePlot.getColoredName() + " (Keep)");
			}
		}
		// End plot update code
	}

}
