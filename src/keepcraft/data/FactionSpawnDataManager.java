package keepcraft.data;

import keepcraft.Keepcraft;
import keepcraft.data.models.*;
import org.bukkit.Location;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class FactionSpawnDataManager extends DataManager<FactionSpawn> {

	public FactionSpawnDataManager(Database database) {
		super(database);
		init();
	}

	public FactionSpawnDataManager() {
		super(Keepcraft.getSqlLiteDatabase());
		init();
	}

	private void init() {
		try {
			PreparedStatement statement = database.createStatement("CREATE TABLE IF NOT EXISTS factionSpawns (FactionValue, LocX, LocY, LocZ)");
			statement.execute();
		} catch (Exception e) {
			Keepcraft.log("Error initializing factions table: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	@Override
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
			Keepcraft.log("Error updating factionSpawn record: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	@Override
	public FactionSpawn getData(Object key) {
		// This should probably not be called so
		throw new UnsupportedOperationException("Don't call this, get the data from the data cache");
	}

	@Override
	public Map<Object, FactionSpawn> getAllData() {
		Map<Object, FactionSpawn> allData = new HashMap<>();
		Keepcraft.log("Updating factionSpawn data cache");

		try {
			PreparedStatement statement = database.createStatement("SELECT FactionValue, LocX, LocY, LocZ FROM factionSpawns");
			ResultSet result = statement.executeQuery();

			while (result.next()) {
				int factionValue = result.getInt("FactionValue");
				int locX = result.getInt("LocX");
				int locY = result.getInt("LocY");
				int locZ = result.getInt("LocZ");

				Keepcraft.log(String.format("FactionSpawn for %s was found at at (%s, %s, %s)", new Object[]{factionValue, locX, locY, locZ}));

				FactionSpawn spawn = new FactionSpawn(factionValue, new Location(Keepcraft.getWorld(), locX, locY, locZ));
				allData.put(factionValue, spawn);
			}

			result.close();
		} catch (Exception e) {
			Keepcraft.log("Error updating factionSpawns data cache: " + e.getMessage());
		} finally {
			database.close();
		}

		return allData;
	}

	@Override
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
			Keepcraft.log("Error creating factionSpawn record: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	@Override
	public void deleteData(FactionSpawn spawn) {
		//logger.log(Level.INFO, String.format("(KC) Deleting record for plot %s", plot.getName()));
		try {
			PreparedStatement statement = database.createStatement("DELETE FROM factionSpawns WHERE FactionValue = ?");
			statement.setInt(1, spawn.getFactionValue());
			statement.execute();
		} catch (Exception e) {
			Keepcraft.log("Error deleting plot record: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	@Override
	public void truncate() {
		Keepcraft.log("Truncating factionSpawns table");
		try {
			PreparedStatement statement = database.createStatement("DELETE FROM factionSpawns");
			statement.execute();
		} catch (Exception e) {
			logger.log(Level.INFO, String.format("(KC) Error truncating factionSpawns: %s", e.getMessage()));
		} finally {
			database.close();
		}
	}

	@Override
	public boolean exists(Object key) {
		// TODO Auto-generated method stub
		return false;
	}

}
