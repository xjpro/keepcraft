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
    private final int TEAM_PLOT_RADIUS = 75;

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
        prepareBaseArea(location, TEAM_PLOT_RADIUS + 25);

        // Find good spawn location
        Location goodSpawnLocation = location.clone();
        goodSpawnLocation.setY(76);
        while(!goodSpawnLocation.getWorld().getBlockAt(goodSpawnLocation.getBlockX(), goodSpawnLocation.getBlockY(), goodSpawnLocation.getBlockZ()).getType().isSolid()) {
            goodSpawnLocation.add(0, -1, 0);
        }
		goodSpawnLocation.add(0, 1, 0); // Go one up so we're in air

        plotService.createTeamPlot(null, goodSpawnLocation, faction, TEAM_PLOT_RADIUS);

        // Center faction spawn on block so it's not buried
		FactionSpawn spawn = new FactionSpawn(faction, goodSpawnLocation.clone().add(0.5, 0, 0.5));
        DataCache.load(FactionSpawn.class, spawn);
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
                            prepareBlock(block);
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
        // Remove water at 64
        else if (y <= 64 && (type == Material.STATIONARY_WATER || type == Material.WATER)) {
            if (y < 58) {
                block.setType(Material.STONE);
            } else {
                block.setType(Material.GRASS);
            }
        }
    }
}
