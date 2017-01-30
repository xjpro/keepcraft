package keepcraft.data;

import keepcraft.Keepcraft;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {

	private Connection connection = null;
	private final String databasePath;

	public Database(String path) {
		databasePath = path;

		Keepcraft.log("init database");
		try {
			// It's a SQLite database
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			Keepcraft.error("Error while initializing database: " + e.getMessage());
		}
	}

	private boolean isConnected() {
		if (connection == null) {
			return false;
		}

		try {
			return !connection.isClosed();
		} catch (SQLException e) {
			return false;
		}
	}

	private void connect() throws SQLException {
		if (!isConnected()) {
			connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
		}
	}

	PreparedStatement createStatement(String sql) throws SQLException {
		connect();
		return connection.prepareStatement(sql);
	}

	void close() {
		try {
			if (isConnected()) {
				connection.close();
			}
		} catch (SQLException e) {
			Keepcraft.error("Error while closing database: " + e.getMessage());
		}
	}
}
