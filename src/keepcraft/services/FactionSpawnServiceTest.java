package keepcraft.services;

import keepcraft.data.Database;
import keepcraft.data.FactionSpawnDataManager;
import keepcraft.data.models.FactionSpawn;
import keepcraft.data.models.UserTeam;
import keepcraft.data.models.WorldPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class FactionSpawnServiceTest {

	private FactionSpawnService factionSpawnService;

	@BeforeEach
	void setUp() {
		Database.deleteIfExists("keepcraft_test.db");
		Database database = new Database("keepcraft_test.db");
		FactionSpawnDataManager factionSpawnDataManager = new FactionSpawnDataManager(database);
		FactionSpawn redTeamSpawn = new FactionSpawn(UserTeam.RED.getId(), new WorldPoint(29, 30, -31));
		factionSpawnDataManager.putData(redTeamSpawn);
		factionSpawnService = new FactionSpawnService(factionSpawnDataManager);
	}

	@Test
	void getFactionSpawnExists() {
		FactionSpawn factionSpawn = factionSpawnService.getFactionSpawn(UserTeam.RED);
		assertNotNull(factionSpawn);
		assertEquals(UserTeam.RED.getId(), factionSpawn.getFactionValue());
		assertEquals(29, factionSpawn.getWorldPoint().x);
		assertEquals(30, factionSpawn.getWorldPoint().y);
		assertEquals(-31, factionSpawn.getWorldPoint().z);
	}

	@Test
	void getFactionSpawnNotExists() {
		FactionSpawn factionSpawn = factionSpawnService.getFactionSpawn(UserTeam.BLUE);
		assertNull(factionSpawn);
	}

	@Test
	void createFactionSpawn() {
		FactionSpawn factionSpawn = factionSpawnService.createFactionSpawn(UserTeam.BLUE, new WorldPoint(-800, 42, 359));
		assertNotNull(factionSpawn);
		assertEquals(UserTeam.BLUE.getId(), factionSpawn.getFactionValue());
		assertEquals(-800, factionSpawn.getWorldPoint().x);
		assertEquals(42, factionSpawn.getWorldPoint().y);
		assertEquals(359, factionSpawn.getWorldPoint().z);
		assertSame(factionSpawn, factionSpawnService.getFactionSpawn(UserTeam.BLUE));
	}

}