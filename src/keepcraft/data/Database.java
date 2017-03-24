package keepcraft.data;

import keepcraft.Keepcraft;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {

	private Connection connection = null;
	private final String databasePath;

	public Database(String path) {
		Keepcraft.log("init database " + path);
		databasePath = path;

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

	public static void deleteIfExists(String pathname) {
		try {
			File file = new File(pathname);
			file.delete();
		} catch (Exception e) {
			// don't care
		}
	}
}
