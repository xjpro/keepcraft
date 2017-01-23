package keepcraft.data.models;

import org.bukkit.Location;

public class FactionSpawn {
    private int factionValue;
    private Location location;

    public FactionSpawn(int factionValue, Location location) {
        this.factionValue = factionValue;
        this.location = location;
    }
    public int getFactionValue() {
        return factionValue;
    }
    public Location getLocation() {
        return location;
    }
}
