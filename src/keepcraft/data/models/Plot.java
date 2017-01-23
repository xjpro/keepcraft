package keepcraft.data.models;

import org.bukkit.Location;
import keepcraft.Chat;
import keepcraft.tasks.Siege;

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

    public double distance(Location loc) {
        return distance(new WorldPoint(loc));
    }

    public double distance(WorldPoint loc) {
        return distance(loc.x, loc.y, loc.z);
    }

    public double distance(double locX, double locY, double locZ) {
        return Math.sqrt(Math.pow(worldPoint.x - locX, 2) + Math.pow(worldPoint.y - locY, 2) + Math.pow(worldPoint.z - locZ, 2));
    }

    public double distanceIgnoreY(double locX, double locZ) {
        return Math.sqrt(Math.pow(worldPoint.x - locX, 2) + Math.pow(worldPoint.z - locZ, 2));
    }

    public boolean intersects(WorldPoint point, double compareRadius) {
        return intersects(point.x, point.y, point.z, compareRadius);
    }

    public boolean intersects(double pointX, double pointY, double pointZ, double compareRadius) {
        if (distance(pointX, pointY, pointZ) < compareRadius) {
            return true;
        }
        return false;
    }

    public boolean intersectsIgnoreY(WorldPoint point, double compareRadius) {
        return intersectsIgnoreY(point.x, point.z, compareRadius);
    }

    public boolean intersectsIgnoreY(double pointX, double pointZ, double compareRadius) {
        if (distanceIgnoreY(pointX, pointZ) < compareRadius) {
            return true;
        }
        return false;
    }

    public boolean intersectsRadius(Location loc) {
        return intersectsIgnoreY(new WorldPoint(loc), radius);
    }

    public boolean intersectsProtectedRadius(Location loc) {
        if (protection == null) {
            return false;
        }
        return intersectsIgnoreY(new WorldPoint(loc), protection.getProtectedRadius());
    }

    public boolean intersectsPartialRadius(Location loc) {
        if (protection == null) {
            return false;
        }
        return intersectsIgnoreY(new WorldPoint(loc), protection.getPartialRadius());
    }

    public boolean intersectsAdminRadius(Location loc) {
        if (protection == null) {
            return false;
        }
        if (protection.getType() == PlotProtection.ADMIN) {
            return true;
        }
        return intersects(new WorldPoint(loc), protection.getAdminRadius());
    }

    public boolean intersectsTriggerRadius(Location loc) {
        if (protection == null) {
            return false;
        }
        return intersects(new WorldPoint(loc), protection.getTriggerRadius());
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

    public boolean isSpawnProtected() {
        if (protection == null) {
            return false;
        }
        return protection.getType() == PlotProtection.SPAWN;
    }

    public boolean isEventProtected() {
        if (protection == null) {
            return false;
        }
        return protection.getType() == PlotProtection.EVENT;
    }

    public boolean isAdminProtected() {
        if (protection == null) {
            return false;
        }
        return protection.getType() == PlotProtection.ADMIN;
    }

    public boolean isFactionProtected(int faction) {
        if (protection == null) {
            return false;
        }
        return protection.getType() == faction;
    }

    public boolean isTNTable() {
        if (protection.isCapturable()) {
            return true;
        }
        if (isFactionProtected(UserFaction.FactionGold)) {
            return true;
        }

        /*
        1/17/2017 - removed this nonsense that created TNT windows when enough people were on 
        double attackerCount = 0;
        double defenderCount = 0;

        for (User user : DataCache.retrieveAll(User.class)) {
            if (user.isAdmin()) {
                continue; // admins don't count for this
            } else if (isFactionProtected(user.getFaction())) {
                defenderCount++; // The plot under attack matches this user's plot, they are a defender
            } else if (this == user.getCurrentPlot() && !isFactionProtected(user.getFaction())) {
                attackerCount++; // They are in the plot and the plot is not theirs, they are an attacker
            }
        }

        long lastExplosionSeconds = (System.currentTimeMillis() - getLastExplosion()) / 1000;

        if (defenderCount < ServerConditions.getMinimumDefenderCount() && lastExplosionSeconds > 600) {
            return false;
        }*/

        return true;
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
        String info = name + Chat.RequestedInfo + " (Protection: " + protection.asString() + Chat.RequestedInfo + ")\n";
        info += "Radius: " + radius + "\n";
        info += "Protected Radius: " + protection.getProtectedRadius() + "\n";
        info += "Partial protection radius: " + protection.getPartialRadius() + "\n";
        info += "Admin Radius: " + protection.getAdminRadius() + "\n";
        info += "Trigger Radius: " + protection.getTriggerRadius() + "\n";
        info += "Capturable: " + protection.getCapturable() + "\n";
        info += "Capture time: " + protection.getCaptureTime() + "s\n";
        info += "Rally number: " + orderNumber + "\n";
        return info;
    }

}
