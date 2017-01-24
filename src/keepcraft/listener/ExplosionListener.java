package keepcraft.listener;

import java.util.Collection;

import keepcraft.services.ChatService;
import keepcraft.services.PlotService;
import keepcraft.services.UserService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import keepcraft.data.models.Direction;
import keepcraft.data.models.Plot;
import keepcraft.data.models.User;


public class ExplosionListener implements Listener {

    private final UserService userService;
    private final PlotService plotService;
    private final ChatService chatService;

    public ExplosionListener(UserService userService, PlotService plotService, ChatService chatService) {
        this.userService = userService;
        this.plotService = plotService;
        this.chatService = chatService;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityExplode(EntityExplodeEvent event) {
        Location loc = event.getLocation();
        Plot plot = plotService.getIntersectedPlot(loc);

        // But wait! no block damage for admin plots
        if (plot != null && (plot.isAdminProtected() || plot.isSpawnProtected() || plot.intersectsAdminRadius(loc))) {
            event.setCancelled(true);
        }

        // Begin notification code
        if (plot != null) {
            Server server = Bukkit.getServer();
            Collection<User> allUsers = userService.getOnlineUsers();

            for (User user : allUsers) {
                if (plot == user.getCurrentPlot() && plot.isFactionProtected(user.getFaction())) {
                    // They need to be notified
                    Player p = server.getPlayer(user.getName());
                    Location locationTo = Direction.lookAt(p.getLocation(), loc);
                    String direction = Direction.getCardinalDirection(locationTo);

                    double distance = p.getLocation().distance(loc);

                    if (distance > 30) {
                        chatService.sendAlertMessage(user, "The rumble of an explosion echoes from the " + direction);
                    } else if (distance > 12) {
                        chatService.sendAlertMessage(user, "The roar of an explosion thunders from the " + direction);
                    }
                }
            }
        }
        // End notification code
    }
}
