package keepcraft;

import java.util.*;
import java.util.List;

import keepcraft.data.DataCache;
import keepcraft.data.models.FactionSpawn;
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
        setBase(UserFaction.FactionRed, new Location(newWorld, 300, 64, 300));
        setBase(UserFaction.FactionBlue, new Location(newWorld, -300, 64, -300));

        newWorld.save();

        // Unload old world
        server.unloadWorld(currentWorld, true); // save & unload old world

        return newWorld;
    }

    private void setBase(int faction, Location location) {
        Keepcraft.log(String.format("Setting up %s faction...", UserFaction.getName(faction)));
        prepareBaseArea(location, 100);
        plotService.createTeamPlot(null, location, faction, 75);
        DataCache.load(FactionSpawn.class, new FactionSpawn(faction, location));
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
                            if (block.getType().isSolid()) {
                                prepareBlock(block);
                            }
                        }
                    }
                }
            }
        }
    }

    private void prepareBlock(Block block) {
    	// This apparently does not work but the idea was to load the chunks first
//        if(!block.getWorld().isChunkLoaded(block.getX(), block.getZ())) {
//            // Generate chunks before modifying
//            // This makes the modification much more consistent
//            block.getWorld().loadChunk(block.getX(), block.getZ(), true);
//        }

        int y = block.getY();
        Material type = block.getType();

        // Flatten above 75
        if (y > 75) {
            block.setType(Material.AIR);
        }
        // Remove water at 62
        else if (y <= 62 && (type == Material.STATIONARY_WATER || type == Material.WATER)) {
            if (y < 57) {
                block.setType(Material.STONE);
            } else {
                block.setType(Material.DIRT);
            }
        }
    }
}
