package keepcraft.listener;

import keepcraft.data.models.LootBlock;
import keepcraft.data.models.User;
import keepcraft.services.ChatService;
import keepcraft.services.LootBlockService;
import keepcraft.services.UserService;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class LootBlockListener implements Listener {

	private final UserService userService;
	private final LootBlockService lootBlockService;
	private final ChatService chatService;

	public LootBlockListener(UserService userService, LootBlockService lootBlockService, ChatService chatService) {
		this.userService = userService;
		this.lootBlockService = lootBlockService;
		this.chatService = chatService;

		lootBlockService.getLootBlocks().forEach(LootBlock::startDispensing);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) return;

		Block placed = event.getBlock();
		if (placed.getType() == Material.CHORUS_FLOWER) {
			Player player = event.getPlayer();
			User user = userService.getOnlineUser(player.getName());

			if (player.isOp() || user.isAdmin()) {
				// create a loot dispenser chest
				placed.setType(Material.CHEST);
				LootBlock lootBlock = lootBlockService.createLootBlock(placed);
				lootBlock.startDispensing();
				chatService.sendSuccessMessage(user, "Loot block placed");
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) return;

		Block broken = event.getBlock();
		if (broken.getType() == Material.CHEST) {
			Player player = event.getPlayer();
			User user = userService.getOnlineUser(player.getName());

			if (player.isOp() || user.isAdmin()) {
				LootBlock lootBlock = lootBlockService.getLootBlock(broken);
				lootBlock.stopDispensing();
				lootBlockService.removeLootBlock(lootBlock);
				chatService.sendSuccessMessage(user, "Loot block destroyed");
			} else {
				// Don't allow regular users to break loot blocks
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockDamage(BlockDamageEvent event) {
		Block damaged = event.getBlock();
		if (damaged.getType() == Material.CHEST) {
			Player player = event.getPlayer();
			User user = userService.getOnlineUser(player.getName());

			if (player.isOp() || user.isAdmin()) {
				LootBlock lootBlock = lootBlockService.getLootBlock(damaged);
				user.setTargetLootBlock(lootBlock);
				player.sendMessage(ChatService.Success + "Loot block targeted");
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onChunkUnload(ChunkUnloadEvent event) {
		Chunk chunk = event.getChunk();
		for (LootBlock lootBlock : lootBlockService.getLootBlocks()) {
			if (chunk.equals(lootBlock.getChunk())) {
				// Leave this chunk in the game world so the loot chest it contains continues to receive loot
				event.setCancelled(true);
			}
		}
	}

}
