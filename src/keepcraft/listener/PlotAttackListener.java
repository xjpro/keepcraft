package keepcraft.listener;

import keepcraft.Keepcraft;
import keepcraft.data.models.Direction;
import keepcraft.data.models.Plot;
import keepcraft.data.models.User;
import keepcraft.services.ChatService;
import keepcraft.services.PlotService;
import keepcraft.services.UserService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class PlotAttackListener implements Listener {

	interface PlotNotifier {
		void notify(User user, double distance, String direction);
	}

	private final UserService userService;
	private final PlotService plotService;
	private final ChatService chatService;

	public PlotAttackListener(UserService userService, PlotService plotService, ChatService chatService) {
		this.userService = userService;
		this.plotService = plotService;
		this.chatService = chatService;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityExplode(EntityExplodeEvent event) {
		if (event.isCancelled()) return;

		notifyAllUsersInPlot(event.getLocation(), (user, distance, direction) -> {
			if (distance > 30) {
				chatService.sendAlertMessage(user, "The rumble of an explosion echoes from the " + direction);
			} else if (distance > 12) {
				chatService.sendAlertMessage(user, "The roar of an explosion thunders from the " + direction);
			}
		});
		notifyPlayersOfAttack(event.getLocation());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) return;

		if (event.getBlock().getType() == Material.MAGMA) {
			notifyAllUsersInPlot(event.getBlock().getLocation(), (user, distance, direction) -> {
				if (distance > 10) {
					chatService.sendAlertMessage(user, "The sound of enemy construction sizzles from the " + direction);
				}
			});
			notifyPlayersOfAttack(event.getBlock().getLocation());
		}
	}

	private void notifyAllUsersInPlot(Location eventLocation, PlotNotifier notifier) {
		Plot plot = plotService.getIntersectedPlot(eventLocation);
		if (plot != null) {
			Server server = Bukkit.getServer();
			for (User user : userService.getOnlineUsers()) {
				if (plot == user.getCurrentPlot() && plot.isTeamProtected(user.getTeam())) {

					Player player = server.getPlayer(user.getName());
					Location locationTo = Direction.lookAt(player.getLocation(), eventLocation);
					String direction = Direction.getCardinalDirection(locationTo);

					double distance = player.getLocation().distance(eventLocation);
					notifier.notify(user, distance, direction);
				}
			}
		}
	}

	private void notifyPlayersOfAttack(Location location) {
		Plot plot = plotService.getIntersectedPlot(location);
		if (plot != null && plot.isTeamProtected()) {
			if (!plot.isAttackInProgress()) {
				for (User user : userService.getOnlineUsers()) {
					chatService.sendAlertMessage(user, String.format("%s has come under attack", plot.getColoredName()));
				}
				//notifyDiscordOfAttack(plot);
			}
			plot.setUnderAttack();
		}
	}

	private void notifyDiscordOfAttack(Plot plot) {
		HttpURLConnection connection = null;
		try {
			URL url = new URL("https://discordapp.com/api/webhooks/295974475965530114/sBMNFVSYrDhuITaDEoiNgja2nG3qt9OnmZ5mGUw4x_r1Ryjeh_AIiApXraoK5Zc3m-0G");
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");

			String input = String.format("{ \"content\": \"@everyone %s has come under attack\" }", plot.getName());

			OutputStream os = connection.getOutputStream();
			os.write(input.getBytes());
			os.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) connection.disconnect();
		}
	}

	// Experimental!
	// Send wolves in the plot to attack players around a given location
	private void setDefendingAnimalsToCounterAttack(Location location) {
		Plot plot = plotService.getIntersectedPlot(location);
		if (plot == null || !plot.isTeamProtected()) return; // Counter attacks only occur in team protected plots

		Collection<Entity> nearbyEntities = Keepcraft.getWorld().getNearbyEntities(location, 25, 25, 25);

		List<LivingEntity> attackers = nearbyEntities
				.stream()
				.filter(entity -> entity.getType() == EntityType.PLAYER)
				.filter(playerEntity -> {
					Player player = (Player) playerEntity;
					return !plot.isTeamProtected(userService.getOnlineUser(player.getName()).getTeam());
				})
				.map(entity -> (LivingEntity) entity)
				.collect(Collectors.toList());

		Random random = new Random();

		for (Entity entity : nearbyEntities) {
			if (entity.getType() == EntityType.WOLF) {
				Wolf wolf = (Wolf) entity;
				// todo check who owns the wolf?
				wolf.setTarget(attackers.get(random.nextInt(attackers.size())));
			}
		}
	}
}
