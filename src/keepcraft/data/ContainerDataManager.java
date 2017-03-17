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
					= database.createStatement("CREATE TABLE IF NOT EXISTS containers (LocX, LocY, LocZ, Permission, OutputType, Output)");
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error initializing table: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public void updateData(Container container) {
		Keepcraft.log("Updating data for containers");
		try {
			PreparedStatement statement
					= database.createStatement("UPDATE containers SET Permission = ?, OutputType = ?, Output = ? WHERE LocX = ? AND LocY = ? AND LocZ = ?");
			statement.setInt(1, container.getPermission().getId());
			statement.setInt(2, container.getOutputType().getId());
			statement.setDouble(3, container.getOutputPerHour());
			statement.setInt(4, container.getWorldPoint().x);
			statement.setInt(5, container.getWorldPoint().y);
			statement.setInt(6, container.getWorldPoint().z);
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error setting containers data: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public Collection<Container> getAllData() {
		ArrayList<Container> allData = new ArrayList<>();

		Keepcraft.log("Beginning lookup of all containers");

		try {
			PreparedStatement statement = database.createStatement("SELECT LocX, LocY, LocZ, Permission, OutputType, Output FROM containers");
			ResultSet result = statement.executeQuery();

			while (result.next()) {
				int locX = result.getInt("LocX");
				int locY = result.getInt("LocY");
				int locZ = result.getInt("LocZ");
				int permissionId = result.getInt("Permission");
				int outputTypeId = result.getInt("OutputType");
				int output = result.getInt("Output");

				Container container = new Container(new WorldPoint(locX, locY, locZ));
				container.setPermission(Container.ContainerPermission.getContainerPermission(permissionId));
				container.setOutputType(Container.ContainerOutputType.getContainerOutputType(outputTypeId));
				container.setOutputPerHour(output);

				allData.add(container);
			}

			result.close();
		} catch (Exception e) {
			Keepcraft.error("Error during all containers data lookup: " + e.getMessage());
		} finally {
			database.close();
		}

		return allData;
	}

	public void putData(Container container) {

		Keepcraft.log("Creating record for new containers");
		try {
			PreparedStatement statement
					= database.createStatement("INSERT INTO containers (LocX, LocY, LocZ, Permission, OutputType, Output) VALUES(?, ?, ?, ?, ?, ?)");
			statement.setInt(1, container.getWorldPoint().x);
			statement.setInt(2, container.getWorldPoint().y);
			statement.setInt(3, container.getWorldPoint().z);
			statement.setInt(4, container.getPermission().getId());
			statement.setInt(5, container.getOutputType().getId());
			statement.setInt(6, container.getOutputPerHour());
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error creating containers data: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public void deleteData(Container container) {
		Keepcraft.log("Deleting record for containers");
		try {
			PreparedStatement statement = database.createStatement("DELETE FROM containers WHERE LocX = ? AND LocY = ? AND LocZ = ?");
			statement.setInt(1, container.getWorldPoint().x);
			statement.setInt(2, container.getWorldPoint().y);
			statement.setInt(3, container.getWorldPoint().z);
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error deleting containers data: " + e.getMessage());
		} finally {
			database.close();
		}
	}
}
