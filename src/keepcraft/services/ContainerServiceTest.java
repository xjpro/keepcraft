package keepcraft.services;

import keepcraft.data.Database;
import keepcraft.data.ContainerDataManager;
import keepcraft.data.MapDataManager;
import keepcraft.data.models.Container;
import keepcraft.data.models.WorldPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContainerServiceTest {

	private ContainerDataManager containerDataManager;
	private ContainerService containerService;

	@BeforeEach
	void setUp() {
		Database.deleteIfExists("keepcraft_test.db");
		Database database = new Database("keepcraft_test.db");
		containerDataManager = new ContainerDataManager(database);
		containerDataManager.putData(new Container(new WorldPoint(238, 4, -239)));
		containerService = new ContainerService(containerDataManager, new MapDataManager(database));
	}

	@Test
	void getContainers() {
		Container[] containers = containerService.getContainers().stream().toArray(Container[]::new);
		assertEquals(1, containers.length);
		assertNotNull(containers[0]);
	}

	@Test
	void getContainerExists() {
		Container container = containerService.getContainer(new WorldPoint(238, 4, -239));
		assertNotNull(container);
		assertEquals(238, container.getWorldPoint().x);
		assertEquals(4, container.getWorldPoint().y);
		assertEquals(-239, container.getWorldPoint().z);
	}

	@Test
	void getContainerNotExists() {
		Container container = containerService.getContainer(new WorldPoint(0, 0, 0));
		assertNull(container);
	}

	@Test
	void updateContainer() {
		Container container = containerService.getContainer(new WorldPoint(238, 4, -239));
		container.setPermission(Container.ContainerPermission.TEAM_VETERAN);
		container.setOutputPerHour(99);
		container.setOutputType(Container.ContainerOutputType.BASE);
		containerService.updateContainer(container);

		Container[] containers = containerDataManager.getAllData().stream().toArray(Container[]::new);
		assertEquals(1, containers.length);
		assertNotNull(containers[0]);
		assertEquals(Container.ContainerPermission.TEAM_VETERAN, containers[0].getPermission());
		assertEquals(Container.ContainerOutputType.BASE, containers[0].getOutputType());
		assertEquals(99, containers[0].getOutputPerHour());
	}

	@Test
	void createContainer() {
		Container container = containerService.createContainer(new WorldPoint(-140, 54, 399));
		assertNotNull(container);
		assertEquals(-140, container.getWorldPoint().x);
		assertEquals(54, container.getWorldPoint().y);
		assertEquals(399, container.getWorldPoint().z);

		// Check both service and data manager have stored the new loot block
		Container[] containersFromService = containerService.getContainers().stream().toArray(Container[]::new);
		assertEquals(2, containersFromService.length);
		assertEquals(containersFromService[1].getWorldPoint(), container.getWorldPoint());
		Container[] containersFromDatabase = containerDataManager.getAllData().stream().toArray(Container[]::new);
		assertEquals(2, containersFromDatabase.length);
		assertEquals(containersFromDatabase[1].getWorldPoint(), container.getWorldPoint());
	}

	@Test
	void removeContainer() {
		Container container = containerService.getContainer(new WorldPoint(238, 4, -239));
		containerService.removeContainer(container);

		// Check both service and data manager have removed the loot block
		Container[] containersFromService = containerService.getContainers().stream().toArray(Container[]::new);
		assertEquals(0, containersFromService.length);
		Container[] containersFromDatabase = containerDataManager.getAllData().stream().toArray(Container[]::new);
		assertEquals(0, containersFromDatabase.length);
	}

}