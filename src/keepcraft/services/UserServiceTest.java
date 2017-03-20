package keepcraft.services;

import keepcraft.data.Database;
import keepcraft.data.UserDataManager;
import keepcraft.data.UserStatsDataManager;
import keepcraft.data.models.User;
import keepcraft.data.models.UserFaction;
import keepcraft.data.models.UserStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class UserServiceTest {

	private UserDataManager userDataManager;
	private UserService userService;

	@BeforeEach
	void beforeEach() {
		Database.deleteIfExists("keepcraft_test.db");
		Database database = new Database("keepcraft_test.db");
		userDataManager = new UserDataManager(database);
		UserStatsDataManager userStatsDataManager = new UserStatsDataManager(database);

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
		userStats.playSeconds = 500;
		userStatsDataManager.saveData("PlayedAlot", uuid, userStats);
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


		// PlayedAlot: 500, Doji: 150, SummitMC: 90, Sivias: 80 | jjnguy: 60, AbeFrohman: 30, NeverPlays: 15

		userService = new UserService(null, userDataManager, userStatsDataManager);
	}

	@Test
	void teamAssignmentNonActivesFirst() {

		User abe = userService.loadOfflineUser("AbeFrohman"); // A or B
		User jjnguy = userService.loadOfflineUser("jjnguy"); // not on abe's team
		assertTeamsEqual();
		User never = userService.loadOfflineUser("NeverPlays"); // A or B
		User summit = userService.loadOfflineUser("SummitMC"); // first active, no other actives present so we can expect summit to be put on small team
		assertTeamsEqual();
		User sivias = userService.loadOfflineUser("Sivias"); // second active: not Summit's team
		User doji = userService.loadOfflineUser("DojiSuave"); // A or B
		assertTeamsEqual();
		User alot = userService.loadOfflineUser("PlayedAlot"); // not on Doji's team
		assertTeamsNotEqual();

		assertNotEquals(abe.getFaction(), jjnguy.getFaction()); // joined after eachother, would never be on same team
		assertNotEquals(summit.getFaction(), sivias.getFaction()); // first 2 actives, would not be on same team
		assertNotEquals(doji.getFaction(), alot.getFaction());
	}

	@Test
	void teamAssignmentActivesFirst() {
		User summit = userService.loadOfflineUser("SummitMC"); // A or B
		User sivias = userService.loadOfflineUser("Sivias"); // not Summit's team
		assertTeamsEqual();
		User doji = userService.loadOfflineUser("DojiSuave"); // A or B
		User alot = userService.loadOfflineUser("PlayedAlot"); // not on Doji's team
		assertTeamsEqual();
		User abe = userService.loadOfflineUser("AbeFrohman"); // A or B
		User jjnguy = userService.loadOfflineUser("jjnguy"); // not on Abe's team
		assertTeamsEqual();
		User never = userService.loadOfflineUser("NeverPlays"); // which team needs numbers
		assertTeamsNotEqual();

		assertNotEquals(summit.getFaction(), sivias.getFaction()); // joined after eachother, would never be on same team
		assertNotEquals(doji.getFaction(), alot.getFaction());
		assertNotEquals(abe.getFaction(), jjnguy.getFaction());
	}

	@Test
	void teamAssignment1() {

		User summit = userService.loadOfflineUser("SummitMC"); // put on team A
		User jjnguy = userService.loadOfflineUser("jjnguy"); // as non-active, should be put on team B to balance numbers
		assertTeamsEqual();
		User sivias = userService.loadOfflineUser("Sivias"); // normally random but since active player, should be on team B
		User never = userService.loadOfflineUser("NeverPlays"); // as non-active, should be put on team A to balance numbers
		assertTeamsEqual();
		User doji = userService.loadOfflineUser("DojiSuave"); // active players are even, selection random
		User abe = userService.loadOfflineUser("AbeFrohman"); // should not be on doji's team
		User alot = userService.loadOfflineUser("PlayedAlot");
		assertTeamsNotEqual();

		assertNotEquals(summit.getFaction(), sivias.getFaction()); // 1 and 2nd active players, should not be on same team
		assertNotEquals(summit.getFaction(), jjnguy.getFaction()); // first non-active to join, should have balanced numbers
		assertEquals(summit.getFaction(), never.getFaction()); // second non-active to join, should have balanced numbers
		assertNotEquals(doji.getFaction(), abe.getFaction()); // last non-active to join, should have balanced numbers
	}

	@Test
	void teamAssignmentMixed2() {
		User alot = userService.loadOfflineUser("PlayedAlot"); // A or B
		User summit = userService.loadOfflineUser("SummitMC"); // not on alot's team
		assertTeamsEqual();
		User abe = userService.loadOfflineUser("AbeFrohman"); // A or B
		User never = userService.loadOfflineUser("NeverPlays"); // not on abe's team
		assertTeamsEqual();
		User sivias = userService.loadOfflineUser("Sivias"); // A or B
		User doji = userService.loadOfflineUser("DojiSuave"); // not on Sivias' team
		assertTeamsEqual();

		assertNotEquals(summit.getFaction(), alot.getFaction());
		assertNotEquals(abe.getFaction(), never.getFaction());
		assertNotEquals(sivias.getFaction(), doji.getFaction());
	}

	@Test
	void teamAssignmentMixed3() {
		User summit = userService.loadOfflineUser("SummitMC"); // A or B
		User never = userService.loadOfflineUser("NeverPlays"); // not on summit's team
		assertTeamsEqual();
		User sivias = userService.loadOfflineUser("Sivias"); // not on summit's team, on Never's team
		User abe = userService.loadOfflineUser("AbeFrohman"); // on summit's team to even things out
		assertTeamsEqual();
		User jjnguy = userService.loadOfflineUser("jjnguy"); // A or B, random
		User doji = userService.loadOfflineUser("DojiSuave"); // actives even, not on jjnguy's team, evening things again
		assertTeamsEqual();
		User alot = userService.loadOfflineUser("PlayedAlot"); // not on Doji's team, actives now even

		assertNotEquals(summit.getFaction(), never.getFaction());
		assertNotEquals(summit.getFaction(), sivias.getFaction());
		assertEquals(never.getFaction(), sivias.getFaction());
		assertEquals(summit.getFaction(), abe.getFaction());
		assertNotEquals(jjnguy.getFaction(), doji.getFaction());
		assertNotEquals(doji.getFaction(), alot.getFaction());
	}

	private void assertTeamsEqual() {
		int redCount = userDataManager.getFactionCount(UserFaction.RED.getId());
		int blueCount = userDataManager.getFactionCount(UserFaction.BLUE.getId());
		assertEquals(redCount, blueCount);
	}

	private void assertTeamsNotEqual() {
		int redCount = userDataManager.getFactionCount(UserFaction.RED.getId());
		int blueCount = userDataManager.getFactionCount(UserFaction.BLUE.getId());
		assertNotEquals(redCount, blueCount);
	}
}
