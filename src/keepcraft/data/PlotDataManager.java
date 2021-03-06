package keepcraft.data;

import keepcraft.Keepcraft;
import keepcraft.data.models.Plot;
import keepcraft.data.models.PlotProtection;
import keepcraft.data.models.WorldPoint;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;

public class PlotDataManager {

	private Database database;

	public PlotDataManager(Database database) {
		this.database = database;
		init();
	}

	private void init() {
		try {
			PreparedStatement statement = database.createStatement("CREATE TABLE IF NOT EXISTS plots (LocX, LocY, LocZ, Radius, Name, OrderNumber, CreatorName, DateTimeSet)");
			statement.execute();

			statement = database.createStatement("CREATE TABLE IF NOT EXISTS plotProtections (PlotId, Type, KeepRadius, AdminRadius, TriggerRadius, Capturable, CaptureTime, CaptureEffect, SpawnId)");
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error(String.format("Error initializing tables: %s", e.getMessage()));
		} finally {
			database.close();
		}
	}

	public void updatePlot(Plot plot) {
		Keepcraft.log(String.format("Updating record for plot %s", plot.getName()));
		try {
			PreparedStatement statement = database.createStatement("UPDATE plots SET LocX = ?, LocY = ?, LocZ = ?, Radius = ?, Name = ?, OrderNumber = ? WHERE ROWID = ?");
			statement.setInt(1, plot.getWorldPoint().x);
			statement.setInt(2, plot.getWorldPoint().y);
			statement.setInt(3, plot.getWorldPoint().z);
			statement.setDouble(4, plot.getRadius());
			statement.setString(5, plot.getName());
			statement.setInt(6, plot.getOrderNumber());
			statement.setInt(7, plot.getId());
			int rowsAffected = statement.executeUpdate();

			if (rowsAffected > 0) {
				PlotProtection protection = plot.getProtection();
				statement = database.createStatement("UPDATE plotProtections SET Type = ?, KeepRadius = ?, AdminRadius = ?, TriggerRadius = ?, Capturable = ?, CaptureTime = ?, CaptureEffect = ?, SpawnId = ? WHERE PlotId = ?");
				statement.setInt(1, protection.getType());
				statement.setDouble(2, protection.getKeepRadius());
				statement.setDouble(3, protection.getAdminRadius());
				statement.setDouble(4, protection.getTriggerRadius());
				statement.setBoolean(5, protection.getCapturable());
				statement.setInt(6, protection.getCaptureTime());
				statement.setInt(7, 0); // Capture effect
				statement.setInt(8, 0); // spawn
				statement.setInt(9, plot.getId());
				statement.executeUpdate();
			}
		} catch (Exception e) {
			Keepcraft.error(String.format("Error updating plot record: %s", e.getMessage()));
		} finally {
			database.close();
		}
	}

	public Collection<Plot> getAllPlots() {
		ArrayList<Plot> allData = new ArrayList<>();
		Keepcraft.log("Retrieving all plots");

		try {
			PreparedStatement statement = database.createStatement(
					"SELECT plots.ROWID as PlotROWID, LocX, LocY, LocZ, Radius, Name, OrderNumber, CreatorName, Type, KeepRadius, AdminRadius, TriggerRadius, Capturable, CaptureTime, CaptureEffect, SpawnId FROM plots JOIN plotProtections ON PlotROWID = PlotId");
			ResultSet result = statement.executeQuery();

			while (result.next()) {
				int id = result.getInt("PlotROWID");
				PlotProtection protection = new PlotProtection(id);
				protection.setType(result.getInt("Type"));
				protection.setKeepRadius(result.getDouble("KeepRadius"));
				protection.setAdminRadius(result.getDouble("AdminRadius"));
				protection.setTriggerRadius(result.getDouble("TriggerRadius"));
				protection.setCapturable(result.getBoolean("Capturable"));
				protection.setCaptureTime(result.getInt("CaptureTime"));

				Plot plot = new Plot(id, protection);
				plot.setWorldPoint(new WorldPoint(result.getInt("LocX"), result.getInt("LocY"), result.getInt("LocZ")));
				plot.setRadius(result.getFloat("Radius"));
				plot.setName(result.getString("Name"));
				plot.setOrderNumber(result.getInt("OrderNumber"));
				plot.setCreatorName(result.getString("CreatorName"));

				Keepcraft.log(String.format("Plot %s was found at (%s, %s, %s)", plot.getName(), plot.getWorldPoint().x, plot.getWorldPoint().y, plot.getWorldPoint().z));

				allData.add(plot);
			}

			result.close();
		} catch (Exception e) {
			Keepcraft.error(String.format("Error retrieving all plots: %s", e.getMessage()));
		} finally {
			database.close();
		}

		return allData;
	}

