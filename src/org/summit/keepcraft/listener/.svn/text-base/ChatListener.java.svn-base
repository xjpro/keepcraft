package org.summit.keepcraft.listener;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.summit.keepcraft.Chat;
import org.summit.keepcraft.data.DataCache;
import org.summit.keepcraft.data.models.User;

public class ChatListener implements Listener
{	
    @EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerChat(PlayerChatEvent event)
	{
		event.setCancelled(true); // Chats only go to faction, use /g for global chat
		
		Player p = event.getPlayer();
		String message = event.getMessage();
		User sender = DataCache.retrieve(User.class, p.getName());

		Collection<User> connectedUsers = DataCache.retrieveAll(User.class);
        
        if(sender.isAdmin())
        {
            Chat.sendAdminMessage(sender, connectedUsers, message);
        }
        else
        {
            Chat.sendFactionMessage(sender, connectedUsers, sender.getFaction(), message);
        }
	}

}
