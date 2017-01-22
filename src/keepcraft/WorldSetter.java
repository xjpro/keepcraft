package keepcraft;

import java.util.*;

import keepcraft.data.models.ServerConditions;
import keepcraft.data.models.UserFaction;
import keepcraft.services.PlotService;
import org.bukkit.*;
import org.bukkit.block.Block;

public class WorldSetter {

    private PlotService plotService = new PlotService();

    public World reset(World currentWorld) {
        Server server = Bukkit.getServer();

        String currentWorldNameNumber = currentWorld.getName().replace("world", "");
        int currentWorldNumber = currentWorldNameNumber.length() == 0 ? 0 : Integer.parseInt(currentWorldNameNumber);

        // Create new world
        WorldCreator creator = (new WorldCreator("world" + (currentWorldNumber + 1)))
                .seed(new Random().nextInt())
                .type(WorldType.NORMAL)
                .environment(World.Environment.NORMAL);
        World newWorld = server.createWorld(creator);

        // Setup new world with plots
        setBase(new Location(newWorld, 300, 64, 300), UserFaction.FactionRed);
        setBase(new Location(newWorld, -300, 64, -300), UserFaction.FactionBlue);

        newWorld.save();

        // Unload old world
        server.unloadWorld(currentWorld, true); // save & unload old world

        return newWorld;
    }

    private void setBase(Location location, int faction) {
        prepareBaseArea(location, 100);
        plotService.createTeamPlot(null, location, faction, 75);

        // MASSIVE TODO FIX SPAWNS
        ServerConditions.setSpawn(faction, location);
    }

    private void prepareBaseArea(Location center, int radius) {
        World world = center.getWorld();
        int centerX = center.getBlockX();
        int centerZ = center.getBlockZ();
        for (int x = centerX - radius; x <= centerX; x++) {
            for (int z = centerZ - radius; z <= centerZ; z++) {
                if ((x - centerX) * (x - centerX) + (z - centerZ) * (z - centerZ) <= radius * radius) {
                    int otherX = centerX - (x - centerX);
                    int otherZ = centerZ - (z - centerZ);

                    // (x, z), (x, otherZ), (otherX , z), (otherX, otherZ) are in the circle
                    for (int y = 0; y <= 150; y++) {
                        List<Block> blocks = Arrays.asList(
                                world.getBlockAt(x, y, z),
                                world.getBlockAt(x, y, otherZ),
                                world.getBlockAt(otherX, y, z),
                                world.getBlockAt(otherX, y, otherZ)
                        );

                        for (Block block : blocks) {
                            Material type = block.getType();
                            if (type != Material.AIR) {
                                // Flatten above 75
                                if (y > 75 && type.isSolid() && type != Material.WOOD) {
                                    block.setType(Material.AIR);
                                }
                                // Remove water at 64
                                else if (y <= 64 && (type == Material.STATIONARY_WATER || type == Material.WATER)) {
                                    if(y < 58) {
                                        block.setType(Material.STONE);
                                    }
                                    else {
                                        block.setType(Material.DIRT);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
