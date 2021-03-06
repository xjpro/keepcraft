package keepcraft.services;

import keepcraft.data.models.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.logging.Logger;

public class ChatService {

	private final static Logger logger = Logger.getLogger("Minecraft");
	private final UserService userService;

	public final static ChatColor NameAdmin = ChatColor.YELLOW;
	public final static ChatColor NameRed = ChatColor.RED;
	public final static ChatColor NameBlue = ChatColor.BLUE;
	public final static ChatColor NameGreen = ChatColor.DARK_GREEN;
	public final static ChatColor NameGold = ChatColor.GOLD;
	public final static ChatColor NameOther = ChatColor.DARK_GRAY;

	public final static ChatColor Success = ChatColor.GREEN;
	public final static ChatColor Failure = ChatColor.DARK_RED;
	public final static ChatColor RequestedInfo = ChatColor.WHITE;
	public final static ChatColor Info = ChatColor.DARK_GRAY;
	public final static ChatColor Change = ChatColor.DARK_PURPLE;

	private final static ChatColor GlobalMessage = ChatColor.GRAY;
	private final static ChatColor FactionMessage = ChatColor.GREEN;
	private final static ChatColor AdminMessage = ChatColor.YELLOW;

	private final static String ChatFormat = "%s %s(%s) %s";
	private final static String AlertFormat = "%s%s";
	private final static String PlayerDeathFormat = "%s %s%s %s";
	private final static String PlotCaptureFormat = "%s %s %s %s";
	private final static String PlotDefendFormat = "%s %s %s";

	public ChatService(UserService userService) {
		this.userService = userService;
	}

	public void sendGlobalMessage(ChatParticipant sender, String text) {
		if (text == null || text.length() == 0) {
			return;
		}

		if (!sender.getReceiveGlobalMessages()) {
			Bukkit.getPlayer(sender.getName()).sendMessage(ChatService.Failure + "You have muted global chat, type '/global on' to chat again");
			return;
		}

		String message = String.format(ChatService.ChatFormat, sender.getChatTag(), ChatService.GlobalMessage, "Global", text);

		for (Player receiver : Bukkit.getOnlinePlayers()) {
			User user = userService.getOnlineUser(receiver.getName());
			if (user.getReceiveGlobalMessages()) {
				receiver.sendMessage(message);
			}
		}

		logger.info(message);
	}

	public void sendFactionMessage(ChatParticipant sender, Collection<User> connectedUsers, UserTeam userTeam, String text) {
		if (text == null || text.length() == 0) {
			return;
		}

		String message = String.format(ChatService.ChatFormat, sender.getChatTag(userTeam), ChatService.FactionMessage, "Team", text);

		for (User receiver : connectedUsers) {
			if (receiver.getPrivilege() == UserPrivilege.ADMIN || receiver.getTeam() == userTeam) {
				Bukkit.getPlayer(receiver.getName()).sendMessage(message);
			}
		}

		logger.info(message);
	}

	public void sendAdminMessage(ChatParticipant sender, Collection<User> connectedUsers, String text) {
		if (text == null || text.length() == 0) {
			return;
		}

		String message = String.format(ChatService.ChatFormat, sender.getChatTag(), ChatService.AdminMessage, "Admin", text);

		for (User receiver : connectedUsers) {
			if (receiver.getPrivilege() == UserPrivilege.ADMIN) {
				Bukkit.getPlayer(receiver.getName()).sendMessage(message);
			}
		}

		logger.info(message);
	}

	public void sendAlertMessage(ChatParticipant target, String text) {
		sendBasicColoredMessage(target, ChatService.Info, text);
	}

	public void sendInfoMessage(ChatParticipant target, String text) {
		sendBasicColoredMessage(target, ChatService.RequestedInfo, text);
	}

	public void sendChangeMessage(ChatParticipant target, String text) {
		sendBasicColoredMessage(target, ChatService.Change, text);
	}

	public void sendSuccessMessage(ChatParticipant target, String text) {
		sendBasicColoredMessage(target, ChatService.Success, text);
	}

	public void sendFailureMessage(ChatParticipant target, String text) {
		sendBasicColoredMessage(target, ChatService.Failure, text);
	}

	public void sendGlobalAlertMessage(String text) {
		String message = String.format(ChatService.AlertFormat, ChatService.Info, text);
		for (Player receiver : Bukkit.getOnlinePlayers()) {
			receiver.sendMessage(message);
		}

		logger.info(message);
	}

	public void sendPlotCaptureMessage(ChatParticipant capturer, String text, Plot target, String time) {
		String message = String.format(ChatService.PlotCaptureFormat, capturer.getColoredName(), ChatService.Info + text, target.getColoredName(), ChatService.Info + time);
		for (Player receiver : Bukkit.getOnlinePlayers()) {
			receiver.sendMessage(message);
		}
		logger.info(message);
	}

	public void sendPlotDefendMessage(ChatParticipant defender, String text, Plot target) {
		String message = String.format(ChatService.PlotDefendFormat, defender.getColoredName(), ChatService.Info + text, target.getColoredName());
		for (Player receiver : Bukkit.getOnlinePlayers()) {
			receiver.sendMessage(message);
		}
		logger.info(message);
	}

	private void sendBasicColoredMessage(ChatParticipant target, ChatColor color, String text) {
		String message = String.format(ChatService.AlertFormat, color, text);
		if (target instanceof ConsoleUser) {
			logger.info(message);
		} else {
			Player player = Bukkit.getPlayer(target.getName());
			if (player != null) {
				player.sendMessage(message);
			}
		}
	}
}
