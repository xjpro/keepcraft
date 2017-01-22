package keepcraft;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import keepcraft.data.models.ServerConditions;
import keepcraft.data.models.UserFaction;
import keepcraft.services.PlotService;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

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
        setBase(new Location(newWorld, 500, 64, 500), UserFaction.FactionRed);
        setBase(new Location(newWorld, -500, 64, -500), UserFaction.FactionBlue);

        newWorld.save();

        // Reset players
        List<Player> playersInWorld = currentWorld.getPlayers();
        playersInWorld.forEach(player -> {
            // Remove all player data
            // Move players over
            player.teleport(new Location(newWorld, 0, 64, 0));
        });

        // Unload old world
        server.unloadWorld(currentWorld, true); // save & unload old world

        return newWorld;
    }

    private void setBase(Location location, int faction) {
        prepareBaseArea(location, 100);
        plotService.createTeamPlot(null, location, faction, 75);
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
                            if (block.getType() != Material.AIR) {
                                // Flatten above 70
                                if (y > 70) {
                                    block.setType(Material.AIR);
                                }
                                // Remove water at 64
                                else if (y <= 64 && block.getType() == Material.STATIONARY_WATER) {
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
