package keepcraft.services;

import keepcraft.data.ContainerDataManager;
import keepcraft.data.models.LootBlock;
import keepcraft.data.models.WorldPoint;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.stream.Collectors;

public class ContainerService {

	private final Plugin plugin;
	private final ContainerDataManager containerDataManager;
	private Collection<LootBlock> containers;
	private int taskId = 0;

	public ContainerService(Plugin plugin, ContainerDataManager containerDataManager) {
		this.plugin = plugin;
		this.containerDataManager = containerDataManager;
		refreshCache();
	}

	public void refreshCache() {
		containers = containerDataManager.getAllData();
	}

	public Collection<LootBlock> getContainers() {
		return containers;
	}

	public Collection<LootBlock> getOutputtingContainers() {
		return containers.stream().filter(container -> container.getOutputPerHour() > 0).collect(Collectors.toList());
	}

	public LootBlock getContainer(WorldPoint worldPoint) {
		for (LootBlock container : getContainers()) {
			if (container.getWorldPoint().equals(worldPoint)) {
				return container;
			}
		}
		return null;
	}

	public void updateContainer(LootBlock container) {
		containerDataManager.updateData(container);
	}

	public LootBlock createContainer(WorldPoint worldPoint) {
		LootBlock container = new LootBlock(worldPoint);
		containerDataManager.putData(container);
		containers.add(container);
		return container;
	}

	public void removeContainer(LootBlock container) {
		containerDataManager.deleteData(container);
		containers.remove(container);
	}

	public void startDispensing() {
		if (plugin == null) return;

		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
			for (LootBlock container : getOutputtingContainers()) {
				container.dispense();
			}
		}, 1200, 1200);
	}

	public void stopDispensing() {
		Bukkit.getScheduler().cancelTask(taskId);
	}
}
