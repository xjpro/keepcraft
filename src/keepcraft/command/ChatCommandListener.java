package keepcraft.command;

import org.bukkit.command.CommandSender;
import keepcraft.Chat;
import keepcraft.data.DataCache;
import keepcraft.data.models.User;

public class ChatCommandListener extends CommandListener {

    @Override
    protected boolean handle(String commandName, CommandSender commandSender, String[] args) {
        User sender = DataCache.retrieve(User.class, commandSender.getName());

        // Tell
        if (commandName.equals("t") && args.length > 1) {
            String targetName = args[0];
            User target = DataCache.retrieve(User.class, targetName);

            if (target == null) {
                commandSender.sendMessage(Chat.Failure + targetName + " is not online"); // no user
                return true;
            }

            String message = "";
            for (int i = 1; i < args.length; i++) {
                message += args[i] + " ";
            }

            Chat.sendPrivateMessage(sender, target, message);
            return true;
        } // Reply
        else if (commandName.equals("r") && args.length > 0) {
            String lastSenderName = sender.getLastPrivateMessageSender();
            User lastMessageSender = DataCache.retrieve(User.class, lastSenderName);
            if (lastMessageSender == null) {
                if (lastSenderName != null) {
                    commandSender.sendMessage(Chat.Failure + lastSenderName + " is not online"); // no user
                } else {
                    commandSender.sendMessage(Chat.Failure + "You have not received any private messages");
                }
                return true;
            }

            String message = "";
            for (int i = 0; i < args.length; i++) {
                message += args[i] + " ";
            }

            Chat.sendPrivateMessage(sender, lastMessageSender, message);
            return true;
        } // Global
        else if (commandName.equals("g") && args.length > 0) {
            String message = "";
            for (int i = 0; i < args.length; i++) {
                message += args[i] + " ";
            }
            Chat.sendGlobalMessage(sender, message);
            return true;
        }

        return false;
    }

}
