package keepcraft.command;

import keepcraft.services.ContainerService;
import keepcraft.services.UserService;
import org.bukkit.command.CommandSender;
import keepcraft.services.ChatService;
import keepcraft.data.models.LootBlock;
import keepcraft.data.models.User;

public class LootBlockCommandListener extends CommandListener {

    private final UserService userService;
    private final ContainerService containerService;
    private final ChatService chatService;

    public LootBlockCommandListener(UserService userService, ContainerService containerService, ChatService chatService) {
    	this.userService = userService;
    	this.containerService = containerService;
    	this.chatService = chatService;
	}

    @Override
    protected boolean handle(String commandName, CommandSender commandSender, String[] args) {
		if (!commandSender.isOp()) return false;

        User sender = userService.getOnlineUser(commandSender.getName());
        LootBlock lootBlock = sender.getTargetContainer();

        if (lootBlock == null) {
        	chatService.sendFailureMessage(sender, "No target - open a container to target it");
            return true;
        }

        if (commandName.equalsIgnoreCase("lootblock") && args.length > 1) {
            if (args[0].equalsIgnoreCase("output") && args.length == 2) {
                int output;
                try {
                    output = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    // invalid input
                    return false;
                }
                lootBlock.setOutputPerHour(output);
				containerService.updateContainer(lootBlock);
                chatService.sendSuccessMessage(sender, String.format("Container output set to %s per hour", output));
                return true;
            } else if (args[0].equalsIgnoreCase("type") && args.length == 2) {
                int typeId;
                try {
                    typeId = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    // invalid input
                    return false;
                }
                lootBlock.setType(LootBlock.ContainerType.getContainerType(typeId));
				containerService.updateContainer(lootBlock);
                chatService.sendSuccessMessage(sender, "Container type set to " + typeId);
                return true;
            } else if (args[0].equalsIgnoreCase("status") && args.length == 2) {
                int status;
                try {
                    status = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    // invalid input
                    return false;
                }
                lootBlock.setStatus(status);
				containerService.updateContainer(lootBlock);
                chatService.sendSuccessMessage(sender, "Container status set to " + status);
                return true;
            }
        }

        return false;
    }

}
