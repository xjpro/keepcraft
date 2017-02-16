package keepcraft.services;

import keepcraft.Keepcraft;
import keepcraft.data.models.Plot;
import keepcraft.data.models.User;
import keepcraft.tasks.Siege;
import org.bukkit.Bukkit;

public class SiegeService {

	private final UserService userService;
	private final PlotService plotService;
	private final ChatService chatService;

	public SiegeService(UserService userService, PlotService plotService, ChatService chatService) {
		this.userService = userService;
		this.plotService = plotService;
		this.chatService = chatService;
	}

	public Siege startSiege(Plot plot, User initiatingUser) {
		Siege siege = new Siege(userService, plotService, chatService, plot, initiatingUser);
		// Run every 30 seconds
		int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Keepcraft.getPlugin(), siege, 0, 600);
		siege.setTaskId(taskId);
		return siege;
	}
}
