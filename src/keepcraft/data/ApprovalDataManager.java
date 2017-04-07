package keepcraft.data;

import keepcraft.Keepcraft;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ApprovalDataManager {

	private Database database;

	public ApprovalDataManager(Database database) {
		this.database = database;
		init();
	}

	private void init() {
		try {
			PreparedStatement statement = database.createStatement("CREATE TABLE IF NOT EXISTS userApprovals " +
					"(DateTime, UserName, ApprovedBy)");
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error initializing table: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public void saveApproval(String userName, String approvedUserName) {
		Keepcraft.log("Saving approval for " + userName);
		try {
			PreparedStatement statement = database.createStatement("INSERT INTO userApprovals " +
					"(DateTime, UserName, ApprovedBy) VALUES(datetime('now'), ?, ?)");
			statement.setString(1, userName);
			statement.setString(2, approvedUserName);
			statement.execute();
		} catch (Exception e) {
			Keepcraft.error("Error saving approval: " + e.getMessage());
		} finally {
			database.close();
		}
	}

	public boolean isApproved(String userName) {
		boolean found = false;
		Keepcraft.log("Checking for approval of " + userName);
		try {
			PreparedStatement statement
					= database.createStatement("SELECT ROWID FROM userApprovals WHERE UserName = ? LIMIT 1");
			statement.setString(1, userName);
			ResultSet result = statement.executeQuery();

			found = result.next();

			result.close();
		} catch (Exception e) {
			Keepcraft.error("Error during approval data lookup: " + e.getMessage());
		} finally {
			database.close();
		}

		return found;
	}
}
