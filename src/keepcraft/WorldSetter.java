package keepcraft;

import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

public class WorldSetter {

    public World reset(World currentWorld) {
//- Removes all player data in prep for team reset
//- Reset map with new seed

        // Unload existing world
        String currentWorldNameNumber = currentWorld.getName().replace("world", "");
        int currentWorldNumber = currentWorldNameNumber.equals("") ? 0 : Integer.parseInt(currentWorldNameNumber);
        
        Server server = Bukkit.getServer();
        server.unloadWorld(currentWorld, true); // save & unload old world
        
        // Create new world
        WorldCreator creator = (new WorldCreator("world" + (currentWorldNumber+1)))
                .seed(new Random().nextInt())
                .type(WorldType.NORMAL)
                .environment(World.Environment.NORMAL);
        World newWorld = server.createWorld(creator);

        // Move players over
        List<Player> playersInWorld = currentWorld.getPlayers();
        playersInWorld.stream().forEach((player) -> {
            player.teleport(new Location(newWorld, 0, 64, 0));
        });
        
        // Have to change the server.properties to be the new world... or rethink this and map the new world named "world" always
        newWorld.save();

//- Always places red base at 500,500
//- Always places blue base at -500,-500
//- Completely flattens both base areas, replaces all water spots with dirt  (probably a bit further than the plot size so no easy jump-in areas)
//- Sets team plot area of 75
//- Sets admin plot area of 10 (for spawn)
//- Sets spawn points at center of plot area
//- Opens server

return newWorld;
    }
}
