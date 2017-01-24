package keepcraft.listener;

import java.util.Collection;

import keepcraft.services.UserService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import keepcraft.services.ChatService;
import keepcraft.data.models.User;

public class ChatListener implements Listener {

    private final UserService userService;
    private final ChatService chatService;

    public ChatListener(UserService userService, ChatService chatService) {
        this.userService = userService;
        this.chatService = chatService;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(PlayerChatEvent event) {
        event.setCancelled(true); // Chats only go to faction, use /g for global chat

        Player p = event.getPlayer();
        String message = event.getMessage();
        User sender = userService.getOnlineUser(p.getName());

        Collection<User> connectedUsers = userService.getOnlineUsers();

        if (sender.isAdmin()) {
            chatService.sendAdminMessage(sender, connectedUsers, message);
        } else {
            chatService.sendFactionMessage(sender, connectedUsers, sender.getFaction(), message);
        }
    }

}
