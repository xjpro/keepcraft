package keepcraft.command;

import keepcraft.data.models.User;
import keepcraft.data.models.UserTeam;
import keepcraft.data.models.UserPrivilege;
import keepcraft.services.ChatService;
import keepcraft.services.UserService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InitCommandListener extends CommandListener {

	private final UserService userService;
	private final ChatService chatService;

	public InitCommandListener(UserService userService, ChatService chatService) {
		this.userService = userService;
		this.chatService = chatService;
	}

	@Override
	protected boolean handle(String commandName, CommandSender commandSender, String[] args) {
		User sender = userService.getOnlineUser(commandSender.getName());
		if (sender.getPrivilege() != UserPrivilege.INIT) {
			chatService.sendFailureMessage(sender, "You have already joined a team");
			return true;
		}

		if (commandName.equals("join")) {
			String targetUserName = args[0].trim();
			User targetUser = userService.getUser(targetUserName);
			if (targetUser == null) {
				chatService.sendFailureMessage(sender, String.format("User '%s' not found", targetUserName));
				return true;
			}
			if (targetUser.getPrivilege() == UserPrivilege.INIT || targetUser.getPrivilege() == UserPrivilege.ADMIN ||
					(targetUser.getTeam() != UserTeam.RED && targetUser.getTeam() != UserTeam.BLUE && targetUser.getTeam() != UserTeam.GREEN)) {
				chatService.sendFailureMessage(sender, String.format("%s is not on a joinable team", targetUserName));
				return true;
			}

			// Set team
			//todo sender.setTeam(targetUser.getTeam());
			userService.updateUser(sender);
			chatService.sendAlertMessage(sender, String.format("You have joined %s", sender.getTeam().getChatColoredNamed()));

			// Kill player to force respawn
			Player player = (Player) commandSender;
			//todo player.setHealth(0);

			return true;
		}
		return false;
	}
}
