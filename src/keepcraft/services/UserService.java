package keepcraft.services;

import keepcraft.Keepcraft;
import keepcraft.data.ApprovalDataManager;
import keepcraft.data.UserConnectionDataManager;
import keepcraft.data.UserDataManager;
import keepcraft.data.UserStatsDataManager;
import keepcraft.data.models.ChatParticipant;
import keepcraft.data.models.User;
import keepcraft.data.models.UserTeam;
import keepcraft.data.models.UserPrivilege;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class UserService {

	private final Plugin plugin;
	private final UserDataManager userDataManager;
	private final UserStatsDataManager userStatsDataManager;
	private final UserConnectionDataManager userConnectionDataManager;
	private final ApprovalDataManager approvalDataManager;
	private HashMap<String, User> onlineUsers = new HashMap<>();

	public UserService(Plugin plugin, UserDataManager userDataManager, UserStatsDataManager userStatsDataManager,
					   UserConnectionDataManager userConnectionDataManager, ApprovalDataManager approvalDataManager) {
		this.plugin = plugin;
		this.userDataManager = userDataManager;
		this.userStatsDataManager = userStatsDataManager;
		this.userConnectionDataManager = userConnectionDataManager;
		this.approvalDataManager = approvalDataManager;
	}

	public void refreshCache() {
		onlineUsers.clear();
	}

	public Collection<User> getOnlineUsers() {
		return onlineUsers.values();
	}

	public Collection<User> getUsers() {
		return userDataManager.getAllUsers();
	}

	public User getUser(String name) {
		User onlineUser = getOnlineUser(name);
		return onlineUser != null ? onlineUser : userDataManager.getData(name);
	}

	public User getOnlineUser(String name) {
		return onlineUsers.get(name);
	}

	// todo potential simpler way to decorate a player with extra data
//	public Player loadMetadata(Player player) {
//		User user = getUser(player.getName());
//		player.setMetadata("privilege", new FixedMetadataValue(Keepcraft.getPlugin(), user.getPrivilege()));
//		player.setMetadata("faction", new FixedMetadataValue(Keepcraft.getPlugin(), user.getTeam()));
//		player.setMetadata("loggedOffFriendlyPlotId", new FixedMetadataValue(Keepcraft.getPlugin(), user.getLoggedOffFriendlyPlotId()));
//		return player;
//	}

	public User loadOfflineUser(String name, String ip) {
		User user = userDataManager.exists(name) ? userDataManager.getData(name) : createUser(name);
		user.startPlayTime();
		onlineUsers.put(user.getName(), user);

		// Log ip
		userConnectionDataManager.saveIP(name, ip);

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

	public void approveUser(User user, ChatParticipant approver) {
		user.setPrivilege(UserPrivilege.MEMBER_VETERAN);
		approvalDataManager.saveApproval(user.getName(), approver.getName());
		updateUser(user);
	}

	public void setFirstLogin(User user) {
		userDataManager.updateFirstLogin(user);
	}

	public boolean removeUser(User user) {
		if (!userDataManager.exists(user.getName())) {
			return false;
		}
		userDataManager.deleteData(user);
		return true;
	}

	public void distributeKnownUsers() {
		List<String> recentlyPlayedUserNamesByPlayTime = userStatsDataManager.getRecentlyPlayedUserNamesByPlayTime();
		// We now have a list a users sorted by their play time A B C D E F

		// Remove known admins todo make this dynamic
		recentlyPlayedUserNamesByPlayTime.removeAll(Arrays.asList("SummitMC", "Sivias", "DojiSuave"));

		// Removed non-approved people
		recentlyPlayedUserNamesByPlayTime.removeIf(userName -> !approvalDataManager.isApproved(userName));

		// Alternate through the list, placing users in each team
		// Teams will be A C E and B D F
		// Only taking top 2/3rd of active users, rest will be assigned as they log in
		UserTeam userTeam = UserTeam.getTeam(UserTeam.getRandomTeamId());
		int numberOfPlayersToAssign = (int) (recentlyPlayedUserNamesByPlayTime.size() * 0.6666);
		numberOfPlayersToAssign = numberOfPlayersToAssign % 2 == 0 ? numberOfPlayersToAssign : numberOfPlayersToAssign + 1;

		for (String userName : recentlyPlayedUserNamesByPlayTime.subList(0, numberOfPlayersToAssign)) {
			User user = new User(userName);
			user.setPrivilege(UserPrivilege.MEMBER_VETERAN);
			user.setTeam(userTeam);
			user.setMoney(0);
			user.setLoggedOffFriendlyPlotId(-1);
			userDataManager.putData(user);

			// switch to next team for next user
			userTeam = userTeam == UserTeam.RED ? UserTeam.BLUE : UserTeam.RED;
		}
	}

	// Create a user not distributed at start of map
	private User createUser(String userName) {
		boolean previouslyApproved = approvalDataManager.isApproved(userName);

		User user = new User(userName);
		user.setPrivilege(previouslyApproved ? UserPrivilege.MEMBER_VETERAN : UserPrivilege.MEMBER_START);
		user.setTeam(UserTeam.getTeam(selectTeamUsingCurrentUserCount()));
		user.setMoney(0);
		user.setLoggedOffFriendlyPlotId(-1);
		user.setFirstTimeLogin(true);
		userDataManager.putData(user);
		return user;
	}

	private int selectTeamUsingCurrentUserCount() {
		// This user has not been previously active, place them on the smallest team
		int redCount = userDataManager.getTeamCount(UserTeam.RED.getId());
		int blueCount = userDataManager.getTeamCount(UserTeam.BLUE.getId());
		int greenCount = 9999;//this.getTeamCount(UserTeam.GREEN.getId());

		if (redCount == blueCount) {
			return UserTeam.getRandomTeamId();
		} else {
			return UserTeam.getSmallestFaction(redCount, blueCount, greenCount);
		}
	}
}
