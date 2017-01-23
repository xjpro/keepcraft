package keepcraft.data.models;

import java.util.Random;
import org.bukkit.ChatColor;
import keepcraft.Chat;

public abstract class UserFaction {

    private final static Random Random = new Random();
    public final static int FactionRed = 100;
    public final static int FactionBlue = 200;
    public final static int FactionGreen = 300;
    public final static int FactionGold = 50;

    public static ChatColor getChatColor(int faction) {
        switch (faction) {
            case FactionRed:
                return Chat.NameRed;
            case FactionBlue:
                return Chat.NameBlue;
            case FactionGreen:
                return Chat.NameGreen;
            case FactionGold:
                return Chat.NameGold;
            default:
                return Chat.NameOther;
        }
    }

    public static String getName(int faction) {
        switch (faction) {
            case FactionRed:
                return "Red";
            case FactionBlue:
                return "Blue";
            case FactionGreen:
                return "Green";
            case FactionGold:
                return "Gold";
            default:
                return "Other";
        }
    }

    public static int getRandomFaction() {
        return (1 + Random.nextInt(3)) * 100;
    }

    public static int getSmallestFaction(int redCount, int blueCount, int greenCount) {
        if (redCount > blueCount) // blue is one of the smaller
        {
            if (blueCount > greenCount) // green is smallest
            {
                return FactionGreen;
            }
            return FactionBlue;
        } else // red is one of the smaller
        {
            if (redCount > greenCount) // green is the smallest
            {
                return FactionGreen;
            }
            return FactionRed;
        }
    }

    public static String asString(int faction) {
        return UserFaction.getName(faction);
    }

    public static String asColoredString(int faction) {
        return getChatColor(faction) + asString(faction);
    }
}
