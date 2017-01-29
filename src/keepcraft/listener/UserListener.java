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

		Bukkit.getServer().getOnlinePlayers().forEach(player -> {
			onPlayerJoin(new PlayerJoinEvent(player, null));
		});
	}

	private static class StartingValueSetter implements Runnable {

		private final Player p;

		public StartingValueSetter(Player player) {
			this.p = player;
		}

		@Override
		public void run() {
			int startingHealth = 10;
			int startingFood = 100;

			p.setHealth(startingHealth);
			p.setFoodLevel(startingFood);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.setJoinMessage(null);

		Player p = event.getPlayer();

		boolean firstTimeUser = !userService.userIsRegistered(p.getName());
		User user = userService.loadOfflineUser(p.getName());

		if (firstTimeUser || user.getPrivilege() == UserPrivilege.INIT) {
			user.setPrivilege(UserPrivilege.MEMBER);
			userService.updateUser(user);

			setBasicEquipment(p);
			teleportHome(p, user);
		} else if (p.getLocation().getWorld() != Keepcraft.getWorld()) {
			Keepcraft.log("Player " + p.getName() + " was on the wrong world, moving to " + Keepcraft.getWorld().getName());
			teleportHome(p, user);
		}

		Plot lastPlot = plotService.getPlot(user.getLastPlotId());
		if (lastPlot != null && !lastPlot.isFactionProtected(user.getFaction())) {
			// Last plot id only stored when we logged off in an owned plot.
			// This plot is now longer secured so teleport home.
			Keepcraft.log(String.format("Player %s logged into a formerly secured area, teleporting home", p.getName()));
			teleportHome(p, user);
			p.sendMessage(ChatService.Info + "The area you logged into is no longer secure, returning home");
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event) {
		event.setQuitMessage(null);

		Player p = event.getPlayer();
		User user = userService.getOnlineUser(p.getName());

		Plot lastPlot = user.getCurrentPlot();
		if (lastPlot != null && lastPlot.isFactionProtected(user.getFaction())) {
			// User is logging off in owned territory, make a note of this so
			// we can later warp them home if the territory switches control
			user.setLastPlotId(lastPlot.getId());
		} else {
			user.setLastPlotId(0);
		}

		userService.setUserOffline(user);
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
		//Bukkit.getScheduler().scheduleSyncDelayedTask(Keepcraft.instance(), new StartingValueSetter(p), 40);
		Keepcraft.log(String.format("%s respawning", p.getName()));
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		User user = userService.getOnlineUser(p.getName());

		ItemStack inHand = event.getItem();

		if (user.isAdmin() || inHand == null) {
			return;
		}

		if (inHand.getType().equals(Material.ENDER_PEARL)) {
			event.setCancelled(true);
			p.sendMessage(ChatService.Failure + "Ender pearl teleporting disabled, pending balance changes");
		} else if (inHand.getType().equals(Material.POTION)) {
			byte data = inHand.getData().getData();
			if (data == 12 || data == 5 || data == 37 || data == 44 || data == 36 || data == 33) {
				event.setCancelled(true);
				p.sendMessage(ChatService.Failure + "This potion is disabled, pending balance changes");
			}
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
