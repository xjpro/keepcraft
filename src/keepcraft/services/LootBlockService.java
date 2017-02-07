package keepcraft.services;

import keepcraft.data.LootBlockDataManager;
import keepcraft.data.models.LootBlock;
import org.bukkit.block.Block;

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

	public LootBlock getLootBlock(Block block) {
		for (LootBlock lootBlock : getLootBlocks()) {
			if (lootBlock.getBlock().equals(block)) {
				return lootBlock;
			}
		}
		return null;
	}

	public void updateLootBlock(LootBlock lootBlock) {
		lootBlockDataManager.updateData(lootBlock);
	}

	public LootBlock createLootBlock(Block block) {
		LootBlock lootBlock = new LootBlock(0, block);
		lootBlockDataManager.putData(lootBlock);
		refreshCache();
		return getLootBlock(block);
	}

	public void removeLootBlock(LootBlock lootBlock) {
		lootBlockDataManager.deleteData(lootBlock);
		lootBlocks.remove(lootBlock);
	}
}