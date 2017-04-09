package keepcraft.listener;

import keepcraft.data.models.Container;
import keepcraft.data.models.User;
import keepcraft.data.models.WorldPoint;
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

public class ContainerListener implements Listener {

	private final UserService userService;
	private final ContainerService containerService;

	public ContainerListener(UserService userService, ContainerService containerService) {
		this.userService = userService;
		this.containerService = containerService;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) return;

		Block placed = event.getBlock();
		if (placed.getType() == Material.CHEST) {
			Player player = event.getPlayer();
			User user = userService.getOnlineUser(player.getName());

			if (player.isOp() || user.isAdmin()) {
				Container container = containerService.createContainer(new WorldPoint(placed.getLocation()));
				user.setTargetContainer(container);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) return;

		Block broken = event.getBlock();
		if (broken.getType() == Material.CHEST) {
			Container container = containerService.getContainer(new WorldPoint(broken.getLocation()));
			if (container != null) {
				containerService.removeContainer(container);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onRightClickChestBlock(PlayerInteractEvent event) {
		if (!event.hasBlock() || event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		Block clickedBlock = event.getClickedBlock();
		if (clickedBlock.getType() == Material.CHEST) {
			Container container = containerService.getContainer(new WorldPoint(clickedBlock.getLocation()));
			if (container != null) {
				Player player = event.getPlayer();
				User user = userService.getOnlineUser(player.getName());

				if (player.isOp() || user.isAdmin()) {
					user.setTargetContainer(container);
				}
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
