package keepcraft.command;

import java.util.Collection;

import keepcraft.services.ChatService;
import keepcraft.services.UserService;
import org.bukkit.command.CommandSender;
import keepcraft.data.models.User;
import keepcraft.data.models.UserFaction;

public class FactionCommandListener extends CommandListener {

    private final UserService userService;
    private final ChatService chatService;

    public FactionCommandListener(UserService userService, ChatService chatService) {
        this.userService = userService;
        this.chatService = chatService;
    }

    @Override
    protected boolean handle(String commandName, CommandSender commandSender, String[] args) {
        User sender = userService.getOnlineUser(commandSender.getName());

        if (!sender.isAdmin()) {
            return true;
        }

        if (commandName.equals("faction")) {
            if (args.length >= 1) {
                int faction;
                try {
                    faction = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    // invalid input
                    commandSender.sendMessage(ChatService.Failure + "Faction number must be an integer");
                    return false;
                }

                if (args.length == 1) {
                    // stats
                }
            }
        } // Red team chat
        else if (commandName.equals("1") && sender.isAdmin()) {
            Collection<User> connectedUsers = userService.getOnlineUsers();
            String message = "";
            for (int i = 0; i < args.length; i++) {
                message += args[i] + " ";
            }
            chatService.sendFactionMessage(sender, connectedUsers, UserFaction.FactionRed, message);
            return true;
        } // Blue team chat
        else if (commandName.equals("2") && sender.isAdmin()) {
            Collection<User> connectedUsers = userService.getOnlineUsers();
            String message = "";
            for (int i = 0; i < args.length; i++) {
                message += args[i] + " ";
            }
            chatService.sendFactionMessage(sender, connectedUsers, UserFaction.FactionBlue, message);
            return true;
        } // Green team chat
        else if (commandName.equals("3") && sender.isAdmin()) {
            Collection<User> connectedUsers = userService.getOnlineUsers();
            String message = "";
            for (int i = 0; i < args.length; i++) {
                message += args[i] + " ";
            }
            chatService.sendFactionMessage(sender, connectedUsers, UserFaction.FactionGreen, message);
            return true;
        }

        return false;
    }
}
