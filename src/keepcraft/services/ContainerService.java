package keepcraft.services;

import keepcraft.data.ContainerDataManager;
import keepcraft.data.MapDataManager;
import keepcraft.data.models.Container;
import keepcraft.data.models.WorldPoint;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class ContainerService {

	private final Plugin plugin;
	private final ContainerDataManager containerDataManager;
	private final MapDataManager mapDataManager;
	private Collection<Container> containers;
	private int taskId = 0;

	public ContainerService(Plugin plugin, ContainerDataManager containerDataManager, MapDataManager mapDataManager) {
		this.plugin = plugin;
		this.containerDataManager = containerDataManager;
		this.mapDataManager = mapDataManager;
		refreshCache();
	}

	public void refreshCache() {
		containers = containerDataManager.getAllData();
	}

	public Collection<Container> getContainers() {
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

	public void startDispensing() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone(ZoneId.of("America/Chicago")));
		calendar.set(Calendar.HOUR_OF_DAY, 20);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 10);

		(new Timer()).schedule(new TimerTask() {
			@Override
			public void run() {
				long mapAgeInDays = mapDataManager.getMapAgeInSeconds() / 60 / 60 / 24;
				System.out.println("map  is" + mapDataManager.getMapAgeInSeconds() + " seconds old");
				System.out.println("map  is" + mapAgeInDays + " days old");
				if (mapAgeInDays == 0) return;

				for (Container container : getOutputtingContainers()) {
					double modifier = 1;

					// Base loot output increasing daily
					if (container.getOutputType() == Container.ContainerOutputType.BASE) {
						modifier += mapAgeInDays * 0.20; // 20% more per day
					}

					container.dispense(modifier);
				}
			}
		}, calendar.getTime());
	}

	public void stopDispensing() {
		Bukkit.getScheduler().cancelTask(taskId);
	}
}
