package keepcraft.listener;

import keepcraft.data.models.LootBlock;
import keepcraft.data.models.User;
import keepcraft.data.models.WorldPoint;
import keepcraft.services.ChatService;
import keepcraft.services.ContainerService;
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
import org.bukkit.inventory.InventoryHolder;

public class LootBlockListener implements Listener {

	private final UserService userService;
	private final ContainerService containerService;
	private final ChatService chatService;

	public LootBlockListener(UserService userService, ContainerService containerService, ChatService chatService) {
		this.userService = userService;
		this.containerService = containerService;
		this.chatService = chatService;
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
				LootBlock lootBlock = containerService.createLootBlock(new WorldPoint(placed.getLocation()));
				user.setTargetContainer(lootBlock);
				chatService.sendSuccessMessage(user, "Loot block placed & targeted");
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) return;

		Block broken = event.getBlock();
		if (broken.getState() instanceof InventoryHolder) {
			LootBlock lootBlock = containerService.getLootBlock(new WorldPoint(broken.getLocation()));
			if (lootBlock == null) return;

			Player player = event.getPlayer();
			User user = userService.getOnlineUser(player.getName());

			if (player.isOp() || user.isAdmin()) {
				containerService.removeLootBlock(lootBlock);
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
		if (clickedBlock.getState() instanceof InventoryHolder) {
			LootBlock lootBlock = containerService.getLootBlock(new WorldPoint(clickedBlock.getLocation()));
			if (lootBlock == null) return;

			Player player = event.getPlayer();
			User user = userService.getOnlineUser(player.getName());

			if (player.isOp() || user.isAdmin()) {
				user.setTargetContainer(lootBlock);
				chatService.sendSuccessMessage(user, "Loot block targeted");
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onChunkUnload(ChunkUnloadEvent event) {
		Chunk chunk = event.getChunk();
		for (LootBlock outputtingContainer : containerService.getOutputtingContainers()) {
			if (chunk.equals(outputtingContainer.getChunk())) {
				// Leave this chunk in the game world so the loot chest it contains continues to receive loot
				event.setCancelled(true);
			}
		}
	}

}
