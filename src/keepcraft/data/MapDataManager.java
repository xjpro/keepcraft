package keepcraft.data;

import keepcraft.Keepcraft;
import keepcraft.data.models.WorldPoint;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.UUID;

public class MapDataManager {

	private Database database;

	public MapDataManager(Database database) {
		this.database = database;
		init();
	}

	private void init() {
		try {
			PreparedStatement statement = database.createStatement("CREATE TABLE IF NOT EXISTS map " +
					"(WorldGUID, StartDateTime, CenterPosX, CenterPosZ)");
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error initializing table: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public long getMapAgeInSeconds() {
		long ageInSeconds = 0;
		try {
			PreparedStatement statement = database.createStatement("SELECT StartDateTime FROM map LIMIT 1");
			ResultSet result = statement.executeQuery();

			boolean found = result.next();

			if (!found) {
				Keepcraft.error("No map was found in data");
			} else {
				Date mapStart = result.getDate("StartDateTime");
				ageInSeconds = ((new Date()).getTime() - mapStart.getTime()) / 1000;
			}

			result.close();
		} catch (Exception e) {
			Keepcraft.error("Error during map data lookup: " + e.getMessage());
		} finally {
			database.close();
		}

		return ageInSeconds;
	}

	public WorldPoint getMapCenter() {
		WorldPoint center = null;
		try {
			PreparedStatement statement = database.createStatement("SELECT CenterPosX, CenterPosZ FROM map LIMIT 1");
			ResultSet result = statement.executeQuery();

			boolean found = result.next();

			if (!found) {
				Keepcraft.error("No map was found in data");
			} else {
				int posX = result.getInt("CenterPosX");
				int posZ = result.getInt("CenterPosZ");
				center = new WorldPoint(posX, 64, posZ);
			}

			result.close();
		} catch (Exception e) {
			Keepcraft.error("Error during map center lookup: " + e.getMessage());
		} finally {
			database.close();
		}

		return center;
	}

	public void createWorldRecord(UUID worldGUID, WorldPoint center) {
		try {
			PreparedStatement statement = database.createStatement("INSERT INTO map " +
					"(WorldGUID, StartDateTime, CenterPosX, CenterPosZ) " +
					"VALUES(?, datetime('now'), ?, ?)");
			statement.setString(1, worldGUID.toString());
			statement.setInt(2, center.x);
			statement.setInt(3, center.z);
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error creating map data: " + e.getMessage());
		} finally {
			database.close();
		}
	}
}
