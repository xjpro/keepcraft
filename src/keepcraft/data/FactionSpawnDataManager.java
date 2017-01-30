package keepcraft.data;

import keepcraft.Keepcraft;
import keepcraft.data.models.FactionSpawn;
import org.bukkit.Location;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FactionSpawnDataManager {

	private Database database;

	public FactionSpawnDataManager(Database database) {
		this.database = database;
		init();
	}

	private void init() {
		try {
			PreparedStatement statement = database.createStatement("CREATE TABLE IF NOT EXISTS factionSpawns (FactionValue, LocX, LocY, LocZ)");
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error initializing factions table: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public void updateData(FactionSpawn spawn) {
		//Keepcraft.log("Updating record for " + faction.toString());
		try {
			PreparedStatement statement = database.createStatement("UPDATE factionSpawns SET LocX = ?, LocY = ?, LocZ = ? WHERE FactionValue = ?");
			statement.setInt(1, spawn.getLocation().getBlockX());
			statement.setInt(2, spawn.getLocation().getBlockY());
			statement.setInt(3, spawn.getLocation().getBlockZ());
			statement.setInt(4, spawn.getFactionValue());
			int rowsAffected = statement.executeUpdate();

			if (rowsAffected == 0) {
				// a we'll have to create record
				putData(spawn);
			}
		} catch (Exception e) {
			Keepcraft.error("Error updating factionSpawn record: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public Collection<FactionSpawn> getAllData() {
		List<FactionSpawn> allData = new ArrayList<>();
		Keepcraft.error("Updating factionSpawn data cache");

		try {
			PreparedStatement statement = database.createStatement("SELECT FactionValue, LocX, LocY, LocZ FROM factionSpawns");
			ResultSet result = statement.executeQuery();

			while (result.next()) {
				int factionValue = result.getInt("FactionValue");
				int locX = result.getInt("LocX");
				int locY = result.getInt("LocY");
				int locZ = result.getInt("LocZ");

				Keepcraft.log(String.format("FactionSpawn for %s was found at at (%s, %s, %s)", factionValue, locX, locY, locZ));

				FactionSpawn spawn = new FactionSpawn(factionValue, new Location(Keepcraft.getWorld(), locX, locY, locZ));
				allData.add(spawn);
			}

			result.close();
		} catch (Exception e) {
			Keepcraft.error("Error updating factionSpawns data cache: " + e.getMessage());
		} finally {
			database.close();
		}

		return allData;
	}

	public void putData(FactionSpawn spawn) {

		Keepcraft.log(String.format("Creating record for factionSpawn %s", spawn.getFactionValue()));
		try {
			PreparedStatement statement
					= database.createStatement("INSERT INTO factionSpawns (FactionValue, LocX, LocY, LocZ) VALUES(?, ?, ?, ?)");
			statement.setInt(1, spawn.getFactionValue());
			statement.setInt(2, spawn.getLocation().getBlockX());
			statement.setInt(3, spawn.getLocation().getBlockY());
			statement.setInt(4, spawn.getLocation().getBlockZ());
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error creating factionSpawn record: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public void deleteData(FactionSpawn spawn) {
		//logger.log(Level.INFO, String.format("(KC) Deleting record for plot %s", plot.getName()));
		try {
			PreparedStatement statement = database.createStatement("DELETE FROM factionSpawns WHERE FactionValue = ?");
			statement.setInt(1, spawn.getFactionValue());
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error deleting plot record: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public void truncate() {
		Keepcraft.log("Truncating factionSpawns table");
		try {
			PreparedStatement statement = database.createStatement("DELETE FROM factionSpawns");
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error(String.format("Error truncating factionSpawns: %s", e.getMessage()));
		} finally {
			database.close();
		}
	}
}
