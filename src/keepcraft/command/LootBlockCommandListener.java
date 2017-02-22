package keepcraft.command;

import keepcraft.services.LootBlockService;
import keepcraft.services.UserService;
import org.bukkit.command.CommandSender;
import keepcraft.services.ChatService;
import keepcraft.data.models.LootBlock;
import keepcraft.data.models.User;

public class LootBlockCommandListener extends CommandListener {

    private final UserService userService;
    private final LootBlockService lootBlockService;
    private final ChatService chatService;

    public LootBlockCommandListener(UserService userService, LootBlockService lootBlockService, ChatService chatService) {
    	this.userService = userService;
    	this.lootBlockService = lootBlockService;
    	this.chatService = chatService;
	}

    @Override
    protected boolean handle(String commandName, CommandSender commandSender, String[] args) {
		if (!commandSender.isOp()) return false;

        User sender = userService.getOnlineUser(commandSender.getName());
        LootBlock lootBlock = sender.getTargetLootBlock();

        if (lootBlock == null) {
        	chatService.sendFailureMessage(sender, "No target - right click a loot block to target it");
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
				lootBlockService.updateLootBlock(lootBlock);
                chatService.sendSuccessMessage(sender, String.format("Loot block output set to %s per hour", output));
                return true;
            } else if (args[0].equalsIgnoreCase("type") && args.length == 2) {
                int type;
                try {
                    type = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    // invalid input
                    return false;
                }
                lootBlock.setType(type);
				lootBlockService.updateLootBlock(lootBlock);
                chatService.sendSuccessMessage(sender, "Loot block type set to " + type);
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
				lootBlockService.updateLootBlock(lootBlock);
                chatService.sendSuccessMessage(sender, "Loot block status set to " + status);
                return true;
            }
        }

        return false;
    }

}
