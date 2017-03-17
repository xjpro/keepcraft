package keepcraft.services;

import keepcraft.data.Database;
import keepcraft.data.PlotDataManager;
import keepcraft.data.models.Plot;
import keepcraft.data.models.PlotProtection;
import keepcraft.data.models.UserFaction;
import keepcraft.data.models.WorldPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class PlotServiceTest {

	private PlotService plotService;
	private PlotDataManager plotDataManager;

	@BeforeEach
	void beforeEach() {
		Database.deleteIfExists("keepcraft_test.db");
		Database database = new Database("keepcraft_test.db");
		plotDataManager = new PlotDataManager(database);

		Plot red = plotDataManager.createPlot(new WorldPoint(150, 66, 150), "Red", 75);
		red.getProtection().setType(PlotProtection.FACTION_A);
		plotDataManager.updatePlot(red);

		Plot blue = plotDataManager.createPlot(new WorldPoint(-150, 64, -150), "Blue", 75);
		blue.getProtection().setType(PlotProtection.FACTION_B);
		plotDataManager.updatePlot(blue);

		Plot center = plotDataManager.createPlot(new WorldPoint(0, 70, 0), "Center", 20);
		center.getProtection().setType(PlotProtection.ADMIN);
		plotDataManager.updatePlot(center);

		plotService = new PlotService(plotDataManager);
	}

	@Test
	void getPlots() {
		Plot[] plots = plotService.getPlots().stream().toArray(Plot[]::new);

		assertEquals(3, plots.length);
		for (Plot plot : plots) {
			assertTrue(plot.getId() > 0);
		}

		Plot plot = plots[0];
		assertEquals(150, plot.getWorldPoint().x);
		assertEquals(66, plot.getWorldPoint().y);
		assertEquals(150, plot.getWorldPoint().z);
		assertEquals(PlotProtection.FACTION_A, plot.getProtection().getType());
		assertEquals("Red", plot.getName());
		assertEquals(75, plot.getRadius());
		assertEquals(plots[0].getId(), plot.getProtection().getPlotId());
		assertEquals(Plot.DEFAULT_RADIUS, plot.getProtection().getAdminRadius());

		plot = plots[1];
		assertEquals(-150, plot.getWorldPoint().x);
		assertEquals(64, plot.getWorldPoint().y);
		assertEquals(-150, plot.getWorldPoint().z);
		assertEquals(PlotProtection.FACTION_B, plot.getProtection().getType());
		assertEquals("Blue", plot.getName());
		assertEquals(75, plot.getRadius());
		assertEquals(plot.getId(), plot.getProtection().getPlotId());
		assertEquals(Plot.DEFAULT_RADIUS, plot.getProtection().getAdminRadius());

		plot = plots[2];
		assertEquals(0, plot.getWorldPoint().x);
		assertEquals(70, plot.getWorldPoint().y);
		assertEquals(0, plot.getWorldPoint().z);
		assertEquals(PlotProtection.ADMIN, plot.getProtection().getType());
		assertEquals("Center", plot.getName());
		assertEquals(20, plot.getRadius());
		assertEquals(plot.getId(), plot.getProtection().getPlotId());
		assertEquals(Plot.DEFAULT_RADIUS, plot.getProtection().getAdminRadius());
	}

	@Test
	void getPlotByName() {
		Plot plot = plotService.getPlot("Blue");
		assertTrue(plot.getId() > 0 && plot.getId() < 4);
		assertEquals("Blue", plot.getName());
		assertEquals(PlotProtection.FACTION_B, plot.getProtection().getType());
	}

	@Test
	void getPlotById() {
		Plot plot = plotService.getPlot(2);
		assertEquals(2, plot.getId());
		assertEquals("Blue", plot.getName());
		assertEquals(PlotProtection.FACTION_B, plot.getProtection().getType());
	}

	@Test
	void createTeamPlot() {
		plotService.createTeamPlot(new WorldPoint(1000, 100, -1000), UserFaction.FactionGold, 99);
		Plot plot = plotService.getPlot("Gold Castle");
		assertTrue(plot.getId() > 0);
		assertEquals("Gold Castle", plot.getName());
		assertEquals(PlotProtection.FACTION_E, plot.getProtection().getType());
		assertEquals(99, plot.getRadius());
		assertEquals(0, plot.getProtection().getKeepRadius());
	}

	@Test
	void createAdminPlot() {
		plotService.createAdminPlot(new WorldPoint(1000, 100, -1000), "Test", 27);
		Plot plot = plotService.getPlot("Test");
		assertTrue(plot.getId() > 0);
		assertEquals("Test", plot.getName());
		assertEquals(PlotProtection.ADMIN, plot.getProtection().getType());
		assertEquals(27, plot.getRadius());
	}

	@Test
	void updatePlot() {
		Plot redPlot = plotService.getPlot("Red");
		redPlot.setName("Changed name");
		redPlot.setOrderNumber(2);
		redPlot.setRadius(99);
		redPlot.setWorldPoint(new WorldPoint(99, 87,  76));
		redPlot.getProtection().setType(PlotProtection.PUBLIC);
		redPlot.getProtection().setCapturable(true);
		redPlot.getProtection().setCaptureTime(60);
		plotService.updatePlot(redPlot);

		Plot updated = plotDataManager.getAllPlots().stream().filter(plot -> plot.getId() == redPlot.getId()).findFirst().orElse(null);
		assertNotNull(updated);
		assertEquals(redPlot.getId(), updated.getId());
		assertEquals("Changed name", updated.getName());
		assertEquals(2, updated.getOrderNumber());
		assertEquals(99, updated.getRadius());
		assertEquals(new WorldPoint(99, 87,  76), updated.getWorldPoint());
		assertEquals(PlotProtection.PUBLIC, updated.getProtection().getType());
		assertEquals(true, updated.getProtection().getCapturable());
		assertEquals(60, updated.getProtection().getCaptureTime());
	}

	@Test
	void removePlot() {
		Plot centerPlot = plotService.getPlot("Center");
		plotService.removePlot(centerPlot);
		Collection<Plot> plots = plotService.getPlots();
		assertEquals(2, plots.size());

		Collection<Plot> allPlots = plotDataManager.getAllPlots();
		assertEquals(2, allPlots.size());
		assertTrue(allPlots.stream().noneMatch(plot -> plot.getName().equals("Center")));
	}

}