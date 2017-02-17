package keepcraft;

import org.bukkit.Location;
import keepcraft.data.models.Plot;
import keepcraft.data.models.PlotProtection;
import keepcraft.data.models.User;
import keepcraft.data.models.UserPrivilege;

public abstract class Privilege {

	private static final int MAP_RADIUS = 3000;

	public static boolean canInteract(User user, Location modifyingLocation, Plot plot) {
//		Location worldCenter = new Location(Keepcraft.getWorld(), 0, 64, 0);
//		if (worldCenter.distance(modifyingLocation) > MAP_RADIUS) {
//			return false; // maximum map size exceeded
//		}

		if (plot == null || plot.getProtection() == null) {
			return true; // Not in a plot
		}

		// match with our user wrapper players
		switch (user.getPrivilege()) {
			case UserPrivilege.ADMIN:
				return true;
			case UserPrivilege.SUPER:
			case UserPrivilege.MEMBER:
				// More conditions apply
				int protectionType = plot.getProtection().getType();
				switch (protectionType) {
					case PlotProtection.ADMIN:
						return false;
					case PlotProtection.PRIVATE:
						// TODO implement private plots, search for permissions
						return false;
					case PlotProtection.FACTION_A:
					case PlotProtection.FACTION_B:
					case PlotProtection.FACTION_C:
					case PlotProtection.FACTION_E:
						// In a team protected plot

						if (plot.isInAdminProtectedRadius(modifyingLocation)) {
							return false; // Only admin can modify admin protected radius
						}
						if (plot.isUnderCenter(modifyingLocation)) {
							// No one may modify the core underneath center of the team plot
							return false;
						}
						if (plot.isInKeepRadius(modifyingLocation)) {
							// TODO this would be used for an inner area for higher level faction members, same as team protection for now
							return user.getFaction() == protectionType;
						}
						if (plot.isInTeamProtectedRadius(modifyingLocation)) {
							return user.getFaction() == protectionType;
						}
						return true; // In plot but not in any protected radius
					case PlotProtection.PUBLIC:
						return true;
				}
				break;
		}

		// Didn't meet any allowed conditions
		return false;
	}

	public static boolean canModifyPlotData(User user, Plot plot) {
		return user.getPrivilege() == UserPrivilege.ADMIN;// || user.getId() == plot.getSetterId();
	}

	public static boolean isAdmin(User user) {
		return user.getPrivilege() == UserPrivilege.ADMIN;
	}

}
