package keepcraft.listener;

import keepcraft.data.models.Container;
import keepcraft.data.models.User;
import keepcraft.data.models.WorldPoint;
import keepcraft.services.ChatService;
import keepcraft.services.ContainerService;
import keepcraft.services.UserService;
import org.bukkit.Chunk;
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

public class ContainerListener implements Listener {

	private final UserService userService;
	private final ContainerService containerService;
	private final ChatService chatService;

	public ContainerListener(UserService userService, ContainerService containerService, ChatService chatService) {
		this.userService = userService;
		this.containerService = containerService;
		this.chatService = chatService;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) return;

		Block placed = event.getBlock();
		if (placed.getState() instanceof InventoryHolder) {
			Player player = event.getPlayer();
			User user = userService.getOnlineUser(player.getName());

			if (player.isOp() || user.isAdmin()) {
				// flag container
				Container container = containerService.createContainer(new WorldPoint(placed.getLocation()));
				user.setTargetContainer(container);
				//chatService.sendSuccessMessage(user, "Container placed & targeted");
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) return;

		Block broken = event.getBlock();
		if (broken.getState() instanceof InventoryHolder) {
			Container container = containerService.getContainer(new WorldPoint(broken.getLocation()));
			if (container == null) return;

			Player player = event.getPlayer();
			User user = userService.getOnlineUser(player.getName());

			if (container.canAccess(user)) {
				chatService.sendFailureMessage(user, "You do not have permission to destroy this");
				containerService.removeContainer(container);
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
			Container container = containerService.getContainer(new WorldPoint(clickedBlock.getLocation()));
			if (container == null) return;

			Player player = event.getPlayer();
			User user = userService.getOnlineUser(player.getName());

			if (container.canAccess(user)) {
				if (container != user.getTargetContainer()) {
					user.setTargetContainer(container);
					// todo can't target chests in enemy territory
					//chatService.sendSuccessMessage(user, "Container targeted");
				}
			} else {
				chatService.sendFailureMessage(user, "You do not have permission to open this");
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onChunkUnload(ChunkUnloadEvent event) {
		Chunk chunk = event.getChunk();
		for (Container outputtingContainer : containerService.getOutputtingContainers()) {
			if (chunk.equals(outputtingContainer.getChunk())) {
				// Leave this chunk in the game world so the loot chest it contains continues to receive loot
				event.setCancelled(true);
			}
		}
	}

}
