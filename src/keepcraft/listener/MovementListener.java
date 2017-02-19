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
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftBoat;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftMinecart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

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
		User user = userService.getOnlineUser(event.getPlayer().getName());
		// User can be null if the player logs off while in a boat, no idea why
		if (user != null) {
			handleMovement(user, event.getTo(), event.getFrom());
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerLeaveVehicle(VehicleExitEvent event) {
		if (event.getExited() instanceof Player && event.getVehicle().getType() == EntityType.MINECART) {
			Location exitLocation = event.getExited().getLocation().clone();
			exitLocation.setX(Math.floor(exitLocation.getX()) + 0.5);
			exitLocation.setX(Math.floor(exitLocation.getY()));
			exitLocation.setZ(Math.floor(exitLocation.getZ()) + 0.5);
			event.getExited().teleport(exitLocation);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerEnteringVehicle(PlayerInteractEntityEvent event) {
		Entity entity = event.getRightClicked();
		if (entity instanceof CraftBoat || entity instanceof CraftMinecart) {
			if (event.getPlayer().getLocation().distance(entity.getLocation()) > 1.3f) {
				// Impose a lower than normal maximum distance on entering a vehicle
				// This prevents players from using boats to get over short walls
				event.setCancelled(true);
			}
		}
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
