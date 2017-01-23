package keepcraft.services;

import keepcraft.data.PlotDataManager;
import keepcraft.data.models.*;
import org.bukkit.Location;

import java.util.Collection;

public class PlotService {

    private PlotDataManager plotDataManager = new PlotDataManager();
    private Collection<Plot> plots;

    public PlotService() {
        plots = plotDataManager.getAllData().values();
    }

    public Collection<Plot> getPlots() {
        return plots;
    }

    public Plot getPlot(String name) {
        return plots.stream()
                .filter(plot -> plot.getName().equals(name))
                .findFirst()
                .get();
    }

    public Plot getPlot(Integer id) {
        return plots.stream()
                .filter(plot -> plot.getId() == id)
                .findFirst()
                .get();
    }

    public Plot createTeamPlot(User setter, Location loc, int userFaction, int radius) {
        Plot plot = createPlot(setter, loc, UserFaction.asString(userFaction) + "'s Base", radius);

        PlotProtection protection = new PlotProtection(-1);
        protection.setType(userFaction);
        protection.setAdminRadius(Plot.DEFAULT_RADIUS);
        protection.setProtectedRadius(radius);
        protection.setTriggerRadius(Plot.DEFAULT_TRIGGER_RADIUS);
        protection.setCapturable(false);
        plot.setProtection(protection);

        plotDataManager.putData(plot);
        plots.add(plot);

        return plot;
    }

    public Plot createAdminPlot(User setter, Location loc, String name, int radius) {
        Plot plot = createPlot(setter, loc, name, radius);

        PlotProtection protection = new PlotProtection(-1);
        protection.setType(PlotProtection.ADMIN);
        protection.setAdminRadius(Plot.DEFAULT_RADIUS);
        protection.setProtectedRadius(radius);
        protection.setTriggerRadius(Plot.DEFAULT_TRIGGER_RADIUS);
        protection.setCapturable(false);
        plot.setProtection(protection);

        plotDataManager.putData(plot);
        plots.add(plot);

        return plot;
    }

    private Plot createPlot(User setter, Location loc, String name, int radius) {
        Plot plot = new Plot();
        plot.setWorldPoint(new WorldPoint(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        plot.setRadius(radius);
        plot.setName(name.trim());
        //plot.setSetterId(setter.getId()); todo use this
        return plot;
    }

}
