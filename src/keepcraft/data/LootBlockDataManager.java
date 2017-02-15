package keepcraft.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;

import keepcraft.Keepcraft;
import keepcraft.data.models.LootBlock;
import keepcraft.data.models.WorldPoint;

public class LootBlockDataManager {

	private Database database;

	public LootBlockDataManager(Database database) {
		this.database = database;
		init();
	}

	private void init() {
		try {
			PreparedStatement statement
					= database.createStatement("CREATE TABLE IF NOT EXISTS lootBlocks (LocX, LocY, LocZ, Status, Type, Output)");
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error initializing table: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public void updateData(LootBlock lootBlock) {
		Keepcraft.log("Updating data for lootBlocks");
		try {
			PreparedStatement statement
					= database.createStatement("UPDATE lootBlocks SET Status = ?, Type = ?, Output = ? WHERE LocX = ? AND LocY = ? AND LocZ = ?");
			statement.setInt(1, lootBlock.getStatus());
			statement.setInt(2, lootBlock.getType());
			statement.setDouble(3, lootBlock.getOutputPerHour());
			statement.setInt(4, lootBlock.getWorldPoint().x);
			statement.setInt(5, lootBlock.getWorldPoint().y);
			statement.setInt(6, lootBlock.getWorldPoint().z);
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error setting lootBlocks data: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public Collection<LootBlock> getAllData() {
		ArrayList<LootBlock> allData = new ArrayList<>();

		Keepcraft.log("Beginning lookup of all lootBlocks");

		try {
			PreparedStatement statement = database.createStatement("SELECT LocX, LocY, LocZ, Status, Type, Output FROM lootBlocks");
			ResultSet result = statement.executeQuery();

			while (result.next()) {
				int locX = result.getInt("LocX");
				int locY = result.getInt("LocY");
				int locZ = result.getInt("LocZ");
				int status = result.getInt("Status");
				int type = result.getInt("Type");
				int output = result.getInt("Output");

				LootBlock lootBlock = new LootBlock(new WorldPoint(locX, locY, locZ));
				lootBlock.setStatus(status);
				lootBlock.setType(type);
				lootBlock.setOutputPerHour(output);

				allData.add(lootBlock);
			}

			result.close();
		} catch (Exception e) {
			Keepcraft.error("Error during all lootBlocks data lookup: " + e.getMessage());
		} finally {
			database.close();
		}

		return allData;
	}

	public void putData(LootBlock lootBlock) {

		Keepcraft.log("Creating record for new lootBlocks");
		try {
			PreparedStatement statement
					= database.createStatement("INSERT INTO lootBlocks (LocX, LocY, LocZ, Status, Type, Output) VALUES(?, ?, ?, ?, ?, ?)");
			statement.setInt(1, lootBlock.getWorldPoint().x);
			statement.setInt(2, lootBlock.getWorldPoint().y);
			statement.setInt(3, lootBlock.getWorldPoint().z);
			statement.setInt(4, lootBlock.getStatus());
			statement.setInt(5, lootBlock.getType());
			statement.setInt(6, lootBlock.getOutputPerHour());
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error creating lootBlocks data: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public void deleteData(LootBlock lootBlock) {
		Keepcraft.log("Deleting record for lootBlocks");
		try {
			PreparedStatement statement = database.createStatement("DELETE FROM lootBlocks WHERE LocX = ? AND LocY = ? AND LocZ = ?");
			statement.setInt(1, lootBlock.getWorldPoint().x);
			statement.setInt(2, lootBlock.getWorldPoint().y);
			statement.setInt(3, lootBlock.getWorldPoint().z);
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error deleting lootBlocks data: " + e.getMessage());
		} finally {
			database.close();
		}
	}
}
