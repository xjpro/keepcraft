package keepcraft.listener;

import java.util.Collection;

import keepcraft.services.ServiceCache;
import keepcraft.services.UserService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import keepcraft.Chat;
import keepcraft.data.models.User;

public class ChatListener implements Listener {

    private UserService userService = ServiceCache.getUserService();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(PlayerChatEvent event) {
        event.setCancelled(true); // Chats only go to faction, use /g for global chat

        Player p = event.getPlayer();
        String message = event.getMessage();
        User sender = userService.getOnlineUser(p.getName());

        Collection<User> connectedUsers = userService.getOnlineUsers();

        if (sender.isAdmin()) {
            Chat.sendAdminMessage(sender, connectedUsers, message);
        } else {
            Chat.sendFactionMessage(sender, connectedUsers, sender.getFaction(), message);
        }
    }

}
