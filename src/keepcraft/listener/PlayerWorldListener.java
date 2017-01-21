package keepcraft.listener;

import keepcraft.Keepcraft;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerWorldListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if(p.getLocation().getWorld() != Keepcraft.getWorld()) {
            p.teleport(new Location(Keepcraft.getWorld(), 0, 64, 0));
        }
    }
}
