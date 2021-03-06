package keepcraft.command;

import java.util.Collection;

import keepcraft.data.models.UserTeam;
import keepcraft.services.ChatService;
import keepcraft.services.UserService;
import org.bukkit.command.CommandSender;
import keepcraft.data.models.User;

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
                    // todo faction stats
                }
            }
        } // Red team chat
        else if (commandName.equals("1") && sender.isAdmin()) {
            Collection<User> connectedUsers = userService.getOnlineUsers();
            String message = "";
            for (String arg : args) {
                message += arg + " ";
            }
            chatService.sendFactionMessage(sender, connectedUsers, UserTeam.RED, message);
            return true;
        } // Blue team chat
        else if (commandName.equals("2") && sender.isAdmin()) {
            Collection<User> connectedUsers = userService.getOnlineUsers();
            String message = "";
            for (String arg : args) {
                message += arg + " ";
            }
            chatService.sendFactionMessage(sender, connectedUsers, UserTeam.BLUE, message);
            return true;
        } // Green team chat
        else if (commandName.equals("3") && sender.isAdmin()) {
            Collection<User> connectedUsers = userService.getOnlineUsers();
            String message = "";
            for (String arg : args) {
                message += arg + " ";
            }
            chatService.sendFactionMessage(sender, connectedUsers, UserTeam.GREEN, message);
            return true;
        }

        return false;
    }
}
