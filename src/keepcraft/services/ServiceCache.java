package keepcraft.services;

// Seems dumb but this allows us to call SQL Lite a lot less often by enabling services to cache
// data internally and return it
public abstract class ServiceCache {

    private static UserService userService = new UserService();
    private static PlotService plotService = new PlotService();
    private static FactionSpawnService factionSpawnService = new FactionSpawnService();

    public static UserService getUserService() {
        return userService;
    }

    public static PlotService getPlotService() {
        return plotService;
    }

    public static FactionSpawnService getFactionSpawnService() {
        return factionSpawnService;
    }

    public static void refreshCaches() {
        userService.refreshCache();
        plotService.refreshCache();
        factionSpawnService.refreshCache();
    }
}
