package keepcraft.data.models;

import keepcraft.services.ChatService;

public class PlotProtection {

    public final static int ADMIN = 900;
    public final static int SPAWN = 800;
    public final static int EVENT = 700;
    public final static int PRIVATE = 500;
    public final static int PROTECTED = 400;
    public final static int FACTION_B = UserFaction.FactionBlue;
    public final static int FACTION_A = UserFaction.FactionRed;
    public final static int FACTION_C = UserFaction.FactionGreen;
    public final static int FACTION_E = UserFaction.FactionGold;
    public final static int PUBLIC = 0;

    private final int plotId;
    private int type;
    private double protectedRadius;
    private double partialRadius;
    private double adminRadius;
    private double captureRadius;
    private boolean capturable;
    private int captureSeconds;
    private int captureEffect;
    private WorldPoint spawnLocation;

    private boolean captureInProgress = false;

    public PlotProtection(int plotId) {
        this.plotId = plotId;
    }

    public int getPlotId() {
        return plotId;
    }

    public int getType() {
        return type;
    }

    public void setType(int value) {
        type = value;
    }

    public boolean isSpawn() {
        return type == PlotProtection.SPAWN;
    }

    public double getProtectedRadius() {
        return protectedRadius;
    }

    public void setProtectedRadius(double value) {
        protectedRadius = value;
    }

    public double getPartialRadius() {
        return partialRadius;
    }

    public void setPartialRadius(double value) {
        partialRadius = value;
    }

    public double getAdminRadius() {
        return adminRadius;
    }

    public void setAdminRadius(double value) {
        adminRadius = value;
    }

    public double getTriggerRadius() {
        return captureRadius;
    }

    public void setTriggerRadius(double value) {
        captureRadius = value;
    }

    public boolean getCapturable() {
        return capturable;
    }

    public boolean isCapturable() {
        return capturable;
    }

    public void setCapturable(boolean value) {
        capturable = value;
    }

    public int getCaptureTime() {
        return captureSeconds;
    }

    public void setCaptureTime(int value) {
        captureSeconds = value;
    }

    public boolean isCaptureInProgress() {
        return captureInProgress;
    }

    public void setCaptureInProgress(boolean value) {
        captureInProgress = value;
    }

    public String asString() {
        return PlotProtection.asString(type);
    }

    public static String asString(int protection) {
        switch (protection) {
            case ADMIN:
                return ChatService.NameAdmin + "Admin";
            case SPAWN:
                return ChatService.NameOther + "Spawn";
            case EVENT:
                return ChatService.NameAdmin + "Event";
            case PRIVATE:
                return "Private";
            case PROTECTED:
                return "Protected";
            case FACTION_B:
                return ChatService.NameBlue + UserFaction.asString(FACTION_B);
            case FACTION_A:
                return ChatService.NameRed + UserFaction.asString(FACTION_A);
            case FACTION_C:
                return ChatService.NameGreen + UserFaction.asString(FACTION_C);
            case FACTION_E:
                return ChatService.NameGold + UserFaction.asString(FACTION_E);
            case PUBLIC:
                return "Public";
            default:
                return "Unknown";
        }
    }

}