	public Plot createPlot(WorldPoint worldPoint, String name, double radius) {
		return createPlot(worldPoint, name, radius, null);
	}

	public Plot createPlot(WorldPoint worldPoint, String name, double radius, String creatorName) {

		Plot plot = null;

		Keepcraft.log(String.format("Creating record for plot %s", name));
		try {
			PreparedStatement statement
					= database.createStatement("INSERT INTO plots (LocX, LocY, LocZ, Radius, Name, OrderNumber, CreatorName, DateTimeSet) VALUES(?, ?, ?, ?, ?, ?, ?, datetime('now'))");
			statement.setInt(1, worldPoint.x);
			statement.setInt(2, worldPoint.y);
			statement.setInt(3, worldPoint.z);
			statement.setDouble(4, radius);
			statement.setString(5, name);
			statement.setInt(6, -1);
			statement.setString(7, creatorName);
			statement.execute();

			statement = database.createStatement("SELECT last_insert_rowid() AS ROWID");
			ResultSet resultSet = statement.executeQuery();
			int id = resultSet.getInt("ROWID");

			PlotProtection plotProtection = new PlotProtection(id);
			plotProtection.setType(PlotProtection.PUBLIC);
			plotProtection.setKeepRadius(0);
			plotProtection.setAdminRadius(Plot.DEFAULT_RADIUS);
			plotProtection.setTriggerRadius(Plot.DEFAULT_TRIGGER_RADIUS);
			plotProtection.setCapturable(false);
			plotProtection.setCaptureTime(0);

			plot = new Plot(id, plotProtection);
			plot.setWorldPoint(worldPoint);
			plot.setName(name);
			plot.setRadius(radius);
			plot.setCreatorName(creatorName);

			statement = database.createStatement("INSERT INTO plotProtections (PlotId, Type, KeepRadius, AdminRadius, TriggerRadius, Capturable, CaptureTime, CaptureEffect, SpawnId) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");
			statement.setInt(1, id);
			statement.setInt(2, plotProtection.getType());
			statement.setDouble(3, plotProtection.getKeepRadius());
			statement.setDouble(4, plotProtection.getAdminRadius());
			statement.setDouble(5, plotProtection.getTriggerRadius());
			statement.setBoolean(6, plotProtection.getCapturable());
			statement.setInt(7, plotProtection.getCaptureTime());
			statement.setInt(8, 0); // Capture effect
			statement.setInt(9, 0); // spawn
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error(String.format("Error creating plot record: %s", e.getMessage()));
		} finally {
			database.close();
		}

		return plot;
	}

	public void deletePlot(Plot plot) {
		Keepcraft.log(String.format("Deleting record for plot %s", plot.getName()));
		try {
			PreparedStatement statement = database.createStatement("DELETE FROM plots WHERE ROWID = ?");
			statement.setInt(1, plot.getId());
			statement.execute();

			statement = database.createStatement("DELETE FROM plotProtections WHERE PlotId = ?");
			statement.setInt(1, plot.getId());
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error(String.format("Error deleting plot record: %s", e.getMessage()));
		} finally {
			database.close();
		}
	}
}
