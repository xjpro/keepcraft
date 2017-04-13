package keepcraft.data.models;

import keepcraft.services.ChatService;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.Date;

/**
 * Data for a player.
 */
public class User implements ChatParticipant {

	public static int InCombatTimeoutSeconds = 15;

	// Persistent data, from database
	private final String name;
	private UserPrivilege privilege;
	private UserTeam team;
	private int money;
	private int loggedOffFriendlyPlotId;
	private boolean firstTimeLogin;
	private long secondsPlayedOnServer;

	// Non persistent real time data
	private Date logOnDateTime = null;
	private Plot currentPlot = null;
	private Container targetContainer = null;
	private boolean receiveGlobalMessages = true;
	private Date lastCombat = null;
	private Plot rallyingTo = null;
	private boolean hiding = false;
	private boolean glowing = false;
	private boolean toggleSneak = false; // todo persist in db
	private boolean persistentSneak = false;
	private Location lastFishLocation = null;

	// Stats (persisted on log off)
	private UserStats userStats = new UserStats();

	public User(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getColoredName() {
		if (privilege == UserPrivilege.ADMIN) {
			return ChatService.NameAdmin + getName() + ChatColor.RESET;
		} else {
			return team.getChatColor() + getName() + ChatColor.RESET;
		}
	}

	@Override
	public String getChatTag() {
		if (privilege == UserPrivilege.ADMIN) {
			return ChatService.NameAdmin + "<" + getName() + ">" + ChatColor.RESET;
		} else {
			return team.getChatColor() + "<" + getName() + ">" + ChatColor.RESET;
		}
	}

	@Override
	public String getChatTag(UserTeam userTeam) {
		if (privilege == UserPrivilege.ADMIN) {
			ChatColor chatColor = userTeam.getChatColor();
			return chatColor + "<" + ChatService.NameAdmin + getName() + chatColor + ">" + ChatColor.RESET;
		}
		return getChatTag();
	}

	public UserPrivilege getPrivilege() {
		return privilege;
	}

	@Override
	public boolean isAdmin() {
		return privilege == UserPrivilege.ADMIN;
	}

	public void setPrivilege(UserPrivilege value) {
		privilege = value;
	}

	public UserTeam getTeam() {
		return team;
	}

	public void setTeam(UserTeam value) {
		team = value;
	}

	@Override
	public String toString() {
		return getInfo();
	}

	public String getInfo() {
		return getColoredName() + ChatService.RequestedInfo + " (" + privilege + ")";
	}

	public String getPrivateInfo() {
		return getColoredName() + ChatService.RequestedInfo + " (" + privilege + ")";
	}

	public int getMoney() {
		return money;
	}

	public void setMoney(int value) {
		money = value;
	}

	public int getLoggedOffFriendlyPlotId() {
		return loggedOffFriendlyPlotId;
	}

	public void setLoggedOffFriendlyPlotId(int value) {
		loggedOffFriendlyPlotId = value;
	}

	public Plot getCurrentPlot() {
		return currentPlot;
	}

	public void setCurrentPlot(Plot plot) {
		currentPlot = plot;
	}

	public Container getTargetContainer() {
		return targetContainer;
	}

	public void setTargetContainer(Container container) {
		targetContainer = container;
	}

	public boolean isInCombat() {
		return lastCombat != null && ((new Date()).getTime() - lastCombat.getTime()) / 1000 < InCombatTimeoutSeconds;
	}

	public void setInCombat() {
		lastCombat = new Date();
	}

	public Plot getRallyingTo() {
		return rallyingTo;
	}

	public void setRallyingTo(Plot value) {
		rallyingTo = value;
	}

	@Override
	public boolean getReceiveGlobalMessages() {
		return receiveGlobalMessages;
	}

	@Override
	public void setReceiveGlobalMessages(boolean value) {
		receiveGlobalMessages = value;
	}

	public long getPlayedSeconds() {
		return ((new Date()).getTime() - logOnDateTime.getTime()) / 1000;
	}

	public void startPlayTime() {
		logOnDateTime = new Date();
	}

	public UserStats getUserStats() {
		return userStats;
	}

	public boolean isFirstTimeLogin() {
		return firstTimeLogin;
	}

	public void setFirstTimeLogin(boolean value) {
		firstTimeLogin = value;
	}

	public boolean isHiding() {
		return hiding;
	}

	public void setHiding(boolean stealth) {
		this.hiding = stealth;
	}

	public boolean isGlowing() {
		return glowing;
	}

	public void setGlowing(boolean value) {
		this.glowing = value;
	}

	public boolean getToggleSneak() {
		return toggleSneak;
	}

	public void setToggleSneak(boolean toggleSneak) {
		this.toggleSneak = toggleSneak;
	}

	public boolean getPersistentSneak() {
		return persistentSneak;
	}

	public void setPersistentSneak(boolean sneak) {
		this.persistentSneak = sneak;
	}

	public long getSecondsPlayedOnServer() {
		return secondsPlayedOnServer;
	}

	public void setSecondsPlayedOnServer(long seconds) {
		secondsPlayedOnServer = seconds;
	}

	public Location getLastFishLocation() {
		return lastFishLocation;
	}

	public void setLastFishLocation(Location location) {
		lastFishLocation = location;
	}

	@Override
	public boolean canApprove(User target) {
		return target.getPrivilege() == UserPrivilege.MEMBER_START &&
				(privilege.equals(UserPrivilege.ADMIN) || (privilege.equals(UserPrivilege.MEMBER_VETERAN) && getTeam() == target.getTeam()));
	}
}
