package keepcraft.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;

import keepcraft.Keepcraft;
import keepcraft.data.models.LootBlock;

public class LootBlockDataManager {

    private Database database;

    public LootBlockDataManager(Database database) {
        this.database = database;
        init();
    }

    private void init() {
        try {
            PreparedStatement statement
                    = database.createStatement("CREATE TABLE IF NOT EXISTS lootBlocks (LocX, LocY, LocZ, Status, Type, Output)");
            statement.execute();
        } catch (Exception e) {
            Keepcraft.error("Error initializing table: " + e.getMessage());
        } finally {
            database.close();
        }
    }

    public void updateData(LootBlock block) {
        Keepcraft.log("Updating data for lootBlocks");
        try {
            PreparedStatement statement
                    = database.createStatement("UPDATE lootBlocks SET LocX = ?, LocY = ?, LocZ = ?, Status = ?, Type = ?, Output = ? WHERE ROWID = ?");
            statement.setInt(1, block.getLocation().getBlockX());
            statement.setInt(2, block.getLocation().getBlockY());
            statement.setInt(3, block.getLocation().getBlockZ());
            statement.setInt(4, block.getStatus());
            statement.setInt(5, block.getType());
            statement.setDouble(6, block.getOutputPerHour());
            statement.setInt(7, block.getId());
            statement.execute();
        } catch (Exception e) {
            Keepcraft.error("Error setting lootBlocks data: " + e.getMessage());
        } finally {
            database.close();
        }
    }

    public Collection<LootBlock> getAllData() {
        ArrayList<LootBlock> allData = new ArrayList<>();

        Keepcraft.log("Beginning lookup of all lootBlocks");

        try {
            PreparedStatement statement = database.createStatement("SELECT ROWID, LocX, LocY, LocZ, Status, Type, Output FROM lootBlocks");
            ResultSet result = statement.executeQuery();
            
            while (result.next()) {
                int id = result.getInt("ROWID");
                int locX = result.getInt("LocX");
                int locY = result.getInt("LocY");
                int locZ = result.getInt("LocZ");
                int status = result.getInt("Status");
                int type = result.getInt("Type");
                int output = result.getInt("Output");

                LootBlock block = new LootBlock(id, Keepcraft.getWorld().getBlockAt(locX, locY, locZ));
                block.setStatus(status);
                block.setType(type);
                block.setOutputPerHour(output);

                allData.add(block);
            }

            result.close();
        } catch (Exception e) {
            Keepcraft.error("Error during all lootBlocks data lookup: " + e.getMessage());
        } finally {
            database.close();
        }

        return allData;
    }

    public void putData(LootBlock block) {

        Keepcraft.log("Creating record for new lootBlocks");
        try {
            PreparedStatement statement
                    = database.createStatement("INSERT INTO lootBlocks (LocX, LocY, LocZ, Status, Type, Output) VALUES(?, ?, ?, ?, ?, ?)");
            statement.setInt(1, block.getLocation().getBlockX());
            statement.setInt(2, block.getLocation().getBlockY());
            statement.setInt(3, block.getLocation().getBlockZ());
            statement.setInt(4, block.getStatus());
            statement.setInt(5, block.getType());
            statement.setDouble(6, block.getOutputPerHour());
            statement.execute();
        } catch (Exception e) {
            Keepcraft.error("Error creating lootBlocks data: " + e.getMessage());
        } finally {
            database.close();
        }
    }

    public void deleteData(LootBlock block) {
        Keepcraft.log("Deleting record for lootBlocks");
        try {
            PreparedStatement statement = database.createStatement("DELETE FROM lootBlocks WHERE ROWID = ?");
            statement.setInt(1, block.getId());
            statement.execute();
        } catch (Exception e) {
            Keepcraft.error("Error deleting lootBlocks data: " + e.getMessage());
        } finally {
            database.close();
        }
    }
}
