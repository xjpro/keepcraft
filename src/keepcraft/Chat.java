package keepcraft;

import java.util.Collection;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import keepcraft.data.DataCache;
import keepcraft.data.models.Plot;
import keepcraft.data.models.User;
import keepcraft.data.models.UserPrivilege;

public abstract class Chat {

    private final static Logger logger = Logger.getLogger("Minecraft");

    public final static ChatColor NameAdmin = ChatColor.YELLOW;
    public final static ChatColor NameRed = ChatColor.DARK_PURPLE;//ChatColor.RED;
    public final static ChatColor NameBlue = ChatColor.DARK_GREEN;//ChatColor.BLUE;
    public final static ChatColor NameGreen = ChatColor.BLUE;
    public final static ChatColor NameGold = ChatColor.AQUA;
    public final static ChatColor NameOther = ChatColor.DARK_GRAY;

    public final static ChatColor Success = ChatColor.GREEN;
    public final static ChatColor Failure = ChatColor.RED;
    public final static ChatColor RequestedInfo = ChatColor.GOLD;
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

    public static void sendGlobalMessage(User sender, String text) {
        if (text == null || text.length() == 0) {
            return;
        }

        if (!sender.getReceiveGlobalMessages()) {
            Bukkit.getPlayer(sender.getName()).sendMessage(Chat.Failure + "You have muted global chat, type '/global on' to chat again");
            return;
        }

        String message = String.format(Chat.ChatFormat, sender.getChatTag(), Chat.GlobalMessage, "Global", text);

        for (Player receiver : Bukkit.getOnlinePlayers()) {
            User user = DataCache.retrieve(User.class, receiver.getName());
            if (user.getReceiveGlobalMessages()) {
                receiver.sendMessage(message);
            }
        }

        logger.info(message);
    }

    public static void sendFactionMessage(User sender, Collection<User> connectedUsers, int faction, String text) {
        if (text == null || text.length() == 0) {
            return;
        }

        String message = String.format(Chat.ChatFormat, sender.getChatTag(faction), Chat.FactionMessage, "Faction", text);

        for (User receiver : connectedUsers) {
            if (receiver.getPrivilege() == UserPrivilege.ADMIN || receiver.getFaction() == faction) {
                Bukkit.getPlayer(receiver.getName()).sendMessage(message);
            }
        }

        logger.info(message);
    }

    public static void sendAdminMessage(User sender, Collection<User> connectedUsers, String text) {
        if (text == null || text.length() == 0) {
            return;
        }

        String message = String.format(Chat.ChatFormat, sender.getChatTag(), Chat.AdminMessage, "Admin", text);

        for (User receiver : connectedUsers) {
            if (receiver.getPrivilege() == UserPrivilege.ADMIN) {
                Bukkit.getPlayer(receiver.getName()).sendMessage(message);
            }
        }

        logger.info(message);
    }

    public static void sendAlertMessage(User target, String text) {
        String message = String.format(Chat.AlertFormat, Chat.Info, text);
        Bukkit.getPlayer(target.getName()).sendMessage(message);
    }

    public static void sendGlobalAlertMessage(String text) {
        String message = String.format(Chat.AlertFormat, Chat.Info, text);
        for (Player receiver : Bukkit.getOnlinePlayers()) {
            receiver.sendMessage(message);
        }

        logger.info(message);
    }

    public static void sendPlotCaptureMessage(User capturer, String text, Plot target, String time) {
        String message = String.format(Chat.PlotCaptureFormat, capturer.getColoredName(), Chat.Info + text, target.getColoredName(), Chat.Info + time);
        for (Player receiver : Bukkit.getOnlinePlayers()) {
            receiver.sendMessage(message);
        }
        logger.info(message);
    }

    public static void sendPlotDefendMessage(User defender, String text, Plot target) {
        String message = String.format(Chat.PlotDefendFormat, defender.getColoredName(), Chat.Info + text, target.getColoredName());
        for (Player receiver : Bukkit.getOnlinePlayers()) {
            receiver.sendMessage(message);
        }
        logger.info(message);
    }

    public static void sendPrivateMessage(User sender, User target, String text) {
        if (text == null || text.length() == 0) {
            return;
        }

        String feedback = String.format(Chat.ChatFormat, Chat.PrivateMessage + "<to " + target.getName() + ">", Chat.PrivateMessage, "Private", text);
        String message = String.format(Chat.ChatFormat, sender.getChatTag(), Chat.PrivateMessage, "Private", text);

        Player sentBy = Bukkit.getPlayer(sender.getName());
        Player sentTo = Bukkit.getPlayer(target.getName());

        sentBy.sendMessage(feedback);
        sentTo.sendMessage(message);

        target.setLastPrivateMessageSender(sender.getName());

        logger.info(sentBy.getName() + " " + feedback);
    }
}
