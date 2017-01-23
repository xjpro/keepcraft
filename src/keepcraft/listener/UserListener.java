package keepcraft.listener;

import keepcraft.data.models.*;
import keepcraft.services.PlotService;
import keepcraft.services.ServiceCache;
import keepcraft.services.UserService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
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
import keepcraft.Chat;
import keepcraft.Keepcraft;
import keepcraft.data.DataCache;

public class UserListener implements Listener {

    private UserService userService = ServiceCache.getUserService();
    private PlotService plotService = ServiceCache.getPlotService();

    private static class StartingValueSetter implements Runnable {

        private final Player p;

        public StartingValueSetter(Player player) {
            this.p = player;
        }

        @Override
        public void run() {
            FileConfiguration config = Keepcraft.config();
            int startingHealth = config.getInt("player.startingHealth", 10);
            int startingFood = config.getInt("player.startingFood", 10);

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
        }
        else if(p.getLocation().getWorld() != Keepcraft.getWorld()) {
            Keepcraft.log("Player " + p.getName() + " was on the wrong world, moving to " + Keepcraft.getWorld().getName());
            teleportHome(p, user);
        }

        Plot lastPlot = plotService.getPlot(user.getLastPlotId());
        if (lastPlot != null && !lastPlot.isFactionProtected(user.getFaction())) {
            // Last plot id only stored when we logged off in an owned plot.
            // This plot is now longer secured so teleport home.
            Keepcraft.log(String.format("Player %s logged into a formerly secured area, teleporting home", p.getName()));
            teleportHome(p, user);
            p.sendMessage(Chat.Info + "The area you logged into is no longer secure, returing home");
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
        FactionSpawn spawn = DataCache.retrieve(FactionSpawn.class, user.getFaction());
        Location respawnLocation = spawn.getLocation();

        if (respawnLocation != null) {
            event.setRespawnLocation(respawnLocation);
        }
        // Otherwise they'll go to default spawn (spawn not set yet)

        if (user != null && user.isAdmin()) {
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
            p.sendMessage(Chat.Failure + "Ender pearl teleporting disabled, pending balance changes");
        } else if (inHand.getType().equals(Material.POTION)) {
            byte data = inHand.getData().getData();
            if (data == 12 || data == 5 || data == 37 || data == 44 || data == 36 || data == 33) {
                event.setCancelled(true);
                p.sendMessage(Chat.Failure + "This potion is disabled, pending balance changes");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
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
        FactionSpawn respawn = DataCache.retrieve(FactionSpawn.class, user.getFaction());
        Location respawnLocation = respawn.getLocation();

        if (respawnLocation != null) {
            p.teleport(respawnLocation);
        }
    }
}
