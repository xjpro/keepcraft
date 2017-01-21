package keepcraft;

import java.util.List;
import java.util.Random;

import keepcraft.data.models.UserFaction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

public class WorldSetter {

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


        // Flatten things out
        // Replace water with dirt
        // Set team plot of 75
        // Set admin plot
        // Set spawn

    }

    private void flattenArea(Location center, int radius) {
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        for (int x = centerX - radius; x <= centerX; x++) {
            for (int y = centerY - radius; y <= centerY; y++) {
                if ((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY) <= radius * radius) {
                    int otherX = centerX - (x - centerX);
                    int otherY = centerY - (y - centerY);
                    // (x, y), (x, otherY), (otherX , y), (otherX, otherY) are in the circle
                }
            }
        }
    }
}
