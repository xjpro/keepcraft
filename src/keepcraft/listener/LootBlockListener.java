package keepcraft.listener;

import keepcraft.data.models.LootBlock;
import keepcraft.data.models.User;
import keepcraft.data.models.WorldPoint;
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
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
		if (placed.getType() == Material.TRAPPED_CHEST) {
			Player player = event.getPlayer();
			User user = userService.getOnlineUser(player.getName());

			if (player.isOp() || user.isAdmin()) {
				// create a loot dispenser chest
				placed.setType(Material.CHEST);
				LootBlock lootBlock = lootBlockService.createLootBlock(new WorldPoint(placed.getLocation()));
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
			LootBlock lootBlock = lootBlockService.getLootBlock(new WorldPoint(broken.getLocation()));
			if (lootBlock == null) return;

			Player player = event.getPlayer();
			User user = userService.getOnlineUser(player.getName());

			if (player.isOp() || user.isAdmin()) {
				lootBlock.stopDispensing();
				lootBlockService.removeLootBlock(lootBlock);
				chatService.sendSuccessMessage(user, "Loot block destroyed");
			} else {
				// Don't allow regular users to break loot blocks
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onRightClickChestBlock(PlayerInteractEvent event) {
		if (!event.hasBlock() || event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		Block clickedBlock = event.getClickedBlock();
		if (clickedBlock.getType() == Material.CHEST) {
			LootBlock lootBlock = lootBlockService.getLootBlock(new WorldPoint(clickedBlock.getLocation()));
			if (lootBlock == null) return;

			Player player = event.getPlayer();
			User user = userService.getOnlineUser(player.getName());

			if (player.isOp() || user.isAdmin()) {
				user.setTargetLootBlock(lootBlock);
				chatService.sendSuccessMessage(user, "Loot block targeted");
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
