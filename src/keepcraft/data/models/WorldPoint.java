package keepcraft.data.models;

import keepcraft.Keepcraft;
import org.bukkit.Location;

public class WorldPoint {

	public final int x;
	public final int y;
	public final int z;

	public WorldPoint(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public WorldPoint(Location location) {
		this(location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	public Location asLocation() {
		return new Location(Keepcraft.getWorld(), (double) x, (double) y, (double) z);
	}

	public double distance(WorldPoint pt) {
		return Math.sqrt(Math.pow(x - pt.x, 2) + Math.pow(y - pt.y, 2) + Math.pow(z - pt.z, 2));
	}

	public double distance(Location pt) {
		return Math.sqrt(Math.pow(x - pt.getX(), 2) + Math.pow(y - pt.getY(), 2) + Math.pow(z - pt.getZ(), 2));
	}

	public double distanceIgnoreY(double locX, double locZ) {
		return Math.sqrt(Math.pow(x - locX, 2) + Math.pow(z - locZ, 2));
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other instanceof WorldPoint) {
			WorldPoint point = (WorldPoint) other;
			return x == point.x && y == point.y && z == point.z;
		}
		if (other instanceof Location) {
			Location point = (Location) other;
			return x == point.getBlockX() && y == point.getBlockY() && z == point.getBlockZ();
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 29 * hash + this.x;
		hash = 29 * hash + this.y;
		hash = 29 * hash + this.z;
		return hash;
	}

}
