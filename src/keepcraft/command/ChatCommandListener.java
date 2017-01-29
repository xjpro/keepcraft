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

        // Tell
        if (commandName.equals("t") && args.length > 1) {
            String targetName = args[0];
            User target = userService.getOnlineUser(targetName);

            if (target == null) {
                commandSender.sendMessage(ChatService.Failure + targetName + " is not online"); // no user
                return true;
            }

            String message = "";
            for (int i = 1; i < args.length; i++) {
                message += args[i] + " ";
            }

            chatService.sendPrivateMessage(sender, target, message);
            return true;
        } // Reply
        else if (commandName.equals("r") && args.length > 0) {
            String lastSenderName = sender.getLastPrivateMessageSender();
            User lastMessageSender = userService.getOnlineUser(lastSenderName);
            if (lastMessageSender == null) {
                if (lastSenderName != null) {
                    commandSender.sendMessage(ChatService.Failure + lastSenderName + " is not online"); // no user
                } else {
                    commandSender.sendMessage(ChatService.Failure + "You have not received any private messages");
                }
                return true;
            }

            String message = "";
            for (String arg : args) {
                message += arg + " ";
            }

            chatService.sendPrivateMessage(sender, lastMessageSender, message);
            return true;
        } // Global
        else if (commandName.equals("g") && args.length > 0) {
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
