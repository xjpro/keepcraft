package keepcraft.services;

import keepcraft.data.Database;
import keepcraft.data.PlotDataManager;
import keepcraft.data.UserDataManager;
import keepcraft.data.UserStatsDataManager;
import keepcraft.data.models.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

	private UserDataManager userDataManager;
	private UserStatsDataManager userStatsDataManager;
	private UserService userService;

	@BeforeEach
	void beforeEach() {
		Database.deleteIfExists("keepcraft_test.db");
		Database database = new Database("keepcraft_test.db");
		userDataManager = new UserDataManager(database);
		userStatsDataManager = new UserStatsDataManager(database);

		// World 1
		UUID uuid = UUID.randomUUID();
		UserStats userStats = new UserStats();
		userStats.playSeconds = 30;
		userStatsDataManager.saveData("SummitMC", uuid, userStats);
		userStats = new UserStats();
		userStats.playSeconds = 20;
		userStatsDataManager.saveData("Sivias", uuid, userStats);
		userStats = new UserStats();
		userStats.playSeconds = 10;
		userStatsDataManager.saveData("jjnguy", uuid, userStats);
		userStats = new UserStats();
		userStats.playSeconds = 50;
		userStatsDataManager.saveData("DojiSuave", uuid, userStats);
		userStats = new UserStats();
		userStats.playSeconds = 10;
		userStatsDataManager.saveData("AbeFrohman", uuid, userStats);
		userStats = new UserStats();
		userStats.playSeconds = 5;
		userStatsDataManager.saveData("NeverPlays", uuid, userStats);

		// World 2
		uuid = UUID.randomUUID();
		userStats = new UserStats();
		userStats.playSeconds = 10;
		userStatsDataManager.saveData("SummitMC", uuid, userStats);
		userStats = new UserStats();
		userStats.playSeconds = 20;
		userStatsDataManager.saveData("Sivias", uuid, userStats);
		userStats = new UserStats();
		userStats.playSeconds = 30;
		userStatsDataManager.saveData("jjnguy", uuid, userStats);
		userStats = new UserStats();
		userStats.playSeconds = 50;
		userStatsDataManager.saveData("DojiSuave", uuid, userStats);
		userStats = new UserStats();
		userStats.playSeconds = 10;
		userStatsDataManager.saveData("AbeFrohman", uuid, userStats);
		userStats = new UserStats();
		userStats.playSeconds = 5;
		userStatsDataManager.saveData("NeverPlays", uuid, userStats);

		// World 3
		uuid = UUID.randomUUID();
		userStats = new UserStats();
		userStats.playSeconds = 50;
		userStatsDataManager.saveData("SummitMC", uuid, userStats);
		userStats = new UserStats();
		userStats.playSeconds = 40;
		userStatsDataManager.saveData("Sivias", uuid, userStats);
		userStats = new UserStats();
		userStats.playSeconds = 20;
		userStatsDataManager.saveData("jjnguy", uuid, userStats);
		userStats = new UserStats();
		userStats.playSeconds = 50;
		userStatsDataManager.saveData("DojiSuave", uuid, userStats);
		userStats = new UserStats();
		userStats.playSeconds = 10;
		userStatsDataManager.saveData("AbeFrohman", uuid, userStats);
		userStats = new UserStats();
		userStats.playSeconds = 5;
		userStatsDataManager.saveData("NeverPlays", uuid, userStats);

		// World 4, shouldn't affect anything but we'll give it crazy values to fail tests
		uuid = UUID.fromString("2b79c281-7287-4627-96fc-788a03901345");
		userStats = new UserStats();
		userStats.playSeconds = 9001;
		userStatsDataManager.saveData("Sivias", uuid, userStats);
		userStats = new UserStats();
		userStats.playSeconds = 50000;
		userStatsDataManager.saveData("jjnguy", uuid, userStats);


		// Doji: 150, SummitMC: 90, Sivias: 80 | jjnguy: 60, AbeFrohman: 30, NeverPlays: 15

		userService = new UserService(null, userDataManager, userStatsDataManager);
	}

	@Test
	void teamAssignmentNonActivesFirst() {

		User abe = userService.loadOfflineUser("AbeFrohman"); // A
		User jjnguy = userService.loadOfflineUser("jjnguy"); // B
		User never = userService.loadOfflineUser("NeverPlays"); // A or B
		User summit = userService.loadOfflineUser("SummitMC"); // A or B
		User sivias = userService.loadOfflineUser("Sivias"); // not Summit's team
		User doji = userService.loadOfflineUser("DojiSuave"); // A or B

		assertNotEquals(abe.getFaction(), jjnguy.getFaction()); // joined after eachother, would never be on same team
		assertNotEquals(summit.getFaction(), sivias.getFaction()); // first 2 actives, would not be on same team

		int redCount = userDataManager.getFactionCount(UserFaction.FactionRed);
		int blueCount = userDataManager.getFactionCount(UserFaction.FactionRed);
		assertEquals(redCount, blueCount);
	}

	@Test
	void teamAssignmentActivesFirst() {
		User summit = userService.loadOfflineUser("SummitMC"); // A or B
		User sivias = userService.loadOfflineUser("Sivias"); // not Summit's team
		User doji = userService.loadOfflineUser("DojiSuave"); // A or B
		User abe = userService.loadOfflineUser("AbeFrohman"); // which team needs numbers
		User jjnguy = userService.loadOfflineUser("jjnguy"); // A or B
		User never = userService.loadOfflineUser("NeverPlays"); // which team needs numbers

		assertNotEquals(summit.getFaction(), sivias.getFaction()); // joined after eachother, would never be on same team
		int redCount = userDataManager.getFactionCount(UserFaction.FactionRed);
		int blueCount = userDataManager.getFactionCount(UserFaction.FactionRed);
		assertEquals(redCount, blueCount);
	}

	@Test
	void teamAssignmentMixed() {

		User summit = userService.loadOfflineUser("SummitMC"); // put on team A
		User jjnguy = userService.loadOfflineUser("jjnguy"); // as non-active, should be put on team B to balance numbers
		// teams even, next assignment random unless it's active player
		User sivias = userService.loadOfflineUser("Sivias"); // normally random but since active player, should be on team B
		User never = userService.loadOfflineUser("NeverPlays"); // as non-active, should be put on team A to balance numbers
		// teams even, next assignment random unless it's active player
		User doji = userService.loadOfflineUser("DojiSuave"); // active players are even, selection random
		User abe = userService.loadOfflineUser("AbeFrohman"); // should not be on doji's team

		assertNotEquals(summit.getFaction(), sivias.getFaction()); // 1 and 2nd active players, should not be on same team
		assertNotEquals(summit.getFaction(), jjnguy.getFaction()); // first non-active to join, should have balanced numbers
		assertEquals(summit.getFaction(), never.getFaction()); // second non-active to join, should have balanced numbers
		assertNotEquals(doji.getFaction(), abe.getFaction()); // last non-active to join, should have balanced numbers

		int redCount = userDataManager.getFactionCount(UserFaction.FactionRed);
		int blueCount = userDataManager.getFactionCount(UserFaction.FactionRed);
		assertEquals(redCount, blueCount);
	}

}
