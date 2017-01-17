package keepcraft.data.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class WorldPoint {

    private final String worldName;
    public final int x;
    public final int y;
    public final int z;

    public WorldPoint(Location location) {
        worldName = location.getWorld().getName();
        x = location.getBlockX();
        y = location.getBlockY();
        z = location.getBlockZ();
    }

    public WorldPoint(String worldName, int x, int y, int z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Location asBukkitLocation() {
        return new Location(Bukkit.getWorld(worldName), (double) x, (double) y, (double) z);
    }

    public String getWorldName() {
        return worldName;
    }

    public double distance(WorldPoint pt) {
        return Math.sqrt(Math.pow(x - pt.x, 2) + Math.pow(y - pt.y, 2) + Math.pow(z - pt.z, 2));
    }

    public double distance(Location pt) {
        return Math.sqrt(Math.pow(x - pt.getX(), 2) + Math.pow(y - pt.getY(), 2) + Math.pow(z - pt.getZ(), 2));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof WorldPoint) {
            WorldPoint point = (WorldPoint) other;
            if (worldName.equals(point.getWorldName()) && x == point.x && y == point.y && z == point.z) {
                return true;
            }
            return false;
        }
        if (other instanceof Location) {
            Location point = (Location) other;
            if (worldName.equals(point.getWorld().getName())
                    && x == point.getBlockX() && y == point.getBlockY() && z == point.getBlockZ()) {
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.worldName != null ? this.worldName.hashCode() : 0);
        hash = 29 * hash + this.x;
        hash = 29 * hash + this.y;
        hash = 29 * hash + this.z;
        return hash;
    }

}
