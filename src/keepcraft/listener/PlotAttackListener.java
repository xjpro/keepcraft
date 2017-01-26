package keepcraft.listener;

import keepcraft.services.ChatService;
import keepcraft.services.PlotService;
import keepcraft.services.UserService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import keepcraft.data.models.Direction;
import keepcraft.data.models.Plot;
import keepcraft.data.models.User;

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

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;

        if (event.getBlock().getType() == Material.REDSTONE_BLOCK) {
            notifyAllUsersInPlot(event.getBlock().getLocation(), (user, distance, direction) -> {
                if (distance > 10) {
                    chatService.sendAlertMessage(user, "The sizzle of magical construction hums from the " + direction);
                }
            });
        }
    }

    private void notifyAllUsersInPlot(Location eventLocation, PlotNotifier notifier) {
        Plot plot = plotService.getIntersectedPlot(eventLocation);
        if (plot != null) {
            Server server = Bukkit.getServer();
            for (User user : userService.getOnlineUsers()) {
                if (plot == user.getCurrentPlot() && plot.isFactionProtected(user.getFaction())) {

                    Player p = server.getPlayer(user.getName());
                    Location locationTo = Direction.lookAt(p.getLocation(), eventLocation);
                    String direction = Direction.getCardinalDirection(locationTo);

                    double distance = p.getLocation().distance(eventLocation);
                    notifier.notify(user, distance, direction);
                }
            }
        }
    }
}
