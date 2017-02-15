package keepcraft.data;

import keepcraft.Keepcraft;
import keepcraft.data.models.Plot;
import keepcraft.data.models.PlotProtection;
import keepcraft.data.models.WorldPoint;
import org.bukkit.Location;

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
			PreparedStatement statement = database.createStatement("CREATE TABLE IF NOT EXISTS plots (LocX, LocY, LocZ, Radius, Name, OrderNumber, SetterId, DateTimeSet)");
			statement.execute();

			statement = database.createStatement("CREATE TABLE IF NOT EXISTS plotProtections (PlotId, Type, ProtectedRadius, KeepRadius, AdminRadius, TriggerRadius, Capturable, CaptureTime, CaptureEffect, SpawnId)");
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
				statement = database.createStatement("UPDATE plotProtections SET Type = ?, ProtectedRadius = ?, KeepRadius = ?, AdminRadius = ?, TriggerRadius = ?, Capturable = ?, CaptureTime = ?, CaptureEffect = ?, SpawnId = ? WHERE PlotId = ?");
				statement.setInt(1, protection.getType());
				statement.setDouble(2, protection.getProtectedRadius());
				statement.setDouble(3, protection.getKeepRadius());
				statement.setDouble(4, protection.getAdminRadius());
				statement.setDouble(5, protection.getTriggerRadius());
				statement.setBoolean(6, protection.getCapturable());
				statement.setInt(7, protection.getCaptureTime());
				statement.setInt(8, 0); // Capture effect
				statement.setInt(9, 0); // spawn
				statement.setInt(10, plot.getId());
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
		Keepcraft.log("Updating plot data cache");

		try {
			PreparedStatement statement = database.createStatement(
					"SELECT plots.ROWID as PlotROWID, LocX, LocY, LocZ, Radius, Name, OrderNumber, SetterId, Type, ProtectedRadius, KeepRadius, AdminRadius, TriggerRadius, Capturable, CaptureTime, CaptureEffect, SpawnId FROM plots JOIN plotProtections ON PlotROWID = PlotId");
			ResultSet result = statement.executeQuery();

			while (result.next()) {
				int id = result.getInt("PlotROWID");
				PlotProtection protection = new PlotProtection(id);
				protection.setType(result.getInt("Type"));
				protection.setProtectedRadius(result.getDouble("ProtectedRadius"));
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
				plot.setSetterId(result.getInt("SetterId"));

				Keepcraft.log(String.format("Plot %s was found at (%s, %s, %s)", plot.getName(), plot.getLocation().getBlockX(), plot.getLocation().getBlockY(), plot.getLocation().getBlockZ()));

				allData.add(plot);
			}

			result.close();
		} catch (Exception e) {
			Keepcraft.error(String.format("Error updating plot data cache: %s", e.getMessage()));
		} finally {
			database.close();
		}

		return allData;
	}

	public Plot createPlot(Location location, String name, double radius) {

		Plot plot = null;

		Keepcraft.log(String.format("Creating record for plot %s", name));
		try {
			PreparedStatement statement
					= database.createStatement("INSERT INTO plots (LocX, LocY, LocZ, Radius, Name, OrderNumber, SetterId, DateTimeSet) VALUES(?, ?, ?, ?, ?, ?, ?, datetime('now'))");
			statement.setInt(1, location.getBlockX());
			statement.setInt(2, location.getBlockY());
			statement.setInt(3, location.getBlockZ());
			statement.setDouble(4, radius);
			statement.setString(5, name);
			statement.setInt(6, -1);
			statement.setInt(7, -1);
			statement.execute();

			statement = database.createStatement("SELECT last_insert_rowid() AS ROWID");
			ResultSet resultSet = statement.executeQuery();
			int id = resultSet.getInt("ROWID");

			plot = new Plot(id);
			plot.setWorldPoint(new WorldPoint(location));
			plot.setName(name);
			plot.setRadius(radius);
			PlotProtection plotProtection = plot.getProtection();

			statement = database.createStatement("INSERT INTO plotProtections (PlotId, Type, ProtectedRadius, KeepRadius, AdminRadius, TriggerRadius, Capturable, CaptureTime, CaptureEffect, SpawnId) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			statement.setInt(1, id);
			statement.setInt(2, plotProtection.getType());
			statement.setDouble(3, plotProtection.getProtectedRadius());
			statement.setDouble(4, plotProtection.getKeepRadius());
			statement.setDouble(5, plotProtection.getAdminRadius());
			statement.setDouble(6, plotProtection.getTriggerRadius());
			statement.setBoolean(7, plotProtection.getCapturable());
			statement.setInt(8, plotProtection.getCaptureTime());
			statement.setInt(9, 0); // Capture effect
			statement.setInt(10, 0); // spawn
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
