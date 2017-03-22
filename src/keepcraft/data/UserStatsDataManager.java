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
					"ArrowShots, ArrowHits, SwordHits, AxeHits, OtherHits)");
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
					"OtherHits = OtherHits + ? " +
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
			statement.setString(19, userName);
			statement.setString(20, worldGUID.toString());
			int rowsAffected = statement.executeUpdate();

			if (rowsAffected == 0) {
				statement = database.createStatement("INSERT INTO userStats " +
						"(PlaySeconds, BlocksPlaced, BlocksRemoved, BlocksAddedToChests, " +
						"BlocksRemovedFromChests, BlocksAddedToTeamChests, BlocksRemovedFromTeamChests, " +
						"PlayerKills, PlayerDeaths, AttackingKills, DefendingKills, AttackingDeaths, DefendingDeaths, " +
						"ArrowShots, ArrowHits, SwordHits, AxeHits, OtherHits, UserName, WorldGUID, RecordStart) " +
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
				statement.setString(20, worldGUID.toString());
				statement.execute();
			}
		} catch (Exception e) {
			Keepcraft.error("Error saving stats data: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public List<String> getRecentlyPlayedUserNamesByPlayTime(UUID currentWorldGUID) {

		List<String> previouslyPlayedUserNames = new ArrayList<>();

		// Gather all users from the past map
		try {
			PreparedStatement statement = database.createStatement("SELECT WorldGUID FROM userStats " +
					"WHERE WorldGUID IS NOT NULL AND WorldGUID != ? " +
					"GROUP BY WorldGUID " +
					"ORDER BY RecordStart DESC LIMIT 1");
			// Fake UUID if one not provided, used for testing
			statement.setString(1, currentWorldGUID != null ? currentWorldGUID.toString() : "2b79c281-7287-4627-96fc-788a03901345");
			ResultSet result = statement.executeQuery();

			ArrayList<String> recentWorldGUIDs = new ArrayList<>();
			while (result.next()) {
				recentWorldGUIDs.add(result.getString("WorldGUID"));
			}

			if (recentWorldGUIDs.size() > 2) {
				// We have enough data to make this determination
				statement = database.createStatement("SELECT UserName, SUM(PlaySeconds) AS TotalPlayed FROM userStats " +
						"WHERE WorldGUID = ? OR WorldGUID = ? OR WorldGUID = ? " +
						"GROUP BY UserName " +
						"ORDER BY TotalPlayed DESC");
				statement.setString(1, recentWorldGUIDs.get(0));
				statement.setString(2, recentWorldGUIDs.get(1));
				statement.setString(3, recentWorldGUIDs.get(2));
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
}
