package keepcraft.services;

import keepcraft.data.PlotDataManager;
import keepcraft.data.models.*;
import org.bukkit.Location;

import java.util.Collection;

public class PlotService {

    private PlotDataManager plotDataManager = new PlotDataManager();
    private Collection<Plot> plots;

    PlotService() {
        refreshCache();
    }

    void refreshCache() {
        plots = plotDataManager.getAllData();
    }

    public Collection<Plot> getPlots() {
        return plots;
    }

    public Plot getPlot(String name) {
        for(Plot plot : plots) {
            if (plot.getName().equals(name)) {
                return plot;
            }
        }
        return null;
    }

    public Plot getPlot(Integer id) {
        for(Plot plot : plots) {
            if (plot.getId() == id) {
                return plot;
            }
        }
        return null;
    }

    public void removePlot(Plot plot) {
        plotDataManager.deleteData(plot);
        plots.remove(plot);
    }

    public void updatePlot(Plot plot) {
        plotDataManager.putData(plot);
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
