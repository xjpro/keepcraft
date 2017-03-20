package keepcraft.data.models;

import java.util.Arrays;
import java.util.Random;

import keepcraft.services.ChatService;
import org.bukkit.ChatColor;

public enum UserTeam {

	RED(100, "Red", ChatService.NameRed),
	BLUE(200, "Blue", ChatService.NameBlue),
	GREEN(300, "Green", ChatService.NameGreen),
	GOLD(50, "Gold", ChatService.NameGold);

	private final static Random Random = new Random();
	private final int id;
	private final String name;
	private final ChatColor chatColor;

	UserTeam(int id, String name, ChatColor chatColor) {
		this.id = id;
		this.name = name;
		this.chatColor = chatColor;
	}

	public static UserTeam getFaction(int id) {
		return Arrays.stream(UserTeam.values()).filter(faction -> faction.getId() == id).findFirst().orElse(null);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ChatColor getChatColor() {
		return chatColor;
	}

	public String getChatColoredNamed() {
		return getChatColor() + getName();
	}

	public static ChatColor getChatColor(int faction) {
		UserTeam userTeam = getFaction(faction);
		return userTeam != null ? userTeam.getChatColor() : ChatService.NameOther;
	}

	public static String getName(int faction) {
		UserTeam userTeam = getFaction(faction);
		return userTeam != null ? userTeam.getName() : "Other";
	}

	public static int getRandomFaction() {
		return (1 + Random.nextInt(2)) * 100;
	}

	public static int getSmallestFaction(int redCount, int blueCount, int greenCount) {

		if (redCount > blueCount) {
			// blue is one of the smaller
			if (blueCount > greenCount) {
				// green is smallest
				return GREEN.getId();
			}
			return BLUE.getId();
		} else {
			// red is one of the smaller
			if (redCount > greenCount) { // green is the smallest
				return GREEN.getId();
			}
			return RED.getId();
		}
	}

	public static String asString(int faction) {
		return UserTeam.getName(faction);
	}

	public static String asColoredString(int faction) {
		return getChatColor(faction) + asString(faction);
	}
}
