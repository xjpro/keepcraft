package keepcraft.data;

import java.util.logging.Level;

import keepcraft.data.models.WorldPoint;
import keepcraft.data.models.Plot;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import keepcraft.Keepcraft;
import keepcraft.data.models.PlotProtection;

public class PlotDataManager extends DataManager<Plot> {

	public PlotDataManager(Database database) {
		super(database);
		init();
	}

	private void init() {
		try {
			PreparedStatement statement = database.createStatement("CREATE TABLE IF NOT EXISTS plots (LocX, LocY, LocZ, Radius, Name, OrderNumber, SetterId, DateTimeSet)");
			statement.execute();

			statement = database.createStatement("CREATE TABLE IF NOT EXISTS plotProtections (PlotId, Type, ProtectedRadius, PartialRadius, AdminRadius, TriggerRadius, Capturable, CaptureTime, CaptureEffect, SpawnId)");
			statement.execute();
		} catch (Exception e) {
			logger.log(Level.INFO, String.format("(KC) Error initializing tables: %s", e.getMessage()));
		} finally {
			database.close();
		}
	}

	@Override
	public void updateData(Plot plot) {
		logger.log(Level.INFO, String.format("(KC) Updating record for plot %s", plot.getName()));
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
				statement = database.createStatement("UPDATE plotProtections SET Type = ?, ProtectedRadius = ?, PartialRadius = ?, AdminRadius = ?, TriggerRadius = ?, Capturable = ?, CaptureTime = ?, CaptureEffect = ?, SpawnId = ? WHERE PlotId = ?");
				statement.setInt(1, protection.getType());
				statement.setDouble(2, protection.getProtectedRadius());
				statement.setDouble(3, protection.getPartialRadius());
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
			logger.log(Level.INFO, String.format("(KC) Error updating plot record: %s", e.getMessage()));
		} finally {
			database.close();
		}
	}

	@Override
	public Plot getData(Object key) {
		// This should probably not be called so
		throw new UnsupportedOperationException("Don't call this, get the data from the data cache");
	}

	@Override
	public Map<Object, Plot> getAllData() {
		Map<Object, Plot> allData = new HashMap<Object, Plot>();
		logger.info("(KC) Updating plot data cache");

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

				logger.log(Level.INFO, String.format("(KC) Plot %s was found at (%s, %s, %s)", new Object[]{name, locX, locY, locZ}));

				Plot plot = new Plot();
				plot.setWorldPoint(new WorldPoint(locX, locY, locZ));
				plot.setId(id);
				plot.setRadius(radius);
				plot.setName(name);
				plot.setOrderNumber(orderNumber);
				plot.setSetterId(setterId);

				allData.put(plot.getId(), plot);
			}

			result.close();

			// Now get protections, if it exists, which it really should
			statement = database.createStatement("SELECT PlotId, Type, ProtectedRadius, PartialRadius, AdminRadius, TriggerRadius, Capturable, CaptureTime, CaptureEffect, SpawnId FROM plotProtections");
			result = statement.executeQuery();

			while (result.next()) {
				int plotId = result.getInt("PlotId");
				int type = result.getInt("Type");
				double protectedRadius = result.getDouble("ProtectedRadius");
				double partialRadius = result.getDouble("PartialRadius");
				double adminRadius = result.getDouble("AdminRadius");
				double triggerRadius = result.getDouble("TriggerRadius");
				boolean capturable = result.getBoolean("Capturable");
				int captureSeconds = result.getInt("CaptureTime");
				int captureEffect = result.getInt("CaptureEffect");
				int spawnId = result.getInt("SpawnId");

				PlotProtection protection = new PlotProtection(plotId);
				protection.setType(type);
				protection.setProtectedRadius(protectedRadius);
				protection.setPartialRadius(partialRadius);
				protection.setAdminRadius(adminRadius);
				protection.setTriggerRadius(triggerRadius);
				protection.setCapturable(capturable);
				protection.setCaptureTime(captureSeconds);
				//protection.set capture effect
				// spawn

				Plot plot = allData.get(plotId);
				if (plot != null) {
					plot.setProtection(protection);
				}

			}
			result.close();
		} catch (Exception e) {
			logger.log(Level.INFO, String.format("(KC) Error updating plot data cache: %s", e.getMessage()));
		} finally {
			database.close();
		}

		return allData;
	}

	@Override
	public void putData(Plot plot) {

		logger.log(Level.INFO, String.format("(KC) Creating record for plot %s", plot.getName()));
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
			statement = database.createStatement("INSERT INTO plotProtections (PlotId, Type, ProtectedRadius, PartialRadius, AdminRadius, TriggerRadius, Capturable, CaptureTime, CaptureEffect, SpawnId) VALUES(last_insert_rowid(), ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			statement.setInt(1, protection.getType());
			statement.setDouble(2, protection.getProtectedRadius());
			statement.setDouble(3, protection.getPartialRadius());
			statement.setDouble(4, protection.getAdminRadius());
			statement.setDouble(5, protection.getTriggerRadius());
			statement.setBoolean(6, protection.getCapturable());
			statement.setInt(7, protection.getCaptureTime());
			statement.setInt(8, 0); // Capture effect
			statement.setInt(9, 0); // spawn
			statement.execute();
		} catch (Exception e) {
			logger.log(Level.INFO, String.format("(KC) Error creating plot record: %s", e.getMessage()));
		} finally {
			database.close();
		}
	}

	@Override
	public void deleteData(Plot plot) {
		logger.log(Level.INFO, String.format("(KC) Deleting record for plot %s", plot.getName()));
		try {
			PreparedStatement statement = database.createStatement("DELETE FROM plots WHERE ROWID = ?");
			statement.setInt(1, plot.getId());
			statement.execute();

			statement = database.createStatement("DELETE FROM plotProtections WHERE PlotId = ?");
			statement.setInt(1, plot.getId());
			statement.execute();
		} catch (Exception e) {
			logger.log(Level.INFO, String.format("(KC) Error deleting plot record: %s", e.getMessage()));
		} finally {
			database.close();
		}
	}

	@Override
	public void truncate() {
		Keepcraft.log("Truncating plots table");
		try {
			PreparedStatement statement = database.createStatement("DELETE FROM plots");
			statement.execute();
		} catch (Exception e) {
			logger.log(Level.INFO, String.format("(KC) Error truncating plot: %s", e.getMessage()));
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
