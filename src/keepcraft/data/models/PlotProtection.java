package keepcraft.data.models;

import keepcraft.services.ChatService;

public class PlotProtection {

    public final static int ADMIN = 900;
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

    // A plot has a radius but protection within that radius can vary:
    // Radius of plot which is "protected", in the usual case this means only team member modifications
    private double protectedRadius;
    // Radius of plot which is the "keep", unused currently this would allow an inner area that only higher level team members can modify
    private double keepRadius;
    // Radius of plot which only admins can modify, in the usual case this is a small area at the center that protects the spawn point
    private double adminRadius;
    // Radius of plot in which a player must be to begin capturing the plot, if captures are allowed
    private double captureRadius;

    // Bool that marks if plot can be captured
    private boolean capturable;
    // How long in seconds is takes to capture the plot
    private int captureSeconds;
    private int captureEffect;

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

    public double getProtectedRadius() {
        return protectedRadius;
    }

    public void setProtectedRadius(double value) {
        protectedRadius = value;
    }

    public double getKeepRadius() {
        return keepRadius;
    }

    public void setKeepRadius(double value) {
        keepRadius = value;
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
