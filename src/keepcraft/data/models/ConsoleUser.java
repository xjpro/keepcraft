package keepcraft.data.models;

import keepcraft.services.ChatService;
import org.bukkit.ChatColor;

public class ConsoleUser implements ChatParticipant {
	@Override
	public String getName() {
		return "CONSOLE";
	}

	@Override
	public String getColoredName() {
		return ChatService.NameAdmin + getName() + ChatColor.RESET;
	}

	@Override
	public String getChatTag() {
		return ChatService.NameAdmin + "<" + getName() + ">" + ChatColor.RESET;
	}

	@Override
	public String getChatTag(UserTeam userTeam) {
		ChatColor chatColor = userTeam.getChatColor();
		return chatColor + "<" + ChatService.NameAdmin + getName() + chatColor + ">" + ChatColor.RESET;
	}

	@Override
	public boolean getReceiveGlobalMessages() {
		return true;
	}

	@Override
	public void setReceiveGlobalMessages(boolean value) {
	}

	@Override
	public boolean isAdmin() {
		return true;
	}

	@Override
	public boolean canApprove(User target) {
		return target.getPrivilege() == UserPrivilege.MEMBER_START;
	}
}
