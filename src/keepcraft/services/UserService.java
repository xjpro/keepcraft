package keepcraft.services;

import keepcraft.Keepcraft;
import keepcraft.data.UserDataManager;
import keepcraft.data.UserStatsDataManager;
import keepcraft.data.models.User;
import keepcraft.data.models.UserTeam;
import keepcraft.data.models.UserPrivilege;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class UserService {

	private final Plugin plugin;
	private final UserDataManager userDataManager;
	private final UserStatsDataManager userStatsDataManager;
	private HashMap<String, User> onlineUsers = new HashMap<>();

	public UserService(Plugin plugin, UserDataManager userDataManager, UserStatsDataManager userStatsDataManager) {
		this.plugin = plugin;
		this.userDataManager = userDataManager;
		this.userStatsDataManager = userStatsDataManager;
	}

	public void refreshCache() {
		onlineUsers.clear();
	}

	public Collection<User> getOnlineUsers() {
		return onlineUsers.values();
	}

	public boolean userIsRegistered(String name) {
		return userDataManager.exists(name);
	}

	public User getUser(String name) {
		return userDataManager.getData(name);
	}

	public User getOnlineUser(String name) {
		return onlineUsers.get(name);
	}

	// todo potential simpler way to decorate a player with extra data
//	public Player loadMetadata(Player player) {
//		User user = getUser(player.getName());
//		player.setMetadata("privilege", new FixedMetadataValue(Keepcraft.getPlugin(), user.getPrivilege()));
//		player.setMetadata("faction", new FixedMetadataValue(Keepcraft.getPlugin(), user.getFaction()));
//		player.setMetadata("loggedOffFriendlyPlotId", new FixedMetadataValue(Keepcraft.getPlugin(), user.getLoggedOffFriendlyPlotId()));
//		return player;
//	}

	public User loadOfflineUser(String name) {
		User user = userDataManager.exists(name) ? userDataManager.getData(name) : createUser(name);
		user.startPlayTime();
		onlineUsers.put(user.getName(), user);
		return user;
	}

	public void saveUserAndSetOffline(User user) {
		userDataManager.updateData(user);
		userStatsDataManager.saveData(user.getName(), Keepcraft.getWorld().getUID(), user.getUserStats());
		onlineUsers.remove(user.getName());
	}

	public void updateUser(User user) {
		userDataManager.updateData(user);
	}

	public boolean removeUser(User user) {
		if (!userDataManager.exists(user.getName())) {
			return false;
		}
		userDataManager.deleteData(user);
		return true;
	}

	private User createUser(String userName) {
		int faction;

		// Determine which team to place the new user on, using two strategies

		UUID worldGUID = plugin != null ? plugin.getServer().getWorld("world").getUID() : null;
		List<String> recentlyPlayedUserNamesByPlayTime = userStatsDataManager.getRecentlyPlayedUserNamesByPlayTime(worldGUID);

		// Take the first half of the previously played user names as our list of "active" users
		List<String> previouslyActiveUserNames = recentlyPlayedUserNamesByPlayTime.subList(0, (int) Math.ceil(recentlyPlayedUserNamesByPlayTime.size() * 0.5));

		if (previouslyActiveUserNames.stream().anyMatch(previouslyActiveUserName -> previouslyActiveUserName.equals(userName))) {
			// This user identified as a previously active user, balance these users so they are even on both teams
			int prevActiveReds = userDataManager.getPreviouslyActiveTeamCount(UserTeam.RED.getId(), previouslyActiveUserNames);
			int prevActiveBlues = userDataManager.getPreviouslyActiveTeamCount(UserTeam.BLUE.getId(), previouslyActiveUserNames);
			int prevActiveGreens = 9999;//userDataManager.getPreviouslyActiveTeamCount(UserTeam.GREEN.getId(), previouslyActiveUserNames);

			if (prevActiveReds == prevActiveBlues) {
				// Previously actives are equal, select based on current numbers instead
				faction = selectTeamUsingCurrentUserCount();
			} else {
				faction = UserTeam.getSmallestFaction(prevActiveReds, prevActiveBlues, prevActiveGreens);
			}
		} else {
			// This user has not been previously active, place them on the smallest team based on current numbers
			faction = selectTeamUsingCurrentUserCount();
		}

		User user = new User(userName);
		user.setPrivilege(UserPrivilege.MEMBER_VETERAN);
		user.setFaction(UserTeam.getFaction(faction));
		user.setMoney(0);
		user.setLoggedOffFriendlyPlotId(0);
		userDataManager.putData(user);
		return user;
	}

	private int selectTeamUsingCurrentUserCount() {
		// This user has not been previously active, place them on the smallest team
		int redCount = userDataManager.getFactionCount(UserTeam.RED.getId());
		int blueCount = userDataManager.getFactionCount(UserTeam.BLUE.getId());
		int greenCount = 9999;//this.getFactionCount(UserTeam.GREEN.getId());

		if (redCount == blueCount) {
			return UserTeam.getRandomFaction();
		} else {
			return UserTeam.getSmallestFaction(redCount, blueCount, greenCount);
		}
	}
}
