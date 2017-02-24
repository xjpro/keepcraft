package keepcraft.services;

import keepcraft.data.LootBlockDataManager;
import keepcraft.data.models.LootBlock;
import keepcraft.data.models.WorldPoint;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.stream.Collectors;

public class ContainerService {

	private final Plugin plugin;
	private final LootBlockDataManager lootBlockDataManager;
	private Collection<LootBlock> lootBlocks;
	private int taskId = 0;

	public ContainerService(Plugin plugin, LootBlockDataManager lootBlockDataManager) {
		this.plugin = plugin;
		this.lootBlockDataManager = lootBlockDataManager;
		refreshCache();
	}

	public void refreshCache() {
		lootBlocks = lootBlockDataManager.getAllData();
	}

	public Collection<LootBlock> getContainers() {
		return lootBlocks;
	}

	public Collection<LootBlock> getOutputtingContainers() {
		return lootBlocks.stream().filter(container -> container.getOutputPerHour() > 0).collect(Collectors.toList());
	}

	public LootBlock getLootBlock(WorldPoint worldPoint) {
		for (LootBlock lootBlock : getContainers()) {
			if (lootBlock.getWorldPoint().equals(worldPoint)) {
				return lootBlock;
			}
		}
		return null;
	}

	public void updateLootBlock(LootBlock lootBlock) {
		lootBlockDataManager.updateData(lootBlock);
	}

	public LootBlock createLootBlock(WorldPoint worldPoint) {
		LootBlock lootBlock = new LootBlock(worldPoint);
		lootBlockDataManager.putData(lootBlock);
		lootBlocks.add(lootBlock);
		return lootBlock;
	}

	public void removeLootBlock(LootBlock lootBlock) {
		lootBlockDataManager.deleteData(lootBlock);
		lootBlocks.remove(lootBlock);
	}

	public void startDispensing() {
		if (plugin == null) return;

		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
			for (LootBlock lootBlock : getOutputtingContainers()) {
				lootBlock.dispense();
			}
		}, 1200, 1200);
	}

	public void stopDispensing() {
		Bukkit.getScheduler().cancelTask(taskId);
	}
}
