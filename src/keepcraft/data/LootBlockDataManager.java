package keepcraft.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import keepcraft.Keepcraft;
import keepcraft.data.models.LootBlock;

public class LootBlockDataManager extends DataManager<LootBlock> {

    public LootBlockDataManager(Database database) {
        super(database);
        init();
    }

    private void init() {
        try {
            PreparedStatement statement
                    = database.createStatement("CREATE TABLE IF NOT EXISTS lootBlocks (LocX, LocY, LocZ, Status, Type, Output)");
            statement.execute();
        } catch (Exception e) {
            Keepcraft.log("Error initializing table: " + e.getMessage());
        } finally {
            database.close();
        }
    }

    @Override
    public void updateData(LootBlock block) {
        Keepcraft.log("Updating data for loot block");
        try {
            PreparedStatement statement
                    = database.createStatement("UPDATE lootBlocks SET LocX = ?, LocY = ?, LocZ = ?, Status = ?, Type = ?, Output = ? WHERE ROWID = ?");
            statement.setInt(1, block.getLocation().getBlockX());
            statement.setInt(2, block.getLocation().getBlockY());
            statement.setInt(3, block.getLocation().getBlockZ());
            statement.setInt(4, block.getStatus());
            statement.setInt(5, block.getType());
            statement.setDouble(6, block.getOutput());
            statement.setInt(7, block.getId());
            statement.execute();

            // TODO: if the record did not exist, we'll have to create it
        } catch (Exception e) {
            Keepcraft.log("Error setting loot block data: " + e.getMessage());
        } finally {
            database.close();
        }
    }

    @Override
    public LootBlock getData(Object key) {
        // This should probably not be called so
        throw new UnsupportedOperationException("Don't call this, get the data from the data cache");
    }

    @Override
    public Map<Object, LootBlock> getAllData() {
        Map<Object, LootBlock> allData = new HashMap<Object, LootBlock>();

        Keepcraft.log("Beginning lookup of all loot blocks");

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

                LootBlock block = new LootBlock(id, Bukkit.getWorld("world").getBlockAt(locX, locY, locZ));
                block.setStatus(status);
                block.setType(type);
                block.setOutput(output);

                allData.put(id, block);
            }

            result.close();
        } catch (Exception e) {
            Keepcraft.log("Error during all loot block data lookup: " + e.getMessage());
        } finally {
            database.close();
        }

        return allData;
    }

    @Override
    public void putData(LootBlock block) {

        Keepcraft.log("Creating record for new loot block");
        try {
            PreparedStatement statement
                    = database.createStatement("INSERT INTO lootBlocks (LocX, LocY, LocZ, Status, Type, Output) VALUES(?, ?, ?, ?, ?, ?)");
            statement.setInt(1, block.getLocation().getBlockX());
            statement.setInt(2, block.getLocation().getBlockY());
            statement.setInt(3, block.getLocation().getBlockZ());
            statement.setInt(4, block.getStatus());
            statement.setInt(5, block.getType());
            statement.setDouble(6, block.getOutput());
            statement.execute();
        } catch (Exception e) {
            Keepcraft.log("Error creating loot block data: " + e.getMessage());
        } finally {
            database.close();
        }
    }

    @Override
    public void deleteData(LootBlock block) {
        Keepcraft.log("Deleting record for loot block");
        try {
            PreparedStatement statement = database.createStatement("DELETE FROM lootBlocks WHERE ROWID = ?");
            statement.setInt(1, block.getId());
            statement.execute();
        } catch (Exception e) {
            Keepcraft.log("Error deleting user data: " + e.getMessage());
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
