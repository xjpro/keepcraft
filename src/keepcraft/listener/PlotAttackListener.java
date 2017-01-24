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
import org.bukkit.event.block.BlockFormEvent;
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) return;
        Location location = event.getLocation();
        Plot plot = plotService.getIntersectedPlot(location);
        if (plot != null) {
            notifyAllUsersInPlot(location, plot, (user, distance, direction) -> {
                if (distance > 30) {
                    chatService.sendAlertMessage(user, "The rumble of an explosion echoes from the " + direction);
                } else if (distance > 12) {
                    chatService.sendAlertMessage(user, "The roar of an explosion thunders from the " + direction);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockForm(BlockFormEvent event) {
        if (event.isCancelled()) return;
        if (event.getBlock().getType() == Material.MOSSY_COBBLESTONE) {
            Location location = event.getBlock().getLocation();
            Plot plot = plotService.getIntersectedPlot(location);
            if (plot != null) {
                notifyAllUsersInPlot(location, plot, (user, distance, direction) -> {
                    if (distance > 30) {
                        chatService.sendAlertMessage(user, "The sizzle of magical construction echoes from the " + direction);
                    } else if (distance > 10) {
                        chatService.sendAlertMessage(user, "The roar of magical construction thunders from the " + direction);
                    }
                });
            }
        }
    }

    private void notifyAllUsersInPlot(Location eventLocation, Plot plot, PlotNotifier notifier) {
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
