package keepcraft.services;

import keepcraft.data.LootBlockDataManager;
import keepcraft.data.models.LootBlock;
import keepcraft.data.models.WorldPoint;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Collection;

public class LootBlockService {

	private final Plugin plugin;
	private final LootBlockDataManager lootBlockDataManager;
	private Collection<LootBlock> lootBlocks;
	private int taskId = 0;

	public LootBlockService(Plugin plugin, LootBlockDataManager lootBlockDataManager) {
		this.plugin = plugin;
		this.lootBlockDataManager = lootBlockDataManager;
		refreshCache();
	}

	public void refreshCache() {
		lootBlocks = lootBlockDataManager.getAllData();
	}

	public Collection<LootBlock> getLootBlocks() {
		return lootBlocks;
	}

	public LootBlock getLootBlock(WorldPoint worldPoint) {
		for (LootBlock lootBlock : getLootBlocks()) {
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
			for (LootBlock lootBlock : getLootBlocks()) {
				lootBlock.dispense();
			}
		}, 1200, 0);
	}

	public void stopDispensing() {
		Bukkit.getScheduler().cancelTask(taskId);
	}
}
