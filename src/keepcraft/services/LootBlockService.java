package keepcraft.services;

import keepcraft.data.LootBlockDataManager;
import keepcraft.data.models.LootBlock;
import keepcraft.data.models.WorldPoint;

import java.util.Collection;

public class LootBlockService {

	private final LootBlockDataManager lootBlockDataManager;
	private Collection<LootBlock> lootBlocks;

	public LootBlockService(LootBlockDataManager lootBlockDataManager) {
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
}
