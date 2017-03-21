package keepcraft.data.models;

import keepcraft.services.ChatService;
import org.bukkit.ChatColor;

import java.util.Date;

/**
 * Data for a player.
 */
public class User {

	public static int InCombatTimeoutSeconds = 15;

	// Persistent data, from database
	private final String name;
	private UserPrivilege privilege;
	private UserTeam team;
	private int money;
	private int loggedOffFriendlyPlotId;
	private boolean firstTimeLogin;

	// Non persistent real time data
	private Date logOnDateTime = null;
	private Plot currentPlot = null;
	private Container targetContainer = null;
	private boolean receiveGlobalMessages = true;
	private String lastPrivateMessageSender = null;
	private Date lastCombat = null;
	private Plot rallyingTo = null;
	private boolean hasStealth = false;
	private boolean toggleSneak = false; // todo persist in db
	private boolean persistentSneak = false;

	// Stats (persisted on log off)
	private UserStats userStats = new UserStats();

	public User(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getColoredName() {
		if (privilege == UserPrivilege.ADMIN) {
			return ChatService.NameAdmin + name;
		} else {
			return team.getChatColor() + name;
		}
	}

	public String getChatTag() {
		if (privilege == UserPrivilege.ADMIN) {
			return ChatService.NameAdmin + "<" + name + ">";
		} else {
			return team.getChatColor() + "<" + name + ">";
		}
	}

	public String getChatTag(UserTeam userTeam) {
		if (privilege == UserPrivilege.ADMIN) {
			ChatColor chatColor = userTeam.getChatColor();
			return chatColor + "<" + ChatService.NameAdmin + name + chatColor + ">";
		}
		return getChatTag();
	}

	public UserPrivilege getPrivilege() {
		return privilege;
	}

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

	public boolean getReceiveGlobalMessages() {
		return receiveGlobalMessages;
	}

	public void setReceiveGlobalMessages(boolean value) {
		receiveGlobalMessages = value;
	}

	public String getLastPrivateMessageSender() {
		return lastPrivateMessageSender;
	}

	public void setLastPrivateMessageSender(String value) {
		lastPrivateMessageSender = value;
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

	public boolean hasStealth() {
		return hasStealth;
	}

	public void setStealth(boolean stealth) {
		this.hasStealth = stealth;
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
}
