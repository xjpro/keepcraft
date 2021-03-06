package keepcraft.data;

import keepcraft.Keepcraft;
import keepcraft.data.models.UserStats;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserStatsDataManager {

	private Database database;

	public UserStatsDataManager(Database database) {
		this.database = database;
		init();
	}

	private void init() {
		try {
			PreparedStatement statement = database.createStatement("CREATE TABLE IF NOT EXISTS userStats " +
					"(RecordStart, UserName, WorldSeed, WorldGUID, PlaySeconds, BlocksPlaced, BlocksRemoved, BlocksAddedToChests, " +
					"BlocksRemovedFromChests, BlocksAddedToTeamChests, BlocksRemovedFromTeamChests, " +
					"PlayerKills, PlayerDeaths, AttackingKills, DefendingKills, AttackingDeaths, DefendingDeaths, " +
					"ArrowShots, ArrowHits, SwordHits, AxeHits, OtherHits, ArrowBlocks, MeleeBlocks, ArrowStrikes, " +
					"CriticalHits, IronMined, GoldMined, DiamondMined)");
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error initializing table: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public void saveData(String userName, UUID worldGUID, UserStats stats) {
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
					"OtherHits = OtherHits + ?, " +
					"ArrowBlocks = ArrowBlocks + ?, " +
					"MeleeBlocks = MeleeBlocks + ?, " +
					"ArrowStrikes = ArrowStrikes + ?, " +
					"CriticalHits = CriticalHits + ?, " +
					"IronMined = IronMined + ?, " +
					"GoldMined = GoldMined + ?, " +
					"DiamondMined = DiamondMined + ? " +
					"WHERE UserName = ? AND WorldGUID = ?");
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
			statement.setInt(19, stats.arrowBlocks);
			statement.setInt(20, stats.meleeBlocks);
			statement.setInt(21, stats.arrowStrikes);
			statement.setInt(22, stats.criticalHits);
			statement.setInt(23, stats.ironMined);
			statement.setInt(24, stats.goldMined);
			statement.setInt(25, stats.diamondMined);

			statement.setString(26, userName);
			statement.setString(27, worldGUID.toString());
			int rowsAffected = statement.executeUpdate();

			if (rowsAffected == 0) {
				statement = database.createStatement("INSERT INTO userStats " +
						"(PlaySeconds, BlocksPlaced, BlocksRemoved, BlocksAddedToChests, " +
						"BlocksRemovedFromChests, BlocksAddedToTeamChests, BlocksRemovedFromTeamChests, " +
						"PlayerKills, PlayerDeaths, AttackingKills, DefendingKills, AttackingDeaths, DefendingDeaths, " +
						"ArrowShots, ArrowHits, SwordHits, AxeHits, OtherHits, ArrowBlocks, MeleeBlocks, ArrowStrikes, " +
						"CriticalHits, IronMined, GoldMined, DiamondMined, UserName, WorldGUID, RecordStart) " +
						"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))");
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
				statement.setInt(19, stats.arrowBlocks);
				statement.setInt(20, stats.meleeBlocks);
				statement.setInt(21, stats.arrowStrikes);
				statement.setInt(22, stats.criticalHits);
				statement.setInt(23, stats.ironMined);
				statement.setInt(24, stats.goldMined);
				statement.setInt(25, stats.diamondMined);
				statement.setString(26, userName);
				statement.setString(27, worldGUID.toString());
				statement.execute();
			}
		} catch (Exception e) {
			Keepcraft.error("Error saving stats data: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public List<String> getRecentlyPlayedUserNamesByPlayTime() {

		List<String> previouslyPlayedUserNames = new ArrayList<>();

		// Gather all users from the past map
		try {
			PreparedStatement statement = database.createStatement("SELECT WorldGUID FROM userStats " +
					"WHERE WorldGUID IS NOT NULL AND ((julianday(datetime('now')) - julianday(RecordStart)) < ?) " +
					"GROUP BY WorldGUID " +
					"ORDER BY RecordStart DESC");
			// Fake UUID if one not provided, used for testing
			statement.setFloat(1, 5.0f);
			ResultSet result = statement.executeQuery();

			ArrayList<String> recentWorldGUIDs = new ArrayList<>();
			while (result.next()) {
				recentWorldGUIDs.add(result.getString("WorldGUID"));
			}

			if (recentWorldGUIDs.size() > 0) {
				// We have world or more worlds in the past week

				StringBuilder whereClause = new StringBuilder();
				for (int i = 0; i < recentWorldGUIDs.size(); i++) {
					whereClause.append(String.format("WorldGUID = \"%s\"", recentWorldGUIDs.get(i)));
					if (i != recentWorldGUIDs.size() - 1) {
						whereClause.append(" OR ");
					}
				}

				statement = database.createStatement("SELECT UserName, SUM(PlaySeconds) AS TotalPlayed FROM userStats " +
						"WHERE " + whereClause + " " +
						"GROUP BY UserName " +
						"ORDER BY TotalPlayed DESC");
				result = statement.executeQuery();

				while (result.next()) {
					previouslyPlayedUserNames.add(result.getString("UserName"));
				}
			}

		} catch (Exception e) {
			Keepcraft.error("Error looking up recent activity: " + e.getMessage());
		} finally {
			database.close();
		}

		return previouslyPlayedUserNames;
	}

	public long getSecondsPlayedOnServer(String userName) {
		long secondsPlayed = 0;
		Keepcraft.log(String.format("Checking for time spent on server for '%s'", userName));
		try {
			PreparedStatement statement
					= database.createStatement("SELECT SUM(PlaySeconds) AS TotalPlayed FROM userStats WHERE UserName = ?");
			statement.setString(1, userName);
			ResultSet result = statement.executeQuery();

			boolean found = result.next();

			if (found) {
				secondsPlayed = result.getLong("TotalPlayed");
			}

			result.close();
		} catch (Exception e) {
			Keepcraft.error("Error during time spent on server lookup: " + e.getMessage());
		} finally {
			database.close();
		}

		return secondsPlayed;
	}
}
