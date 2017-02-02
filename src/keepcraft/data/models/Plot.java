package keepcraft.data.models;

import keepcraft.services.ChatService;
import org.bukkit.Location;
import keepcraft.tasks.Siege;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Persistent data for a plot.
 */
public class Plot {

	public final static int DEFAULT_RADIUS = 10;
	public final static int DEFAULT_TRIGGER_RADIUS = 10;

	private WorldPoint worldPoint;
	private int id;
	private float radius;
	private String name;
	private PlotProtection protection;
	private int orderNumber = -1;
	private int setterId;

	//private User setter;
	//private Timestamp timestampSet;
	private Siege activeSiege = null;

	public Location getLocation() {
		return worldPoint.asLocation();
	}

	public WorldPoint getWorldPoint() {
		return worldPoint;
	}

	public void setWorldPoint(WorldPoint value) {
		worldPoint = value;
	}

	private double distance(Location loc) {
		return distance(new WorldPoint(loc));
	}

	private double distance(WorldPoint loc) {
		return distance(loc.x, loc.y, loc.z);
	}

	private double distance(double locX, double locY, double locZ) {
		return Math.sqrt(Math.pow(worldPoint.x - locX, 2) + Math.pow(worldPoint.y - locY, 2) + Math.pow(worldPoint.z - locZ, 2));
	}

	private double distanceIgnoreY(double locX, double locZ) {
		return Math.sqrt(Math.pow(worldPoint.x - locX, 2) + Math.pow(worldPoint.z - locZ, 2));
	}

	private boolean intersects(WorldPoint point, double compareRadius) {
		return intersects(point.x, point.y, point.z, compareRadius);
	}

	private boolean intersects(double pointX, double pointY, double pointZ, double compareRadius) {
		return distance(pointX, pointY, pointZ) < compareRadius;
	}

	private boolean intersectsIgnoreY(WorldPoint point, double compareRadius) {
		return intersectsIgnoreY(point.x, point.z, compareRadius);
	}

	private boolean intersectsIgnoreY(double pointX, double pointZ, double compareRadius) {
		return distanceIgnoreY(pointX, pointZ) < compareRadius;
	}

	public boolean isInRadius(Location loc) {
		return intersectsIgnoreY(new WorldPoint(loc), radius);
	}

	public boolean isInTeamProtectedRadius(Location loc) {
		return protection != null && intersectsIgnoreY(new WorldPoint(loc), protection.getProtectedRadius());
	}

	public boolean isInKeepRadius(Location loc) {
		return protection != null && intersectsIgnoreY(new WorldPoint(loc), protection.getKeepRadius());
	}

	public boolean isInAdminProtectedRadius(Location loc) {
		return protection != null && (protection.getType() == PlotProtection.ADMIN || intersects(new WorldPoint(loc), protection.getAdminRadius()));
	}

	public boolean isInTriggerRadius(Location loc) {
		return protection != null && intersects(new WorldPoint(loc), protection.getTriggerRadius());
	}

	public boolean isUnderCenter(Location location) {
		Location plotLocation = getLocation();
		return plotLocation.getBlockX() == location.getBlockX() && plotLocation.getBlockZ() == location.getBlockZ() && plotLocation.getBlockY() > location.getBlockY();
	}

	public int getId() {
		return id;
	}

	public void setId(int value) {
		id = value;
	}

	public String getName() {
		return name;
	}

	public String getColoredName() {
		return UserFaction.getChatColor(getProtection().getType()) + name;
	}

	public void setName(String value) {
		name = value;
	}

	public PlotProtection getProtection() {
		return protection;
	}

	public boolean isEventProtected() {
		return protection != null && protection.getType() == PlotProtection.EVENT;
	}

	public boolean isAdminProtected() {
		return protection != null && protection.getType() == PlotProtection.ADMIN;
	}

	public boolean isFactionProtected(int faction) {
		return protection != null && protection.getType() == faction;
	}

	public boolean isFactionProtected() {
		return protection != null &&
				(protection.getType() == UserFaction.FactionRed ||
						protection.getType() == UserFaction.FactionBlue ||
						protection.getType() == UserFaction.FactionGreen ||
						protection.getType() == UserFaction.FactionGold
				);
	}

	public boolean isImmuneToAttack() {
		if (isAdminProtected()) {
			// Admin plots always immune
			return true;
		}
		if (protection.isCapturable()) {
			// Capturable plots never immune
			return false;
		}

		ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Chicago"));
		int hour = now.getHour();

		// Immune if before 8pm (hour values 19 and below) or after 11 (hour values 23 and above)
		return hour < 20 || hour > 22;
		// Immune if before 8pm (hour values 19 and below), resets to 0 at midnight so we don't have to check upper bound
		//return hour < 20;
		// Example of use:
		// immune if before 7pm (hour values 18 and below) or after 11 (hour values 23 and above)
		// return hour < 19 || hour > 22;
		// immune if before 6pm (hour values 17 and below) or after 10 (hour values 22 and above)
		// return hour < 18 || hour > 21;
	}

	public void setProtection(PlotProtection value) {
		protection = value;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float value) {
		radius = value;
	}

	public int getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(int value) {
		orderNumber = value;
	}

	public int getSetterId() {
		return setterId;
	}

	public void setSetterId(int value) {
		setterId = value;
	}

	public Siege getSiege() {
		return activeSiege;
	}

	public void setSiege(Siege value) {
		activeSiege = value;
	}

	public String getInfo() {
		String info = name + ChatService.RequestedInfo + " (Protection: " + protection.asString() + ChatService.RequestedInfo + ")\n";
		info += "Radius: " + radius + "\n";
		info += "Protected Radius: " + protection.getProtectedRadius() + "\n";
		if (protection.getKeepRadius() > 0) {
			info += "Keep protected radius: " + protection.getKeepRadius() + "\n";
		}
		if (protection.getAdminRadius() > 0) {
			info += "Admin Radius: " + protection.getAdminRadius() + "\n";
		}
		if (protection.getTriggerRadius() > 0) {
			info += "Trigger Radius: " + protection.getTriggerRadius() + "\n";
		}
		if (protection.getCapturable()) {
			info += "Capturable: " + protection.getCapturable() + "\n";
			info += "Capture time: " + protection.getCaptureTime() + "s\n";
		}
		info += "Rally number: " + orderNumber + "\n";
		return info;
	}

}
