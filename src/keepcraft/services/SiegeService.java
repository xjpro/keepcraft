package keepcraft.services;

import keepcraft.data.models.Plot;
import keepcraft.data.models.User;
import keepcraft.tasks.Siege;

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
		return new Siege(userService, plotService, chatService, plot, initiatingUser);
	}
}
