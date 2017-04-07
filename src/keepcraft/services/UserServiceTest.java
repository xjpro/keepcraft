package keepcraft.services;

import keepcraft.data.*;
import keepcraft.data.models.User;
import keepcraft.data.models.UserTeam;
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
		UserConnectionDataManager userConnectionDataManager = new UserConnectionDataManager(database);
		ApprovalDataManager approvalDataManager = new ApprovalDataManager(database);

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

		userService = new UserService(null, userDataManager, userStatsDataManager, userConnectionDataManager, approvalDataManager);
	}

	@Test
	void teamAssignmentNonActivesFirst() {

		User abe = userService.loadOfflineUser("AbeFrohman", "localhost"); // A or B
		User jjnguy = userService.loadOfflineUser("jjnguy", "localhost"); // not on abe's team
		assertTeamsEqual();
		User never = userService.loadOfflineUser("NeverPlays", "localhost"); // A or B
		User summit = userService.loadOfflineUser("SummitMC", "localhost"); // first active, no other actives present so we can expect summit to be put on small team
		assertTeamsEqual();
		User sivias = userService.loadOfflineUser("Sivias", "localhost"); // second active: not Summit's team
		User doji = userService.loadOfflineUser("DojiSuave", "localhost"); // A or B
		assertTeamsEqual();
		User alot = userService.loadOfflineUser("PlayedAlot", "localhost"); // not on Doji's team
		assertTeamsNotEqual();

		assertNotEquals(abe.getTeam(), jjnguy.getTeam()); // joined after eachother, would never be on same team
		assertNotEquals(summit.getTeam(), sivias.getTeam()); // first 2 actives, would not be on same team
		assertNotEquals(doji.getTeam(), alot.getTeam());
	}

	@Test
	void teamAssignmentActivesFirst() {
		User summit = userService.loadOfflineUser("SummitMC", "localhost"); // A or B
		User sivias = userService.loadOfflineUser("Sivias", "localhost"); // not Summit's team
		assertTeamsEqual();
		User doji = userService.loadOfflineUser("DojiSuave", "localhost"); // A or B
		User alot = userService.loadOfflineUser("PlayedAlot", "localhost"); // not on Doji's team
		assertTeamsEqual();
		User abe = userService.loadOfflineUser("AbeFrohman", "localhost"); // A or B
		User jjnguy = userService.loadOfflineUser("jjnguy", "localhost"); // not on Abe's team
		assertTeamsEqual();
		User never = userService.loadOfflineUser("NeverPlays", "localhost"); // which team needs numbers
		assertTeamsNotEqual();

		assertNotEquals(summit.getTeam(), sivias.getTeam()); // joined after eachother, would never be on same team
		assertNotEquals(doji.getTeam(), alot.getTeam());
		assertNotEquals(abe.getTeam(), jjnguy.getTeam());
	}

	@Test
	void teamAssignment1() {

		User summit = userService.loadOfflineUser("SummitMC", "localhost"); // put on team A
		User jjnguy = userService.loadOfflineUser("jjnguy", "localhost"); // as non-active, should be put on team B to balance numbers
		assertTeamsEqual();
		User sivias = userService.loadOfflineUser("Sivias", "localhost"); // normally random but since active player, should be on team B
		User never = userService.loadOfflineUser("NeverPlays", "localhost"); // as non-active, should be put on team A to balance numbers
		assertTeamsEqual();
		User doji = userService.loadOfflineUser("DojiSuave", "localhost"); // active players are even, selection random
		User abe = userService.loadOfflineUser("AbeFrohman", "localhost"); // should not be on doji's team
		User alot = userService.loadOfflineUser("PlayedAlot", "localhost");
		assertTeamsNotEqual();

		assertNotEquals(summit.getTeam(), sivias.getTeam()); // 1 and 2nd active players, should not be on same team
		assertNotEquals(summit.getTeam(), jjnguy.getTeam()); // first non-active to join, should have balanced numbers
		assertEquals(summit.getTeam(), never.getTeam()); // second non-active to join, should have balanced numbers
		assertNotEquals(doji.getTeam(), abe.getTeam()); // last non-active to join, should have balanced numbers
	}

	@Test
	void teamAssignmentMixed2() {
		User alot = userService.loadOfflineUser("PlayedAlot", "localhost"); // A or B
		User summit = userService.loadOfflineUser("SummitMC", "localhost"); // not on alot's team
		assertTeamsEqual();
		User abe = userService.loadOfflineUser("AbeFrohman", "localhost"); // A or B
		User never = userService.loadOfflineUser("NeverPlays", "localhost"); // not on abe's team
		assertTeamsEqual();
		User sivias = userService.loadOfflineUser("Sivias", "localhost"); // A or B
		User doji = userService.loadOfflineUser("DojiSuave", "localhost"); // not on Sivias' team
		assertTeamsEqual();

		assertNotEquals(summit.getTeam(), alot.getTeam());
		assertNotEquals(abe.getTeam(), never.getTeam());
		assertNotEquals(sivias.getTeam(), doji.getTeam());
	}

	@Test
	void teamAssignmentMixed3() {
		User summit = userService.loadOfflineUser("SummitMC", "localhost"); // A or B
		User never = userService.loadOfflineUser("NeverPlays", "localhost"); // not on summit's team
		assertTeamsEqual();
		User sivias = userService.loadOfflineUser("Sivias", "localhost"); // not on summit's team, on Never's team
		User abe = userService.loadOfflineUser("AbeFrohman", "localhost"); // on summit's team to even things out
		assertTeamsEqual();
		User jjnguy = userService.loadOfflineUser("jjnguy", "localhost"); // A or B, random
		User doji = userService.loadOfflineUser("DojiSuave", "localhost"); // actives even, not on jjnguy's team, evening things again
		assertTeamsEqual();
		User alot = userService.loadOfflineUser("PlayedAlot", "localhost"); // not on Doji's team, actives now even

		assertNotEquals(summit.getTeam(), never.getTeam());
		assertNotEquals(summit.getTeam(), sivias.getTeam());
		assertEquals(never.getTeam(), sivias.getTeam());
		assertEquals(summit.getTeam(), abe.getTeam());
		assertNotEquals(jjnguy.getTeam(), doji.getTeam());
		assertNotEquals(doji.getTeam(), alot.getTeam());
	}

	private void assertTeamsEqual() {
		int redCount = userDataManager.getTeamCount(UserTeam.RED.getId());
		int blueCount = userDataManager.getTeamCount(UserTeam.BLUE.getId());
		assertEquals(redCount, blueCount);
	}

	private void assertTeamsNotEqual() {
		int redCount = userDataManager.getTeamCount(UserTeam.RED.getId());
		int blueCount = userDataManager.getTeamCount(UserTeam.BLUE.getId());
		assertNotEquals(redCount, blueCount);
	}
}
