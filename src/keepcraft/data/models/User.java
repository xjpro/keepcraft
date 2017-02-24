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
	private int faction;
	private int money;
	private int loggedOffFriendlyPlotId;

	// Non persistent real time data
	private Date logOnDateTime = null;
	private Plot currentPlot = null;
	private Container targetContainer = null;
	private boolean receiveGlobalMessages = true;
	private String lastPrivateMessageSender = null;
	private Date lastCombat = null;
	private Plot rallyingTo = null;

	// Tasks
	private int inCombatTaskId = 0;

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
			return UserFaction.getChatColor(this.faction) + name;
		}
	}

	public String getChatTag() {
		if (privilege == UserPrivilege.ADMIN) {
			return ChatService.NameAdmin + "<" + name + ">";
		} else {
			return UserFaction.getChatColor(this.faction) + "<" + name + ">";
		}
	}

	public String getChatTag(int faction) {
		if (privilege == UserPrivilege.ADMIN) {
			ChatColor factionChatColor = UserFaction.getChatColor(faction);
			return factionChatColor + "<" + ChatService.NameAdmin + name + factionChatColor + ">";
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

	public int getFaction() {
		return faction;
	}

	public void setFaction(int value) {
		faction = value;
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
}
