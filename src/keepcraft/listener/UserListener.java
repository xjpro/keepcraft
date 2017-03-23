package keepcraft.listener;

import keepcraft.Keepcraft;
import keepcraft.data.models.*;
import keepcraft.services.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class UserListener implements Listener {

	public static int RespawnSeconds = 15;

	private final UserService userService;
	private final PlotService plotService;
	private final FactionSpawnService factionSpawnService;
	private final ChatService chatService;
	private final TeamService teamService;

	public UserListener(UserService userService, PlotService plotService, FactionSpawnService factionSpawnService, ChatService chatService) {
		this.userService = userService;
		this.plotService = plotService;
		this.factionSpawnService = factionSpawnService;
		this.chatService = chatService;
		this.teamService = new TeamService();
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.setJoinMessage(null);

		Player player = event.getPlayer();
		//Player player = userService.loadMetadata(event.getPlayer());

		User user = userService.loadOfflineUser(player.getName(), player.getAddress().toString());

		if (user.isFirstTimeLogin()) {
			if (player.isOp()) {
				user.setPrivilege(UserPrivilege.ADMIN);
			}

			userService.setFirstLogin(user);
			userService.updateUser(user);

			teleportHome(player, user);
		} else if (player.getLocation().getWorld() != Keepcraft.getWorld()) {
			Keepcraft.log("Player " + player.getName() + " was on the wrong world, moving to " + Keepcraft.getWorld().getName());
			teleportHome(player, user);
		} else if (player.getGameMode() == GameMode.SPECTATOR) {
			// If user logs in and they are in spec mode, they must have disconnected while respawning...
			respawnAfterTimeout(player, user);
		}

		teamService.addPlayerToTeam(user.getTeam(), player);

		//player.setDisplayName(UserTeam.getChatColor(user.getTeam()) + player.getDisplayName());

		Plot loggedOffFriendlyPlot = plotService.getPlot(user.getLoggedOffFriendlyPlotId());
		if (loggedOffFriendlyPlot != null && !loggedOffFriendlyPlot.isTeamProtected(user.getTeam())) {
			// loggedOffFriendlyPlot is only stored when we logged off in an owned plot
			// This plot is now longer secured so teleport home
			Keepcraft.log(String.format("%s logged into a formerly secured area, teleporting home", player.getName()));
			teleportHome(player, user);
			player.sendMessage(ChatService.Info + "The area you logged into is no longer secure, returning home");
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event) {
		event.setQuitMessage(null);

		Player player = event.getPlayer();
		User user = userService.getOnlineUser(player.getName());

		Plot currentPlot = user.getCurrentPlot();
		if (currentPlot != null && currentPlot.isTeamProtected(user.getTeam())) {
			// User is logging off in owned territory, make a note of this so
			// we can later warp them home if the territory switches control
			user.setLoggedOffFriendlyPlotId(currentPlot.getId());
		} else {
			user.setLoggedOffFriendlyPlotId(-1);
		}

		userService.saveUserAndSetOffline(user);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		User user = userService.getOnlineUser(player.getName());
		FactionSpawn spawn = safelyGetFactionSpawn(user);
		Location respawnLocation = spawn.getWorldPoint().asLocation();

		// If user died while in combat they must wait to respawn...
		if (user.isInCombat()) {
			respawnLocation.setY(192);
			respawnAfterTimeout(player, user);
		}

		event.setRespawnLocation(respawnLocation);

		// Want to set the player's starting health and food values but the server will not respond to
		// those changes in this method body. So we'll set a slightly delayed task to do it.
		//Bukkit.getScheduler().scheduleSyncDelayedTask(Keepcraft.getPlugin(), new StartingValueSetter(p), 40);
		Keepcraft.log(String.format("%s respawning", player.getName()));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerPortal(PlayerPortalEvent event) {
		event.setCancelled(true);
	}

	private void teleportHome(Player p, User user) {
		FactionSpawn respawn = safelyGetFactionSpawn(user);
		p.teleport(respawn.getWorldPoint().asLocation());
	}

	private void respawnAfterTimeout(Player player, User user) {
		GameMode originalGameMode = player.getGameMode();

		player.setGameMode(GameMode.SPECTATOR);

		Bukkit.getScheduler().scheduleSyncDelayedTask(Keepcraft.getPlugin(), () -> {
			// Have to put this on a delayed task or it will throw a null exception when trying to find the player
			chatService.sendAlertMessage(user, String.format("Respawning in %s seconds", RespawnSeconds));
		}, 0);

		Bukkit.getScheduler().scheduleSyncDelayedTask(Keepcraft.getPlugin(), () -> {
			// Respawn player: set their game mode back and move them home
			player.setGameMode(originalGameMode == GameMode.SPECTATOR ? GameMode.SURVIVAL : originalGameMode);
			teleportHome(player, user);
		}, 20 * RespawnSeconds);
	}

	private FactionSpawn safelyGetFactionSpawn(User user) {
		FactionSpawn spawn = factionSpawnService.getFactionSpawn(user.getTeam());

		if (spawn == null) {
			if (user.getTeam() == UserTeam.GOLD) {
				return factionSpawnService.getFactionSpawn(UserTeam.RED);
			}

			// A very bad thing has happened and we apparently have no spawn data, refresh cache in an attempt to recover
			Keepcraft.error(String.format("Could not find spawn for %s of team %s", user.getName(), user.getTeam()));
			factionSpawnService.refreshCache(); // Attempt to restore things as they should be
			spawn = factionSpawnService.getFactionSpawn(user.getTeam());

			if (spawn == null) {
				// Something has gone horribly, wrong... no data is available, we have no choice but to shutdown server
				Keepcraft.error("Spawn data could not be loaded! Shutting down server");
				Bukkit.getServer().shutdown();
			}
		}
		return spawn;
	}
}
