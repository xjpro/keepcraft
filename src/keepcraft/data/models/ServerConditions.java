package keepcraft.data.models;

import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;

public abstract class ServerConditions {

    private static FileConfiguration config;
    private static int mapRadius = -1;

    public static void init(FileConfiguration config) {
        ServerConditions.config = config;
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
