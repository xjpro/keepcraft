package keepcraft.services;

import keepcraft.data.PlotDataManager;
import keepcraft.data.models.*;
import org.bukkit.Location;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PlotService {

	private final PlotDataManager plotDataManager;
	private Collection<Plot> plots;

	public PlotService(PlotDataManager plotDataManager) {
		this.plotDataManager = plotDataManager;
		refreshCache();
	}

	public void refreshCache() {
		plots = plotDataManager.getAllPlots();
	}

	public Collection<Plot> getPlots() {
		return plots;
	}

	public Plot getPlot(String name) {
		return plots.stream().filter(plot -> plot.getName().equals(name)).findFirst().orElse(null);
	}

	public Plot getPlot(int id) {
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

	public int getUnusedOrderNumber() {
		int orderNumber = 1;
		List<Plot> plotsByOrderNumber = plots.stream()
				.sorted(Comparator.comparing(Plot::getOrderNumber))
				.collect(Collectors.toList());
		for (Plot plot : plotsByOrderNumber) {
			if (plot.getOrderNumber() == orderNumber) orderNumber++;
		}
		return orderNumber;
	}

	public void removePlot(Plot plot) {
		plotDataManager.deletePlot(plot);
		plots.remove(plot);
	}

	public void updatePlot(Plot plot) {
		plotDataManager.updatePlot(plot);
	}

	public Plot createTeamPlot(WorldPoint worldPoint, UserTeam userTeam, int radius) {
		String plotName = String.format("%s Castle", userTeam.getName());

		Plot plot = plotDataManager.createPlot(worldPoint, plotName, radius);
		PlotProtection protection = plot.getProtection();
		protection.setType(userTeam.getId());
		protection.setAdminRadius(3);

		plotDataManager.updatePlot(plot);
		refreshCache();
		return getPlot(plotName);
	}

	public Plot createAdminPlot(WorldPoint worldPoint, String plotName, int radius) {
		Plot plot = plotDataManager.createPlot(worldPoint, plotName, radius);
		PlotProtection protection = plot.getProtection();
		protection.setType(PlotProtection.ADMIN);
		protection.setAdminRadius(Plot.DEFAULT_RADIUS);

		plotDataManager.updatePlot(plot);
		refreshCache();
		return getPlot(plotName);
	}

	public Plot createOutpostPlot(WorldPoint worldPoint, User user) {
		int unusedOrderNumber = getUnusedOrderNumber();
		String plotName = String.format("%s Outpost %s", user.getTeam().getName(), unusedOrderNumber);
		Plot plot = plotDataManager.createPlot(worldPoint, plotName, Plot.DEFAULT_OUTPOST_RADIUS);
		plot.setCreator(user.getName());
		plot.setOrderNumber(unusedOrderNumber);
		PlotProtection protection = plot.getProtection();
		protection.setType(user.getTeam().getId());
		protection.setCapturable(true);
		protection.setAdminRadius(0);

		plotDataManager.updatePlot(plot);
		refreshCache();
		return getPlot(plotName);
	}
}
