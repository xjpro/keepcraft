package keepcraft.command;

import keepcraft.services.ChatService;
import keepcraft.services.UserService;
import org.bukkit.command.CommandSender;
import keepcraft.data.models.User;

public class ChatCommandListener extends CommandListener {

	private final UserService userService;
	private final ChatService chatService;

	public ChatCommandListener(UserService userService, ChatService chatService) {
		this.userService = userService;
		this.chatService = chatService;
	}

	@Override
	protected boolean handle(String commandName, CommandSender commandSender, String[] args) {
		User sender = userService.getOnlineUser(commandSender.getName());

		// Global
		if (commandName.equals("g") && args.length > 0) {
			String message = "";
			for (String arg : args) {
				message += arg + " ";
			}
			chatService.sendGlobalMessage(sender, message);
			return true;
		}

		return false;
	}

}
