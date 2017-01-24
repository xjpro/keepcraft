package keepcraft.services;

import java.util.Collection;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import keepcraft.data.models.Plot;
import keepcraft.data.models.User;
import keepcraft.data.models.UserPrivilege;

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
    public final static ChatColor RequestedInfo = ChatColor.AQUA;
    public final static ChatColor Info = ChatColor.DARK_GRAY;
    public final static ChatColor Change = ChatColor.DARK_PURPLE;

    public final static ChatColor PrivateMessage = ChatColor.LIGHT_PURPLE;
    public final static ChatColor GlobalMessage = ChatColor.GRAY;
    public final static ChatColor FactionMessage = ChatColor.GREEN;
    public final static ChatColor AdminMessage = ChatColor.YELLOW;

    public final static String ChatFormat = "%s %s(%s) %s";
    public final static String AlertFormat = "%s%s";
    public final static String PlayerDeathFormat = "%s %s%s %s";
    public final static String PlotCaptureFormat = "%s %s %s %s";
    public final static String PlotDefendFormat = "%s %s %s";

    public ChatService(UserService userService) {
        this.userService = userService;
    }

    public void sendGlobalMessage(User sender, String text) {
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

    public void sendFactionMessage(User sender, Collection<User> connectedUsers, int faction, String text) {
        if (text == null || text.length() == 0) {
            return;
        }

        String message = String.format(ChatService.ChatFormat, sender.getChatTag(faction), ChatService.FactionMessage, "Faction", text);

        for (User receiver : connectedUsers) {
            if (receiver.getPrivilege() == UserPrivilege.ADMIN || receiver.getFaction() == faction) {
                Bukkit.getPlayer(receiver.getName()).sendMessage(message);
            }
        }

        logger.info(message);
    }

    public void sendAdminMessage(User sender, Collection<User> connectedUsers, String text) {
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

    public void sendAlertMessage(User target, String text) {
        String message = String.format(ChatService.AlertFormat, ChatService.Info, text);
        Bukkit.getPlayer(target.getName()).sendMessage(message);
    }

    public void sendGlobalAlertMessage(String text) {
        String message = String.format(ChatService.AlertFormat, ChatService.Info, text);
        for (Player receiver : Bukkit.getOnlinePlayers()) {
            receiver.sendMessage(message);
        }

        logger.info(message);
    }

    public void sendPlotCaptureMessage(User capturer, String text, Plot target, String time) {
        String message = String.format(ChatService.PlotCaptureFormat, capturer.getColoredName(), ChatService.Info + text, target.getColoredName(), ChatService.Info + time);
        for (Player receiver : Bukkit.getOnlinePlayers()) {
            receiver.sendMessage(message);
        }
        logger.info(message);
    }

    public void sendPlotDefendMessage(User defender, String text, Plot target) {
        String message = String.format(ChatService.PlotDefendFormat, defender.getColoredName(), ChatService.Info + text, target.getColoredName());
        for (Player receiver : Bukkit.getOnlinePlayers()) {
            receiver.sendMessage(message);
        }
        logger.info(message);
    }

    public void sendPrivateMessage(User sender, User target, String text) {
        if (text == null || text.length() == 0) {
            return;
        }

        String feedback = String.format(ChatService.ChatFormat, ChatService.PrivateMessage + "<to " + target.getName() + ">", ChatService.PrivateMessage, "Private", text);
        String message = String.format(ChatService.ChatFormat, sender.getChatTag(), ChatService.PrivateMessage, "Private", text);

        Player sentBy = Bukkit.getPlayer(sender.getName());
        Player sentTo = Bukkit.getPlayer(target.getName());

        sentBy.sendMessage(feedback);
        sentTo.sendMessage(message);

        target.setLastPrivateMessageSender(sender.getName());

        logger.info(sentBy.getName() + " " + feedback);
    }
}
