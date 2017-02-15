package keepcraft.listener;

import keepcraft.data.models.*;
import keepcraft.services.ChatService;
import keepcraft.services.FactionSpawnService;
import keepcraft.services.PlotService;
import keepcraft.services.UserService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import keepcraft.Keepcraft;

public class UserListener implements Listener {

	private final UserService userService;
	private final PlotService plotService;
	private final FactionSpawnService factionSpawnService;

	public UserListener(UserService userService, PlotService plotService, FactionSpawnService factionSpawnService) {
		this.userService = userService;
		this.plotService = plotService;
		this.factionSpawnService = factionSpawnService;
	}

	private static class StartingValueSetter implements Runnable {

		private final Player p;

		public StartingValueSetter(Player player) {
			this.p = player;
		}

		@Override
		public void run() {
			p.setHealth(10);
			p.setFoodLevel(20);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.setJoinMessage(null);

		Player player = event.getPlayer();

		boolean firstTimeUser = !userService.userIsRegistered(player.getName());
		User user = userService.loadOfflineUser(player.getName());

		if (firstTimeUser || user.getPrivilege() == UserPrivilege.INIT) {
			if (player.isOp()) {
				user.setPrivilege(UserPrivilege.ADMIN);
				user.setFaction(UserFaction.FactionGold);
			} else {
				user.setPrivilege(UserPrivilege.MEMBER);
			}

			userService.updateUser(user);

			setBasicEquipment(player);
			teleportHome(player, user);
		} else if (player.getLocation().getWorld() != Keepcraft.getWorld()) {
			Keepcraft.log("Player " + player.getName() + " was on the wrong world, moving to " + Keepcraft.getWorld().getName());
			teleportHome(player, user);
		}

		if (player.isOp()) {
			player.setPlayerListName(ChatService.NameAdmin + player.getDisplayName());
		} else {
			player.setPlayerListName(UserFaction.getChatColor(user.getFaction()) + player.getDisplayName());
		}

		//player.setDisplayName(UserFaction.getChatColor(user.getFaction()) + player.getDisplayName());

		Plot loggedOffFriendlyPlot = plotService.getPlot(user.getLoggedOffFriendlyPlotId());
		if (loggedOffFriendlyPlot != null && !loggedOffFriendlyPlot.isFactionProtected(user.getFaction())) {
			// Last plot id only stored when we logged off in an owned plot.
			// This plot is now longer secured so teleport home.

			// todo check that this still works
			//Keepcraft.log(String.format("Player %s logged into a formerly secured area, teleporting home", player.getName()));
			//teleportHome(player, user);
			//player.sendMessage(ChatService.Info + "The area you logged into is no longer secure, returning home");
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event) {
		event.setQuitMessage(null);

		Player p = event.getPlayer();
		User user = userService.getOnlineUser(p.getName());

		Plot currentPlot = user.getCurrentPlot();
		if (currentPlot != null && currentPlot.isFactionProtected(user.getFaction())) {
			// User is logging off in owned territory, make a note of this so
			// we can later warp them home if the territory switches control
			user.setLoggedOffFriendlyPlotId(currentPlot.getId());
		} else {
			user.setLoggedOffFriendlyPlotId(0);
		}

		userService.saveUserAndSetOffline(user);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player p = event.getPlayer();
		User user = userService.getOnlineUser(p.getName());
		FactionSpawn spawn = safelyGetFactionSpawn(user);

		event.setRespawnLocation(spawn.getLocation());

		if (user.isAdmin()) {
			setAdminEquipment(p);
		} else {
			setBasicEquipment(p);
		}

		// Want to set the player's starting health and food values but the server will not respond to
		// those changes in this method body. So we'll set a slightly delayed task to do it.
		//Bukkit.getScheduler().scheduleSyncDelayedTask(Keepcraft.getPlugin(), new StartingValueSetter(p), 40);
		Keepcraft.log(String.format("%s respawning", p.getName()));
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.isCancelled()) return;

		ItemStack inHand = event.getItem();
		if(inHand == null) return;

		Player player = event.getPlayer();
		User user = userService.getOnlineUser(player.getName());

		if (player.isOp() || user.isAdmin()) {
			return;
		}

		if (inHand.getType().equals(Material.ENDER_PEARL)) {
			event.setCancelled(true);
			player.sendMessage(ChatService.Failure + "Ender pearl teleporting disabled, pending balance changes");
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerPortal(PlayerPortalEvent event) {
		event.setCancelled(true);
	}

	private void setBasicEquipment(Player p) {
		//PlayerInventory inventory = p.getInventory();
		//inventory.addItem(new ItemStack(Material.WOOD_SWORD, 1));
		//inventory.addItem(new ItemStack(Material.BREAD, 1));
		//inventory.setHelmet(new ItemStack(Material.LEATHER_HELMET, 1));
	}

	private void setAdminEquipment(Player p) {
		PlayerInventory inventory = p.getInventory();
		inventory.addItem(new ItemStack(Material.DIAMOND_SWORD, 1));
		inventory.addItem(new ItemStack(Material.BOW, 1));
		inventory.addItem(new ItemStack(Material.ARROW, 32));
		inventory.addItem(new ItemStack(Material.GOLDEN_APPLE, 4));
		inventory.setHelmet(new ItemStack(Material.DIAMOND_HELMET, 1));
		inventory.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE, 1));
		inventory.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS, 1));
		inventory.setBoots(new ItemStack(Material.DIAMOND_BOOTS, 1));
	}

	private void teleportHome(Player p, User user) {
		FactionSpawn respawn = safelyGetFactionSpawn(user);
		p.teleport(respawn.getLocation());
	}

	private FactionSpawn safelyGetFactionSpawn(User user) {
		FactionSpawn spawn = factionSpawnService.getFactionSpawn(user.getFaction());

		if (spawn == null) {
			// A very bad thing has happened and we apparently have no spawn data, refresh cache in an attempt to recover
			Keepcraft.error(String.format("Could not find spawn for %s of faction %s", user.getName(), user.getFaction()));
			factionSpawnService.refreshCache(); // Attempt to restore things as they should be
			spawn = factionSpawnService.getFactionSpawn(user.getFaction());

			if (spawn == null) {
				// Something has gone horribly, wrong... no data is available, we have no choice but to shutdown server
				Keepcraft.error("Spawn data could not be loaded! Shutting down server");
				Bukkit.getServer().shutdown();
			}
		}
		return spawn;
	}
}
