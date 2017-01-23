package keepcraft.data;

import keepcraft.Keepcraft;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class Database {

    private static Logger logger = Logger.getLogger("Minecraft");
    private Connection connection = null;
    private String databasePath;

    public Database(String path) {
        databasePath = path;

        Keepcraft.log("init database");
        try {
            // It's a SQLite database
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            logger.info("Error while initializing database: " + e.getMessage());
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

    public boolean databaseExists() {
        try {
            connect();
        } catch (SQLException e) {
            logger.info("Error while initializing database: " + e.getMessage());
            return false;
        }
        return true;
    }

    public PreparedStatement createStatement(String sql) throws SQLException {
        connect();
        return connection.prepareStatement(sql);
    }

    public void close() {
        try {
            if (isConnected()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.info("Error while closing database: " + e.getMessage());
        }
    }
}
