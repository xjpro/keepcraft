package keepcraft;

import org.bukkit.Location;
import keepcraft.data.models.Plot;
import keepcraft.data.models.PlotProtection;
import keepcraft.data.models.User;
import keepcraft.data.models.UserPrivilege;

public abstract class Privilege {

    private static final int MAP_RADIUS = 3000;

    public static boolean canPromote(User user) {
        return isAdmin(user);
    }

    public static boolean canDemote(User user) {
        return isAdmin(user);
    }

    public static boolean canSetSpawn(User user) {
        return isAdmin(user);
    }

    public static boolean canModifyUserData(User user) {
        return isAdmin(user);
    }

    public static boolean canModifyServerConditions(User user) {
        return isAdmin(user);
    }

    public static boolean canInteract(User user, Location modifyingLocation, Plot plot) {
        Location center = new Location(Keepcraft.getWorld(), 0, 64, 0);
        if (center.distance(modifyingLocation) > MAP_RADIUS) {
            return false; // maximum map size exceeded
        }

        if (plot == null || plot.getProtection() == null) {
            return true;
        }

        // match with our user wrapper players
        switch (user.getPrivilege()) {
            case UserPrivilege.ADMIN:
            case UserPrivilege.MODERATOR:
                return true;
            case UserPrivilege.SUPER:
            case UserPrivilege.MEMBER:
                // More conditions apply
                int protectionType = plot.getProtection().getType();
                switch (protectionType) {
                    case PlotProtection.ADMIN:
                    case PlotProtection.SPAWN:
                        return false;
                    case PlotProtection.PROTECTED:
                    case PlotProtection.PRIVATE:
                        // TODO: will need to search all permissions...
                        return false;
                    case PlotProtection.FACTION_A:
                    case PlotProtection.FACTION_B:
                    case PlotProtection.FACTION_C:
                    case PlotProtection.FACTION_E:
                        if (plot.isInAdminProtectedRadius(modifyingLocation)) {
                            return false;
                        }
                        if (plot.isInTeamProtectedRadius(modifyingLocation)) {
                            return user.getFaction() == protectionType;
                        }
                        if (plot.isInPartialRadius(modifyingLocation)) {
                            return user.getFaction() == protectionType;
                        }
                        return true;
                    case PlotProtection.PUBLIC:
                        return true;
                }
                break;
        }

        // Didn't meet any allowed conditions
        return false;
    }

    public static boolean canModifyPlotData(User user, Plot plot) {
        if (user.getPrivilege() == UserPrivilege.ADMIN) {
            return true;
        }
        if (user.getId() == plot.getSetterId()) {
            return true;
        }

        return false;
    }

    private static boolean isAdmin(User user) {
        return user.getPrivilege() == UserPrivilege.ADMIN;
    }

}
