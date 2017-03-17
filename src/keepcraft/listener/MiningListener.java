package keepcraft.listener;

import keepcraft.WorldSetter;
import keepcraft.data.MapDataManager;
import keepcraft.data.models.WorldPoint;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class MiningListener {

	private final int MAX_ADDITIONAL_ITEMS_CREATED = 2;
	private final Random random = new Random();
	private final WorldPoint center;

	public MiningListener(MapDataManager mapDataManager) {
		this.center = mapDataManager.getMapCenter();
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) return;
		Block block = event.getBlock();

		// If the event drops no xp there's no bonus drops
		if (event.getExpToDrop() == 0) return;

		ItemStack drop = null;
		switch (block.getType()) {
			case DIAMOND_ORE:
				drop = new ItemStack(Material.DIAMOND);
				break;
			case EMERALD_ORE:
				drop = new ItemStack(Material.EMERALD);
				break;
			case LAPIS_ORE:
				drop = new ItemStack(Material.INK_SACK, 1, (byte) 4); // todo is this right?
				break;
			case GOLD_ORE:
				drop = new ItemStack(Material.GOLD_ORE);
				break;
			case REDSTONE_ORE:
				drop = new ItemStack(Material.REDSTONE);
				break;
			case IRON_ORE:
				drop = new ItemStack(Material.IRON_ORE);
				break;
			case COAL_ORE:
				drop = new ItemStack(Material.COAL);
				break;
			case QUARTZ_ORE:
				drop = new ItemStack(Material.QUARTZ);
				break;
		}

		// If there's no drop the block type doesn't generate bonus drops
		if (drop == null) return;

		double distanceFromCenter = center.distanceIgnoreY(block.getX(), block.getZ());
		// Calculate 0-100% of how close block is from center, stopping at team territory
		double additionalItemsCreated = MAX_ADDITIONAL_ITEMS_CREATED * Math.max(1 - (distanceFromCenter / (WorldSetter.BASE_DISTANCE_FROM_CENTER - WorldSetter.TEAM_PLOT_RADIUS)), 0);
		if (additionalItemsCreated == 0) return;

		int guaranteedItemsDropped = (int) additionalItemsCreated;
		double chanceOfAdditionalItemsDropped = additionalItemsCreated - guaranteedItemsDropped;

		for (int i = 0; i < guaranteedItemsDropped; i++) {
			block.getWorld().dropItemNaturally(block.getLocation(), drop);
		}
		if (chanceOfAdditionalItemsDropped > random.nextDouble()) {
			block.getWorld().dropItemNaturally(block.getLocation(), drop);
		}
	}
}
