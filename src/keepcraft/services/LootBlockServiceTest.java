package keepcraft.services;

import keepcraft.data.Database;
import keepcraft.data.LootBlockDataManager;
import keepcraft.data.models.LootBlock;
import keepcraft.data.models.WorldPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LootBlockServiceTest {

	private LootBlockDataManager lootBlockDataManager;
	private LootBlockService lootBlockService;

	@BeforeEach
	void setUp() {
		Database.deleteIfExists("keepcraft_test.db");
		Database database = new Database("keepcraft_test.db");
		lootBlockDataManager = new LootBlockDataManager(database);
		lootBlockDataManager.putData(new LootBlock(new WorldPoint(238, 4, -239)));
		lootBlockService = new LootBlockService(lootBlockDataManager);
	}

	@Test
	void getLootBlocks() {
		LootBlock[] lootBlocks = lootBlockService.getLootBlocks().stream().toArray(LootBlock[]::new);
		assertEquals(1, lootBlocks.length);
		assertNotNull(lootBlocks[0]);
	}

	@Test
	void getLootBlockExists() {
		LootBlock lootBlock = lootBlockService.getLootBlock(new WorldPoint(238, 4, -239));
		assertNotNull(lootBlock);
		assertEquals(238, lootBlock.getWorldPoint().x);
		assertEquals(4, lootBlock.getWorldPoint().y);
		assertEquals(-239, lootBlock.getWorldPoint().z);
	}

	@Test
	void getLootBlockNotExists() {
		LootBlock lootBlock = lootBlockService.getLootBlock(new WorldPoint(0, 0, 0));
		assertNull(lootBlock);
	}

	@Test
	void updateLootBlock() {
		LootBlock lootBlock = lootBlockService.getLootBlock(new WorldPoint(238, 4, -239));
		lootBlock.setType(3);
		lootBlock.setOutputPerHour(99);
		lootBlock.setStatus(2);
		lootBlockService.updateLootBlock(lootBlock);

		LootBlock[] lootBlocks = lootBlockDataManager.getAllData().stream().toArray(LootBlock[]::new);
		assertEquals(1, lootBlocks.length);
		assertNotNull(lootBlocks[0]);
		assertEquals(3, lootBlocks[0].getType());
		assertEquals(99, lootBlocks[0].getOutputPerHour());
		assertEquals(2, lootBlocks[0].getStatus());
	}

	@Test
	void createLootBlock() {
		LootBlock lootBlock = lootBlockService.createLootBlock(new WorldPoint(-140, 54, 399));
		assertNotNull(lootBlock);
		assertEquals(-140, lootBlock.getWorldPoint().x);
		assertEquals(54, lootBlock.getWorldPoint().y);
		assertEquals(399, lootBlock.getWorldPoint().z);

		// Check both service and data manager have stored the new loot block
		LootBlock[] lootBlocksFromService = lootBlockService.getLootBlocks().stream().toArray(LootBlock[]::new);
		assertEquals(2, lootBlocksFromService.length);
		assertEquals(lootBlocksFromService[1].getWorldPoint(), lootBlock.getWorldPoint());
		LootBlock[] lootBlocksFromDatabase = lootBlockDataManager.getAllData().stream().toArray(LootBlock[]::new);
		assertEquals(2, lootBlocksFromDatabase.length);
		assertEquals(lootBlocksFromDatabase[1].getWorldPoint(), lootBlock.getWorldPoint());
	}

	@Test
	void removeLootBlock() {
		LootBlock lootBlock = lootBlockService.getLootBlock(new WorldPoint(238, 4, -239));
		lootBlockService.removeLootBlock(lootBlock);

		// Check both service and data manager have removed the loot block
		LootBlock[] lootBlocksFromService = lootBlockService.getLootBlocks().stream().toArray(LootBlock[]::new);
		assertEquals(0, lootBlocksFromService.length);
		LootBlock[] lootBlocksFromDatabase = lootBlockDataManager.getAllData().stream().toArray(LootBlock[]::new);
		assertEquals(0, lootBlocksFromDatabase.length);
	}

}