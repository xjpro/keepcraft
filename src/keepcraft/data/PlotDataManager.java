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

	public void updateData(Plot plot) {
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

			if (rowsAffected == 0) {
				// a new plot, we'll have to create its record
				putData(plot);
			} else {
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

	public Collection<Plot> getAllData() {
		ArrayList<Plot> allData = new ArrayList<>();
		Keepcraft.log("Updating plot data cache");

		try {
			PreparedStatement statement = database.createStatement("SELECT ROWID, LocX, LocY, LocZ, Radius, Name, OrderNumber, SetterId FROM plots");
			ResultSet result = statement.executeQuery();

			while (result.next()) {
				int id = result.getInt("ROWID");
				int locX = result.getInt("LocX");
				int locY = result.getInt("LocY");
				int locZ = result.getInt("LocZ");
				float radius = result.getFloat("Radius");
				String name = result.getString("Name");
				int orderNumber = result.getInt("OrderNumber");
				int setterId = result.getInt("SetterId");

				Keepcraft.log(String.format("Plot %s was found at (%s, %s, %s)", name, locX, locY, locZ));

				Plot plot = new Plot();
				plot.setWorldPoint(new WorldPoint(locX, locY, locZ));
				plot.setId(id);
				plot.setRadius(radius);
				plot.setName(name);
				plot.setOrderNumber(orderNumber);
				plot.setSetterId(setterId);

				allData.add(plot);
			}

			result.close();

			// Now get protections, if it exists, which it really should
			statement = database.createStatement("SELECT PlotId, Type, ProtectedRadius, KeepRadius, AdminRadius, TriggerRadius, Capturable, CaptureTime, CaptureEffect, SpawnId FROM plotProtections");
			result = statement.executeQuery();

			while (result.next()) {
				int plotId = result.getInt("PlotId");
				int type = result.getInt("Type");
				double protectedRadius = result.getDouble("ProtectedRadius");
				double keepRadius = result.getDouble("KeepRadius");
				double adminRadius = result.getDouble("AdminRadius");
				double triggerRadius = result.getDouble("TriggerRadius");
				boolean capturable = result.getBoolean("Capturable");
				int captureSeconds = result.getInt("CaptureTime");
				//int captureEffect = result.getInt("CaptureEffect");
				//int spawnId = result.getInt("SpawnId");

				PlotProtection protection = new PlotProtection(plotId);
				protection.setType(type);
				protection.setProtectedRadius(protectedRadius);
				protection.setKeepRadius(keepRadius);
				protection.setAdminRadius(adminRadius);
				protection.setTriggerRadius(triggerRadius);
				protection.setCapturable(capturable);
				protection.setCaptureTime(captureSeconds);
				//protection.set capture effect
				// spawn

				for(Plot plot : allData) {
					if (plot.getId() == protection.getPlotId()) {
						plot.setProtection(protection);
						break;
					}
				}
			}
			result.close();
		} catch (Exception e) {
			Keepcraft.error(String.format("Error updating plot data cache: %s", e.getMessage()));
		} finally {
			database.close();
		}

		return allData;
	}

	public void putData(Plot plot) {

		Keepcraft.log(String.format("Creating record for plot %s", plot.getName()));
		try {
			PreparedStatement statement
					= database.createStatement("INSERT INTO plots (LocX, LocY, LocZ, Radius, Name, OrderNumber, SetterId, DateTimeSet) VALUES(?, ?, ?, ?, ?, ?, ?, datetime('now'))");
			statement.setInt(1, plot.getWorldPoint().x);
			statement.setInt(2, plot.getWorldPoint().y);
			statement.setInt(3, plot.getWorldPoint().z);
			statement.setDouble(4, plot.getRadius());
			statement.setString(5, plot.getName());
			statement.setInt(6, plot.getOrderNumber());
			statement.setInt(7, plot.getSetterId());
			statement.execute();

			PlotProtection protection = plot.getProtection();
			statement = database.createStatement("INSERT INTO plotProtections (PlotId, Type, ProtectedRadius, KeepRadius, AdminRadius, TriggerRadius, Capturable, CaptureTime, CaptureEffect, SpawnId) VALUES(last_insert_rowid(), ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			statement.setInt(1, protection.getType());
			statement.setDouble(2, protection.getProtectedRadius());
			statement.setDouble(3, protection.getKeepRadius());
			statement.setDouble(4, protection.getAdminRadius());
			statement.setDouble(5, protection.getTriggerRadius());
			statement.setBoolean(6, protection.getCapturable());
			statement.setInt(7, protection.getCaptureTime());
			statement.setInt(8, 0); // Capture effect
			statement.setInt(9, 0); // spawn
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error(String.format("Error creating plot record: %s", e.getMessage()));
		} finally {
			database.close();
		}
	}

	public void deleteData(Plot plot) {
		Keepcraft.log(String.format("Deleting record for plot %s", plot.getName()));
		try {
			PreparedStatement statement = database.createStatement("DELETE FROM plots WHERE ROWID = ?");
			statement.setInt(1, plot.getId());
			statement.execute();

			statement = database.createStatement("DELETE FROM plotProtections WHERE PlotId = ?");
			statement.setInt(1, plot.getId());
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error(String.format("(KC) Error deleting plot record: %s", e.getMessage()));
		} finally {
			database.close();
		}
	}

//	public void truncate() {
//		Keepcraft.log("Truncating plots & plotProtections tables");
//		try {
//			PreparedStatement statement = database.createStatement("DELETE FROM plots");
//			statement.execute();
//
//			statement = database.createStatement("DELETE FROM plotProtections");
//			statement.execute();
//		} catch (Exception e) {
//			Keepcraft.error(String.format("Error truncating plot: %s", e.getMessage()));
//		} finally {
//			database.close();
//		}
//	}

}
