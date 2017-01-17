package keepcraft.data.models;

import org.bukkit.Location;

public class Direction {

    public static Location lookAt(Location loc, Location lookat) {
        //Clone the loc to prevent applied changes to the input loc
        loc = loc.clone();

        // Values of change in distance (make it relative)
        double dx = lookat.getX() - loc.getX();
        double dy = lookat.getY() - loc.getY();
        double dz = lookat.getZ() - loc.getZ();

        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                loc.setYaw((float) (1.5 * Math.PI));
            } else {
                loc.setYaw((float) (0.5 * Math.PI));
            }
            loc.setYaw((float) loc.getYaw() - (float) Math.atan(dz / dx));
        } else if (dz < 0) {
            loc.setYaw((float) Math.PI);
        }

        // Get the distance from dx/dz
        double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));

        // Set pitch
        loc.setPitch((float) -Math.atan(dy / dxz));

        // Set values, convert to degrees (invert the yaw since Bukkit uses a different yaw dimension format)
        loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
        loc.setPitch(loc.getPitch() * 180f / (float) Math.PI);

        return loc;
    }

    /**
     * Get the cardinal compass direction of a player.
     *
     * @param player
     * @return
     */
    public static String getCardinalDirection(Location location) {
        double rot = (location.getYaw() - 90) % 360;
        if (rot < 0) {
            rot += 360.0;
        }
        return getDirection(rot);
    }

    /**
     * Converts a rotation to a cardinal direction name.
     *
     * @param rot
     * @return
     */
    private static String getDirection(double rot) {
        if (0 <= rot && rot < 22.5) {
            return "west";
        } else if (22.5 <= rot && rot < 67.5) {
            return "northwest";
        } else if (67.5 <= rot && rot < 112.5) {
            return "north";
        } else if (112.5 <= rot && rot < 157.5) {
            return "northeast";
        } else if (157.5 <= rot && rot < 202.5) {
            return "east";
        } else if (202.5 <= rot && rot < 247.5) {
            return "southeast";
        } else if (247.5 <= rot && rot < 292.5) {
            return "south";
        } else if (292.5 <= rot && rot < 337.5) {
            return "southwest";
        } else if (337.5 <= rot && rot < 360.0) {
            return "west";
        } else {
            return null;
        }
    }
}
