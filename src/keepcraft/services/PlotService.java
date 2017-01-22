package keepcraft.services;

import keepcraft.data.DataCache;
import keepcraft.data.models.*;
import org.bukkit.Location;

public class PlotService {

    public Plot createTeamPlot(User setter, Location loc, int userFaction, int radius) {
        Plot plot = createPlot(setter, loc, UserFaction.asString(userFaction) + "'s Base", radius);

        PlotProtection protection = new PlotProtection(-1);
        protection.setType(userFaction);
        protection.setAdminRadius(Plot.DEFAULT_RADIUS);
        protection.setProtectedRadius(radius);
        protection.setTriggerRadius(Plot.DEFAULT_TRIGGER_RADIUS);
        protection.setChestLevel(Plot.DEFAULT_CHEST_LEVEL);
        protection.setCapturable(false);

        plot.setProtection(protection);

        DataCache.load(Plot.class, plot);
        return plot;
    }

    public Plot createAdminPlot(User setter, Location loc, String name, int radius) {
        Plot plot = createPlot(setter, loc, name, radius);

        PlotProtection protection = new PlotProtection(-1);
        protection.setType(PlotProtection.ADMIN);
        protection.setAdminRadius(Plot.DEFAULT_RADIUS);
        protection.setProtectedRadius(radius);
        protection.setTriggerRadius(Plot.DEFAULT_TRIGGER_RADIUS);
        protection.setChestLevel(Plot.DEFAULT_CHEST_LEVEL);
        protection.setCapturable(false);

        plot.setProtection(protection);

        DataCache.load(Plot.class, plot);
        return plot;
    }

    private Plot createPlot(User setter, Location loc, String name, int radius) {
        Plot plot = new Plot();
        plot.setLocation(new WorldPoint("main", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        plot.setRadius(radius);
        plot.setName(name.trim());
        plot.setSetterId(setter.getId());
        return plot;
    }

}
