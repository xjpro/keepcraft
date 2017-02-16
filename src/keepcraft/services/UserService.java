package keepcraft.services;

import keepcraft.Keepcraft;
import keepcraft.data.UserDataManager;
import keepcraft.data.UserStatsDataManager;
import keepcraft.data.models.User;
import keepcraft.data.models.UserFaction;
import keepcraft.data.models.UserPrivilege;

import java.util.Collection;
import java.util.HashMap;

public class UserService {

	private final UserDataManager userDataManager;
	private final UserStatsDataManager userStatsDataManager;
	private HashMap<String, User> onlineUsers = new HashMap<>();

	public UserService(UserDataManager userDataManager, UserStatsDataManager userStatisticsDataManager) {
		this.userDataManager = userDataManager;
		this.userStatsDataManager = userStatisticsDataManager;
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
		user.setLogOnTime();
		onlineUsers.put(user.getName(), user);
		return user;
	}

	public void saveUserAndSetOffline(User user) {
		userDataManager.updateData(user);
		userStatsDataManager.saveData(user.getName(), Keepcraft.getWorld().getSeed(), user.getUserStats());
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

	private User createUser(String name) {
		// Determine faction to place on
		int redCount = userDataManager.getFactionCount(UserFaction.FactionRed);
		int blueCount = userDataManager.getFactionCount(UserFaction.FactionBlue);
		int greenCount = 9999;//this.getFactionCount(UserFaction.FactionGreen);

		User user = new User(name);
		user.setPrivilege(UserPrivilege.MEMBER);
		user.setFaction(UserFaction.getSmallestFaction(redCount, blueCount, greenCount));
		user.setMoney(0);
		user.setLoggedOffFriendlyPlotId(0);
		userDataManager.putData(user);
		return user;
	}
}
