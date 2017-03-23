package keepcraft.listener;

import keepcraft.data.models.User;
import keepcraft.services.ChatService;
import keepcraft.services.UserService;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class FishingListener implements Listener {

	private final UserService userService;
	private final ChatService chatService;

	public FishingListener(UserService userService, ChatService chatService) {
		this.userService = userService;
		this.chatService = chatService;
	}

	@EventHandler
	public void onPlayerCaughtFish(PlayerFishEvent event) {
		if (event.isCancelled()) return;

		User user = userService.getOnlineUser(event.getPlayer().getName());
		Location location = event.getHook().getLocation();

		if (user.getLastFishLocation() != null) {
			Location lastFishLocation = user.getLastFishLocation();
			if (location.distance(lastFishLocation) < 3) {
				chatService.sendAlertMessage(user, "There are no more fish at this location");
				event.setCancelled(true);
			}
		}

		user.setLastFishLocation(location);
	}

}
