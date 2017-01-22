package keepcraft.data.models;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public abstract class ServerConditions {

    private static FileConfiguration config;
    private static Map<Integer, Location> table = new HashMap<>();
    private static int mapRadius = -1;
    private static int minimumDefenderCount = -1;

    public static void init(FileConfiguration config, World world) {
        ServerConditions.config = config;

        loadSpawn(0, world); // center location
        loadSpawn(UserFaction.FactionRed, world);
        loadSpawn(UserFaction.FactionBlue, world);
        loadSpawn(UserFaction.FactionGreen, world);
        loadSpawn(UserFaction.FactionGold, world);
    }

    public static Location getSpawn(int faction) {
        return table.get(faction);
    }

    public static void setSpawn(int faction, Location loc) {
        table.remove(faction);
        table.put(faction, loc);

        if (loc != null) {
            config.set("spawn" + faction, loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
            save();
        }
    }

    private static void loadSpawn(int faction, World world) {
        String spawnString = (String) config.get("spawn" + faction);

        Location spawn = null;
        if (spawnString != null) {
            String[] split = spawnString.split(",");
            spawn = new Location(world, Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
        }

        setSpawn(faction, spawn);
    }

    public static int getMapRadius() {
        if (mapRadius == -1) {
            Integer count = (Integer) config.get("mapRadius");
            if (count == null) {
                setMapRadius(3000);
            } else {
                mapRadius = count;
            }
        }
        return mapRadius;
    }

    public static void setMapRadius(int value) {
        mapRadius = value;
        config.set("mapRadius", mapRadius);
        save();
    }

    public static String getFactionName(int faction) {
        String name = (String) config.get("faction" + faction + "_name");
        if (name == null) {
            switch (faction) {
                case UserFaction.FactionRed:
                    return "Rho";
                case UserFaction.FactionBlue:
                    return "Beta";
                case UserFaction.FactionGreen:
                    return "Gamma";
                case UserFaction.FactionGold:
                    return "Admin";
                default:
                    return "Rogue";
            }
        }
        return name;
    }

    public static void setFactionName(int faction, String name) {
        config.set("faction" + faction + "_name", name);
        save();
    }

    private static void save() {
        try {
            config.save("plugins/Keepcraft/config.yml");
        } catch (IOException e) {
            System.err.println("could not load keepcraft config!");
        }
    }

}
