package keepcraft.services;

import keepcraft.data.ContainerDataManager;
import keepcraft.data.models.Container;
import keepcraft.data.models.WorldPoint;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.stream.Collectors;

public class ContainerService {

	private final Plugin plugin;
	private final ContainerDataManager containerDataManager;
	private Collection<Container> containers;
	private int taskId = 0;

	public ContainerService(Plugin plugin, ContainerDataManager containerDataManager) {
		this.plugin = plugin;
		this.containerDataManager = containerDataManager;
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
		if (plugin == null) return;

		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
			for (Container container : getOutputtingContainers()) {
				container.dispense();
			}
		}, 1200, 1200);
	}

	public void stopDispensing() {
		Bukkit.getScheduler().cancelTask(taskId);
	}
}
