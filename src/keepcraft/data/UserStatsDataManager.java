package keepcraft.data;

import keepcraft.Keepcraft;
import keepcraft.data.models.UserStats;

import java.sql.PreparedStatement;

public class UserStatsDataManager {

	private Database database;

	public UserStatsDataManager(Database database) {
		this.database = database;
		init();
	}

	private void init() {
		try {
			PreparedStatement statement = database.createStatement("CREATE TABLE IF NOT EXISTS userStats " +
					"(RecordStart, UserName, WorldSeed, PlaySeconds, BlocksPlaced, BlocksRemoved, BlocksAddedToChests, " +
					"BlocksRemovedFromChests, BlocksAddedToTeamChests, BlocksRemovedFromTeamChests, " +
					"PlayerKills, PlayerDeaths, AttackingKills, DefendingKills, AttackingDeaths, DefendingDeaths, " +
					"ArrowShots, ArrowHits, SwordHits, AxeHits, OtherHits)");
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error initializing table: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public void saveData(String userName, long worldSeed, UserStats stats) {
		Keepcraft.log("Updating stats for " + userName);
		try {
			PreparedStatement statement = database.createStatement("UPDATE userStats SET " +
					"PlaySeconds = PlaySeconds + ?, " +
					"BlocksPlaced = BlocksPlaced + ?, " +
					"BlocksRemoved = BlocksRemoved + ?, " +
					"BlocksAddedToChests = BlocksAddedToChests + ?, " +
					"BlocksRemovedFromChests = BlocksRemovedFromChests + ?, " +
					"BlocksAddedToTeamChests = BlocksAddedToTeamChests + ?, " +
					"BlocksRemovedFromTeamChests = BlocksRemovedFromTeamChests + ?, " +
					"PlayerKills = PlayerKills + ?, " +
					"PlayerDeaths = PlayerDeaths + ?, " +
					"AttackingKills = AttackingKills + ?, " +
					"DefendingKills = DefendingKills + ?, " +
					"AttackingDeaths = AttackingDeaths + ?, " +
					"DefendingDeaths = DefendingDeaths + ?, " +
					"ArrowShots = ArrowShots + ?, " +
					"ArrowHits = ArrowHits + ?, " +
					"SwordHits = SwordHits + ?, " +
					"AxeHits = AxeHits + ?, " +
					"OtherHits = OtherHits + ? " +
					"WHERE UserName = ? AND WorldSeed = ?");
			statement.setInt(1, (int) stats.playSeconds);
			statement.setInt(2, stats.blocksPlaced);
			statement.setInt(3, stats.blocksRemoved);
			statement.setInt(4, stats.blocksAddedToChests);
			statement.setInt(5, stats.blocksRemovedFromChests);
			statement.setInt(6, stats.blocksAddedToTeamChests);
			statement.setInt(7, stats.blocksRemovedFromTeamChests);
			statement.setInt(8, stats.playerKills);
			statement.setInt(9, stats.playerDeaths);
			statement.setInt(10, stats.attackingKills);
			statement.setInt(11, stats.defendingKills);
			statement.setInt(12, stats.attackingDeaths);
			statement.setInt(13, stats.defendingDeaths);
			statement.setInt(14, stats.arrowShots);
			statement.setInt(15, stats.arrowHits);
			statement.setInt(16, stats.swordHits);
			statement.setInt(17, stats.axeHits);
			statement.setInt(18, stats.otherHits);
			statement.setString(19, userName);
			statement.setLong(20, worldSeed);
			int rowsAffected = statement.executeUpdate();

			if (rowsAffected == 0) {
				statement = database.createStatement("INSERT INTO userStats " +
						"(PlaySeconds, BlocksPlaced, BlocksRemoved, BlocksAddedToChests, " +
						"BlocksRemovedFromChests, BlocksAddedToTeamChests, BlocksRemovedFromTeamChests, " +
						"PlayerKills, PlayerDeaths, AttackingKills, DefendingKills, AttackingDeaths, DefendingDeaths, " +
						"ArrowShots, ArrowHits, SwordHits, AxeHits, OtherHits, UserName, WorldSeed, RecordStart) " +
						"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))");
				statement.setInt(1, (int) stats.playSeconds);
				statement.setInt(2, stats.blocksPlaced);
				statement.setInt(3, stats.blocksRemoved);
				statement.setInt(4, stats.blocksAddedToChests);
				statement.setInt(5, stats.blocksRemovedFromChests);
				statement.setInt(6, stats.blocksAddedToTeamChests);
				statement.setInt(7, stats.blocksRemovedFromTeamChests);
				statement.setInt(8, stats.playerKills);
				statement.setInt(9, stats.playerDeaths);
				statement.setInt(10, stats.attackingKills);
				statement.setInt(11, stats.defendingKills);
				statement.setInt(12, stats.attackingDeaths);
				statement.setInt(13, stats.defendingDeaths);
				statement.setInt(14, stats.arrowShots);
				statement.setInt(15, stats.arrowHits);
				statement.setInt(16, stats.swordHits);
				statement.setInt(17, stats.axeHits);
				statement.setInt(18, stats.otherHits);
				statement.setString(19, userName);
				statement.setLong(20, worldSeed);
				statement.execute();
			}
		} catch (Exception e) {
			Keepcraft.error("Error saving stats data: " + e.getMessage());
		} finally {
			database.close();
		}
	}
}
