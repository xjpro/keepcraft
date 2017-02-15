package keepcraft.services;

import keepcraft.data.Database;
import keepcraft.data.PlotDataManager;
import keepcraft.data.models.Plot;
import keepcraft.data.models.PlotProtection;
import keepcraft.data.models.UserFaction;
import keepcraft.data.models.WorldPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlotServiceTest {

	private PlotService plotService;

	@BeforeEach
	void beforeEach() {
		deleteIfExists("keepcraft_test.db");
		Database database = new Database("keepcraft_test.db");
		PlotDataManager plotDataManager = new PlotDataManager(database);

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

	@AfterEach
	void tearDown() {
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
		assertEquals(75, plot.getProtection().getProtectedRadius());
		assertEquals(Plot.DEFAULT_RADIUS, plot.getProtection().getAdminRadius());

		plot = plots[1];
		assertEquals(-150, plot.getWorldPoint().x);
		assertEquals(64, plot.getWorldPoint().y);
		assertEquals(-150, plot.getWorldPoint().z);
		assertEquals(PlotProtection.FACTION_B, plot.getProtection().getType());
		assertEquals("Blue", plot.getName());
		assertEquals(75, plot.getRadius());
		assertEquals(plot.getId(), plot.getProtection().getPlotId());
		assertEquals(75, plot.getProtection().getProtectedRadius());
		assertEquals(Plot.DEFAULT_RADIUS, plot.getProtection().getAdminRadius());

		plot = plots[2];
		assertEquals(0, plot.getWorldPoint().x);
		assertEquals(70, plot.getWorldPoint().y);
		assertEquals(0, plot.getWorldPoint().z);
		assertEquals(PlotProtection.ADMIN, plot.getProtection().getType());
		assertEquals("Center", plot.getName());
		assertEquals(20, plot.getRadius());
		assertEquals(plot.getId(), plot.getProtection().getPlotId());
		assertEquals(20, plot.getProtection().getProtectedRadius());
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
		Plot plot = plotService.getPlot("Gold Base");
		assertTrue(plot.getId() > 0);
		assertEquals("Gold Base", plot.getName());
		assertEquals(PlotProtection.FACTION_E, plot.getProtection().getType());
		assertEquals(99, plot.getRadius());
		assertEquals(99, plot.getProtection().getProtectedRadius());
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

	private void deleteIfExists(String pathname) {
		try {
			File file = new File(pathname);
			file.delete();
		} catch (Exception e) {
			// don't care
		}
	}

}