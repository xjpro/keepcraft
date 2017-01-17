package keepcraft.data.models;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import keepcraft.Chat;

/**
 * Data for a player.
 */
public class User {

    // Persistent data, from database
    private int id;
    private String name;
    private int privilege;
    private int faction;
    private int money;
    private int loggedOffPlotId;

    // Non persistent real time data
    private Plot currentPlot = null;
    private LootBlock targetLootBlock = null;
    private Location knownLocation = null;
    private boolean receiveGlobalMessages = true;
    private String lastPrivateMessageSender = null;

    public User(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColoredName() {
        if (privilege == UserPrivilege.ADMIN) {
            return Chat.NameAdmin + name;
        } else {
            return UserFaction.getChatColor(this.faction) + name;
        }
    }

    public String getChatTag() {
        if (privilege == UserPrivilege.ADMIN) {
            return Chat.NameAdmin + "<" + name + ">";
        } else {
            return UserFaction.getChatColor(this.faction) + "<" + name + ">";
        }
    }

    public String getChatTag(int faction) {
        if (privilege == UserPrivilege.ADMIN) {
            ChatColor factionChatColor = UserFaction.getChatColor(faction);
            return factionChatColor + "<" + Chat.NameAdmin + name + factionChatColor + ">";
        }
        return getChatTag();
    }

    public void setName(String value) {
        name = value;
    }

    public int getPrivilege() {
        return privilege;
    }

    public boolean isAdmin() {
        return privilege == UserPrivilege.ADMIN;
    }

    public void setPrivilege(int value) {
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
        return getColoredName() + Chat.RequestedInfo + " (" + UserPrivilege.asString(privilege) + ")";
    }

    public String getPrivateInfo() {
        return getColoredName() + Chat.RequestedInfo + " (" + UserPrivilege.asString(privilege) + ")";
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int value) {
        money = value;
    }

    public int getLastPlotId() {
        return loggedOffPlotId;
    }

    public void setLastPlotId(int value) {
        loggedOffPlotId = value;
    }

    public Plot getCurrentPlot() {
        return currentPlot;
    }

    public void setCurrentPlot(Plot value) {
        currentPlot = value;
    }

    public LootBlock getTargetLootBlock() {
        return targetLootBlock;
    }

    public void setTargetLootBlock(LootBlock value) {
        targetLootBlock = value;
    }

    public Location getLastLocation() {
        return knownLocation;
    }

    public void setLastLocation(Location value) {
        knownLocation = value;
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
}
