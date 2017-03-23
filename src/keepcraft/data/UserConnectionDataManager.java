package keepcraft.data;

import keepcraft.Keepcraft;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserConnectionDataManager {

	private Database database;

	public UserConnectionDataManager(Database database) {
		this.database = database;
		init();
	}

	private void init() {
		try {
			PreparedStatement statement = database.createStatement("CREATE TABLE IF NOT EXISTS userConnections " +
					"(UserName, IP)");
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error initializing table: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public void saveIP(String userName, String ip) {
		try {
			PreparedStatement statement = database.createStatement("SELECT ROWID FROM userConnections WHERE UserName = ? AND IP = ?");
			statement.setString(1, userName);
			statement.setString(2, ip);
			ResultSet result = statement.executeQuery();

			boolean found = result.next();
			if (!found) {
				statement = database.createStatement("INSERT INTO userConnections (UserName, IP) VALUES(?, ?)");
				statement.setString(1, userName);
				statement.setString(2, ip);
				statement.execute();
			}
		} catch (Exception e) {
			Keepcraft.error("Error saving connections data: " + e.getMessage());
		} finally {
			database.close();
		}
	}
}
