package keepcraft.services;

import keepcraft.data.ContainerDataManager;
import keepcraft.data.MapDataManager;
import keepcraft.data.models.Container;
import keepcraft.data.models.WorldPoint;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class ContainerService {

	private final ContainerDataManager containerDataManager;
	private final MapDataManager mapDataManager;
	private Collection<Container> containers;
	private final Timer timer;

	public ContainerService(ContainerDataManager containerDataManager, MapDataManager mapDataManager) {
		this.containerDataManager = containerDataManager;
		this.mapDataManager = mapDataManager;
		this.timer = new Timer();
		refreshCache();
	}

	public void refreshCache() {
		containers = containerDataManager.getAllData();
	}

	Collection<Container> getContainers() {
		return containers;
	}

	public Collection<Container> getOutputtingContainers() {
		return containers.stream().filter(container -> container.getOutputPerHour() > 0).collect(Collectors.toList());
	}

	public Container getContainer(WorldPoint worldPoint) {
		for (Container container : getContainers()) {
			if (container.getWorldPoint().equals(worldPoint)) {
				return container;
			}
		}
		return null;
	}

	public void updateContainer(Container container) {
		containerDataManager.updateData(container);
	}

	public Container createContainer(WorldPoint worldPoint) {
		Container container = new Container(worldPoint);
		containerDataManager.putData(container);
		containers.add(container);
		return container;
	}

	public void removeContainer(Container container) {
		containerDataManager.deleteData(container);
		containers.remove(container);
	}

	public void dispenseAllContainers() {
		long mapAgeInDays = mapDataManager.getMapAgeInDays();
		getOutputtingContainers().stream()
				.filter(container -> container.getOutputType() == Container.ContainerOutputType.BASE || container.getOutputType() == Container.ContainerOutputType.OUTPOST)
				.forEach(container -> {
					double modifier = 1.0;
					if (container.getOutputType() == Container.ContainerOutputType.BASE) {
						modifier = 1.0 + (mapAgeInDays * 0.2);
					}
					container.dispense(modifier);
				});
	}

	public void startDispensing() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone(ZoneId.of("America/Chicago")));

		// Containers output at 11pm (end of raiding time)
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				dispenseAllContainers();
			}
		}, calendar.getTime());
	}

	public void stopDispensing() {
		timer.cancel();
	}
}
