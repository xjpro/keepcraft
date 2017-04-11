package keepcraft.data.models;

import keepcraft.services.ChatService;
import keepcraft.tasks.Siege;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * Persistent data for a plot.
 */
public class Plot {

	public final static int DEFAULT_OUTPOST_RADIUS = 30;
	public final static int DEFAULT_RADIUS = 10;
	public final static int DEFAULT_TRIGGER_RADIUS = 10;
	private final static int UNDER_ATTACK_TIMEOUT_SECONDS = 10 * 60;

	private final int id;
	private final PlotProtection protection;
	private WorldPoint worldPoint;
	private double radius;
	private String name;
	private int orderNumber = -1;
	private String creatorName;

	// Real time happenings
	private Siege activeSiege = null;
	private Date lastAttackDate = new Date(0);

	public Plot(int id, PlotProtection protection) {
		this.id = id;
		this.protection = protection;
	}

	public boolean isBasePlot() {
		return isTeamProtected() && !protection.isCapturable();
	}

	public boolean isAttackInProgress() {
		return getSecondsSinceLastNotification() <= Plot.UNDER_ATTACK_TIMEOUT_SECONDS;
	}

	public boolean canBeRalliedTo(User user) {
		return isTeamProtected(user.getTeam()) && !protection.isCaptureInProgress();
	}

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
		return isInRadius(loc);
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
		return plotLocation.getBlockX() == location.getBlockX() && plotLocation.getBlockZ() == location.getBlockZ() && plotLocation.getBlockY() >= location.getBlockY();
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getColoredName() {
		return UserTeam.getChatColor(getProtection().getType()) + name + ChatColor.RESET;
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

	public boolean isTeamProtected(UserTeam userTeam) {
		return protection != null && protection.getType() == userTeam.getId();
	}

	public boolean isTeamProtected() {
		return protection != null &&
				(protection.getType() == UserTeam.RED.getId() ||
						protection.getType() == UserTeam.BLUE.getId() ||
						protection.getType() == UserTeam.GREEN.getId() ||
						protection.getType() == UserTeam.GOLD.getId()
				);
	}

	public boolean isImmuneToAttack() {
		if (isAdminProtected()) {
			// Admin plots always immune
			return true;
		}
//		if (protection.isCapturable()) {
//			// Capturable plots never immune
//			return false;
//		}

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

	public double getRadius() {
		return radius;
	}

	public void setRadius(double value) {
		radius = value;
	}

	public int getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(int value) {
		orderNumber = value;
	}

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(String value) {
		creatorName = value;
	}

	public Siege getSiege() {
		return activeSiege;
	}

	public void setSiege(Siege value) {
		activeSiege = value;
	}

	private long getSecondsSinceLastNotification() {
		return ((new Date()).getTime() - lastAttackDate.getTime()) / 1000;
	}

	public void setUnderAttack() {
		lastAttackDate = new Date();
	}

	public String getInfo() {
		String info = getColoredName() + ChatService.RequestedInfo + " (Protection: " + protection.asString() + ChatService.RequestedInfo + ")\n";
		info += "Radius: " + radius + "\n";
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
		//info += "Rally number: " + orderNumber + "\n";
		if (getCreatorName() != null) {
			info += String.format("Created by: %s", getCreatorName());
		}
		return info;
	}

	// Can interact, i.e. open chests, activate switches, etc.
	public boolean canInteract(User user, Block interactedWith) {
		UserPrivilege userPrivilege = user.getPrivilege();
		if (userPrivilege == UserPrivilege.ADMIN) return true;

		int plotProtectionId = getProtection().getType();
		switch (plotProtectionId) {
			case PlotProtection.PUBLIC:
			case PlotProtection.PRIVATE: // todo unsupported at the moment
				return true;
			default:
				// Team protection
				if (isInTeamProtectedRadius(interactedWith.getLocation())) {
					if (user.getTeam().getId() == plotProtectionId) {
						if (userPrivilege == UserPrivilege.NONMEMBER) {
							return false; // Exception: non-members cannot interact with anything
						} else if (userPrivilege == UserPrivilege.MEMBER_START) {
							// Special rules for starting members: can interact with basic doors, switches, vehicles
							switch (interactedWith.getType()) {
								case WOODEN_DOOR:
								case DARK_OAK_DOOR:
								case ACACIA_DOOR:
								case BIRCH_DOOR:
								case JUNGLE_DOOR:
								case SPRUCE_DOOR:
								case TRAP_DOOR:
								case WOOD_PLATE:
								case STONE_PLATE:
								case LEVER:
								case WOOD_BUTTON:
								case STONE_BUTTON:
								case BOAT:
								case BOAT_ACACIA:
								case BOAT_BIRCH:
								case BOAT_DARK_OAK:
								case BOAT_JUNGLE:
								case BOAT_SPRUCE:
								case MINECART:
								case ENDER_CHEST:
									return true;
								case CHEST:
									// Opens chest on wood
									return interactedWith.getRelative(BlockFace.DOWN).getType() == Material.WOOD;
								default:
									return false;
							}
						} else if (userPrivilege == UserPrivilege.MEMBER_NORMAL || userPrivilege == UserPrivilege.MEMBER_VETERAN) {
							// Normal and veteran members can access anything in team protected area
							return true;
						}
					} else {
						// Enemy in a plot
						switch (interactedWith.getType()) {
							// Do not allow enemy team to access buttons, switches, levers, etc.
							// Exception: if the switch is near a door and not a stone plate, destroy it so it can't be used to block TNT placement
							case STONE_BUTTON:
							case STONE_PLATE:
							case IRON_PLATE:
							case GOLD_PLATE:
							case LEVER:
							case TORCH:
							case REDSTONE_TORCH_ON:
							case REDSTONE_TORCH_OFF:
							case PAINTING:
							case SIGN:
								return false;
						}
					}
				}
				return true;
		}
	}

	// Can modify, i.e. place and remove blocks
	public boolean canModify(User user, Location location) {
		UserPrivilege userPrivilege = user.getPrivilege();
		if (userPrivilege == UserPrivilege.ADMIN) return true;

		int plotProtectionId = getProtection().getType();
		switch (plotProtectionId) {
			case PlotProtection.PUBLIC:
			case PlotProtection.PRIVATE: // todo unsupported at the moment
				return true;
			default:
				// Team protection
				if (isInAdminProtectedRadius(location)) {
					return user.isAdmin();
				} else if (isInTeamProtectedRadius(location) && user.getTeam().getId() == plotProtectionId) {
					if (userPrivilege == UserPrivilege.NONMEMBER || userPrivilege == UserPrivilege.MEMBER_START) {
						return false; // Exception: non-members and starting members cannot interact with anything
					}
					if (userPrivilege == UserPrivilege.MEMBER_NORMAL || userPrivilege == UserPrivilege.MEMBER_VETERAN) {
						// Normal and veteran members can access anything in team protected area
						return true;
					}
				}
				return false; // No known cases matched
		}
	}
}
