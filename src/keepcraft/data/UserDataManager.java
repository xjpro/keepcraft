package keepcraft.data;

import keepcraft.Keepcraft;
import keepcraft.data.models.User;
import keepcraft.data.models.UserFaction;
import keepcraft.data.models.UserPrivilege;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDataManager {

	private Database database;

	public UserDataManager(Database database) {
		this.database = database;
		init();
	}

	private void init() {
		try {
			PreparedStatement statement
					= database.createStatement("CREATE TABLE IF NOT EXISTS users (Name, Privilege, Faction, Money, LastPlotId, FirstOnline, LastOnline)");
			statement.execute();
		} catch (Exception e) {
			Keepcraft.log("Error initializing table: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public void updateData(User user) {
		Keepcraft.log("Updating data for " + user.getName());
		try {
			PreparedStatement statement
					= database.createStatement("UPDATE users SET Privilege = ?, Faction = ?, Money = ?, LastPlotId = ?, LastOnline = datetime('now') WHERE Name = ?");
			statement.setInt(1, user.getPrivilege());
			statement.setInt(2, user.getFaction());
			statement.setInt(3, user.getMoney());
			statement.setInt(4, user.getLoggedOffFriendlyPlotId());
			statement.setString(5, user.getName());
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error setting user data: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public User getData(String name) {

		Keepcraft.log("Beginning lookup on " + name);
		User user = null;

		try {
			PreparedStatement statement = database.createStatement("SELECT Privilege, Faction, Money, LastPlotId FROM users WHERE Name = ? LIMIT 1");
			statement.setString(1, name);
			ResultSet result = statement.executeQuery();

			boolean found = result.next();

			if (!found) {
				Keepcraft.log("No user was found for name " + name);
			} else {
				user = new User(name);
				user.setPrivilege(result.getInt("Privilege"));
				user.setFaction(result.getInt("Faction"));
				user.setMoney(result.getInt("Money"));
				user.setLoggedOffFriendlyPlotId(result.getInt("LastPlotId"));
				Keepcraft.log("User data was retrieved with values: " + user);
			}

			result.close();
		} catch (Exception e) {
			Keepcraft.error("Error during user data lookup: " + e.getMessage());
		} finally {
			database.close();
		}

		return user;
	}

//	public Collection<User> getAllData() {
//		ArrayList<User> allData = new ArrayList<>();
//
//		Keepcraft.log("Beginning lookup of all user data");
//
//		try {
//			PreparedStatement statement
//					= database.createStatement("SELECT ROWID, Name, Privilege, Faction, Money FROM users");
//			ResultSet result = statement.executeQuery();
//
//			while (result.next()) {
//				int id = result.getInt("ROWID");
//				String name = result.getString("Name");
//				int privilege = result.getInt("Privilege");
//				int faction = result.getInt("Faction");
//				int money = result.getInt("Money");
//				int lastPlotId = result.getInt("LastPlotId");
//
//				User user = new User(id);
//				user.setName(name);
//				user.setPrivilege(privilege);
//				user.setFaction(faction);
//				user.setMoney(money);
//				user.setLoggedOffFriendlyPlotId(lastPlotId);
//
//				allData.add(user);
//			}
//
//			result.close();
//		} catch (Exception e) {
//			Keepcraft.error("Error during all user data lookup: " + e.getMessage());
//		} finally {
//			database.close();
//		}
//
//		return allData;
//	}

	public void putData(User user) {

		Keepcraft.log("Creating record for " + user.getName());
		try {
			PreparedStatement statement
					= database.createStatement("INSERT INTO users (Name, Privilege, Faction, Money, LastPlotId, FirstOnline, LastOnline) VALUES(?, ?, ?, ?, ?, datetime('now'), datetime('now'))");
			statement.setString(1, user.getName());
			statement.setInt(2, user.getPrivilege());
			statement.setInt(3, user.getFaction());
			statement.setInt(4, user.getMoney());
			statement.setInt(5, user.getLoggedOffFriendlyPlotId());
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error creating user data: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public void deleteData(User user) {
		Keepcraft.log("Deleting record for " + user.getName());
		try {
			PreparedStatement statement
					= database.createStatement("DELETE FROM users WHERE Name = ?");
			statement.setString(1, user.getName());
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error deleting user data: " + e.getMessage());
		} finally {
			database.close();
		}
	}

//	public void resetNonAdminUserData() {
//		Keepcraft.log("Reset non-admin user records");
//		try {
//			PreparedStatement statement
//					= database.createStatement("UPDATE users SET Privilege = ?, Money = 0, LastPlotId = NULL WHERE Privilege < ?");
//			statement.setInt(1, UserPrivilege.INIT);
//			statement.setInt(2, UserPrivilege.ADMIN);
//			statement.execute();
//		} catch (Exception e) {
//			Keepcraft.log("Error resetting non-admin user data: " + e.getMessage());
//		} finally {
//			database.close();
//		}
//	}

//	public void truncate() {
//		Keepcraft.log("Truncating users table");
//		try {
//			PreparedStatement statement = database.createStatement("DELETE FROM users");
//			statement.execute();
//		} catch (Exception e) {
//			Keepcraft.log("(KC) Error truncating users: " + e.getMessage());
//		} finally {
//			database.close();
//		}
//	}

	public boolean exists(Object key) {
		String name;
		if (key instanceof String) {
			name = (String) key;
		} else {
			return false;
		}

		boolean found = false;
		Keepcraft.log("Checking for existence of " + name);
		try {
			PreparedStatement statement
					= database.createStatement("SELECT ROWID FROM users WHERE Name = ? LIMIT 1");
			statement.setString(1, name);
			ResultSet result = statement.executeQuery();

			found = result.next();

			result.close();
		} catch (Exception e) {
			Keepcraft.error("Error during user data lookup: " + e.getMessage());
		} finally {
			database.close();
		}

		return found;
	}

	public int getFactionCount(int faction) {
		int memberCount = 0;
		try {
			PreparedStatement statement = database.createStatement(
					"SELECT ROWID FROM users WHERE Faction = ? AND Privilege != ? AND "
							+ "((julianday(datetime('now')) - julianday(LastOnline)) < ?)"
			);
			statement.setInt(1, faction);
			statement.setInt(2, UserPrivilege.ADMIN);
			statement.setFloat(3, 3.0f);
			ResultSet result = statement.executeQuery();

			while (result.next()) {
				memberCount++;
			}

			result.close();
		} catch (Exception e) {
			Keepcraft.error("Error counting faction members: " + e.getMessage());
		} finally {
			database.close();
		}

		Keepcraft.log("Active member count for " + UserFaction.asString(faction) + " is " + memberCount);

		return memberCount;
	}
}
