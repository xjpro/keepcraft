package keepcraft.data.models;

import org.bukkit.Location;

public class FactionSpawn {

    private int factionValue;
    private WorldPoint worldPoint;

    public FactionSpawn(int factionValue, WorldPoint worldPoint) {
        this.factionValue = factionValue;
        this.worldPoint = worldPoint;
    }

    public FactionSpawn(int factionValue, Location location) {
        this(factionValue, new WorldPoint(location));
    }

    public int getFactionValue() {
        return factionValue;
    }

    public Location getLocation() {
        return worldPoint.asLocation();
    }
}
