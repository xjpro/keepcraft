package keepcraft.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;

import keepcraft.Keepcraft;
import keepcraft.data.models.Container;
import keepcraft.data.models.WorldPoint;

public class ContainerDataManager {

	private Database database;

	public ContainerDataManager(Database database) {
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

	public void updateData(Container container) {
		Keepcraft.log("Updating data for lootBlocks");
		try {
			PreparedStatement statement
					= database.createStatement("UPDATE lootBlocks SET Status = ?, Type = ?, Output = ? WHERE LocX = ? AND LocY = ? AND LocZ = ?");
			statement.setInt(1, container.getStatus());
			statement.setInt(2, container.getType().getId());
			statement.setDouble(3, container.getOutputPerHour());
			statement.setInt(4, container.getWorldPoint().x);
			statement.setInt(5, container.getWorldPoint().y);
			statement.setInt(6, container.getWorldPoint().z);
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error setting lootBlocks data: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public Collection<Container> getAllData() {
		ArrayList<Container> allData = new ArrayList<>();

		Keepcraft.log("Beginning lookup of all lootBlocks");

		try {
			PreparedStatement statement = database.createStatement("SELECT LocX, LocY, LocZ, Status, Type, Output FROM lootBlocks");
			ResultSet result = statement.executeQuery();

			while (result.next()) {
				int locX = result.getInt("LocX");
				int locY = result.getInt("LocY");
				int locZ = result.getInt("LocZ");
				int status = result.getInt("Status");
				int typeId = result.getInt("Type");
				int output = result.getInt("Output");

				Container container = new Container(new WorldPoint(locX, locY, locZ));
				container.setStatus(status);
				container.setType(Container.ContainerType.getContainerType(typeId));
				container.setOutputPerHour(output);

				allData.add(container);
			}

			result.close();
		} catch (Exception e) {
			Keepcraft.error("Error during all lootBlocks data lookup: " + e.getMessage());
		} finally {
			database.close();
		}

		return allData;
	}

	public void putData(Container container) {

		Keepcraft.log("Creating record for new lootBlocks");
		try {
			PreparedStatement statement
					= database.createStatement("INSERT INTO lootBlocks (LocX, LocY, LocZ, Status, Type, Output) VALUES(?, ?, ?, ?, ?, ?)");
			statement.setInt(1, container.getWorldPoint().x);
			statement.setInt(2, container.getWorldPoint().y);
			statement.setInt(3, container.getWorldPoint().z);
			statement.setInt(4, container.getStatus());
			statement.setInt(5, container.getType().getId());
			statement.setInt(6, container.getOutputPerHour());
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error creating lootBlocks data: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public void deleteData(Container container) {
		Keepcraft.log("Deleting record for lootBlocks");
		try {
			PreparedStatement statement = database.createStatement("DELETE FROM lootBlocks WHERE LocX = ? AND LocY = ? AND LocZ = ?");
			statement.setInt(1, container.getWorldPoint().x);
			statement.setInt(2, container.getWorldPoint().y);
			statement.setInt(3, container.getWorldPoint().z);
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error deleting lootBlocks data: " + e.getMessage());
		} finally {
			database.close();
		}
	}
}
