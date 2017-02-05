package keepcraft.services;

import keepcraft.data.PlotDataManager;
import keepcraft.data.models.*;
import org.bukkit.Location;

import java.util.Collection;

public class PlotService {

    private final PlotDataManager plotDataManager;
    private Collection<Plot> plots;

    public PlotService(PlotDataManager plotDataManager) {
        this.plotDataManager = plotDataManager;
        refreshCache();
    }

    public void refreshCache() {
        plots = plotDataManager.getAllData();
    }

    public Collection<Plot> getPlots() {
        return plots;
    }

    public Plot getPlot(String name) {
        return plots.stream().filter(plot -> plot.getName().equals(name)).findFirst().orElse(null);
    }

    public Plot getPlot(Integer id) {
        return plots.stream().filter(plot -> plot.getId() == id).findFirst().orElse(null);
    }

    public Plot getIntersectedPlot(Location location) {
        Plot[] intersectedPlots = plots.stream().filter(plot -> plot.isInRadius(location)).toArray(Plot[]::new);

        if (intersectedPlots.length == 0) {
            return null;
        } else if (intersectedPlots.length == 1) {
            return intersectedPlots[0];
        } else {
            Plot selected = intersectedPlots[0];
            for (int i = 1; i < intersectedPlots.length; i++) {
                Plot plot = intersectedPlots[i];
                // When we have multiple plots intersected, prioritize admin/spawn plots
                if (plot.isAdminProtected()) {
                    return plot;
                }
            }
            return selected;
        }
    }

    public void removePlot(Plot plot) {
        plotDataManager.deleteData(plot);
        plots.remove(plot);
    }

    public void updatePlot(Plot plot) {
        plotDataManager.updateData(plot);
    }

    public Plot createTeamPlot(User setter, Location loc, int userFaction, int radius) {
        Plot plot = createPlot(setter, loc, UserFaction.asString(userFaction) + "'s Base", radius);

        PlotProtection protection = new PlotProtection(-1);
        protection.setType(userFaction);
        protection.setAdminRadius(3);
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

    private Plot createPlot(User setter, Location location, String name, int radius) {
        Plot plot = new Plot();
        plot.setWorldPoint(new WorldPoint(location));
        plot.setRadius(radius);
        plot.setName(name.trim());
        //plot.setSetterId(setter.getId()); todo use this
        return plot;
    }
}
