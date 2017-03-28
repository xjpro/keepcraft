package keepcraft;

import keepcraft.data.models.*;
import org.bukkit.Location;

public abstract class Privilege {

	public static boolean canInteract(User user, Location modifyingLocation, Plot plot) {
		if (plot == null || plot.getProtection() == null) {
			return true; // Not in a plot
		}

		// match with our user wrapper players
		switch (user.getPrivilege()) {
			case ADMIN:
				return true;
			case MEMBER_VETERAN:
			case MEMBER_NORMAL:
			case MEMBER_START:
				// More conditions apply
				int protectionType = plot.getProtection().getType();
				switch (protectionType) {
					case PlotProtection.ADMIN:
						return false;
					case PlotProtection.PRIVATE:
						// TODO implement private plots, search for permissions
						return false;
					case PlotProtection.PUBLIC:
						return true;
					default:

						UserTeam userTeam = UserTeam.getTeam(protectionType);
						if (userTeam != null) {
							// In a team protected plot

							if (plot.isInAdminProtectedRadius(modifyingLocation)) {
								return false; // Only admin can modify admin protected radius
							}
							if (plot.isUnderCenter(modifyingLocation)) {
								// No one may modify the core underneath center of the team plot
								return false;
							}
							if (plot.isInKeepRadius(modifyingLocation)) {
								// TODO this would be used for an inner area for higher level team members, same as team protection for now
								return user.getTeam().getId() == protectionType;
							}
							if (plot.isInTeamProtectedRadius(modifyingLocation)) {
								return user.getTeam().getId() == protectionType;
							}
						}

						return true; // In plot but not in any protected radius
				}
		}

		// Didn't meet any allowed conditions
		return false;
	}

	public static boolean isAdmin(User user) {
		return user.getPrivilege() == UserPrivilege.ADMIN;
	}

}
