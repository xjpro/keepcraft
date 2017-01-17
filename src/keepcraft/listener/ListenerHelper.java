package keepcraft.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Location;
import keepcraft.data.models.Plot;

public abstract class ListenerHelper {

    /**
     * Comparator class for sorting a PlotData locations against a player
     * location.
     */
    private static class DistanceComparator implements Comparator<Plot> {

        private final Location location;

        public DistanceComparator(Location loc) {
            location = loc;
        }

        @Override
        public int compare(Plot arg1, Plot arg2) {
            double dist1 = arg1.distance(location);
            double dist2 = arg2.distance(location);

            if (dist1 == dist2) {
                return 0;
            } else if (dist1 < dist2) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    public static List<Plot> getNearestPlots(Location location, List<Plot> allPlots) {
        Collections.sort(allPlots, new DistanceComparator(location));
        return allPlots;
    }

    public static List<Plot> getIntersectedPlots(Location location, List<Plot> allPlots) {
        List<Plot> intersectedPlots = new ArrayList<Plot>();
        for (Plot plot : allPlots) {
            if (plot.intersectsRadius(location)) {
                intersectedPlots.add(plot);
            }
        }
        return intersectedPlots;
    }

    public static Plot getIntersectedPlot(Location location, List<Plot> allPlots) {
        List<Plot> intersectedPlots = getIntersectedPlots(location, allPlots);

        if (intersectedPlots.isEmpty()) {
            return null;
        } else if (intersectedPlots.size() == 1) {
            return intersectedPlots.get(0);
        } else {
            Plot selected = intersectedPlots.get(0);
            for (int i = 1; i < intersectedPlots.size(); i++) {
                Plot plot = intersectedPlots.get(i);
                if (plot.isSpawnProtected() || plot.isAdminProtected()) {
                    return plot;
                }
            }
            return selected;
        }
    }
}
