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
	private Collection<LootBlock> lootBlocks;
	private int taskId = 0;

	public ContainerService(Plugin plugin, ContainerDataManager containerDataManager) {
		this.plugin = plugin;
		this.containerDataManager = containerDataManager;
		refreshCache();
	}

	public void refreshCache() {
		lootBlocks = containerDataManager.getAllData();
	}

	public Collection<LootBlock> getContainers() {
		return lootBlocks;
	}

	public Collection<LootBlock> getOutputtingContainers() {
		return lootBlocks.stream().filter(container -> container.getOutputPerHour() > 0).collect(Collectors.toList());
	}

	public LootBlock getContainer(WorldPoint worldPoint) {
		for (LootBlock lootBlock : getContainers()) {
			if (lootBlock.getWorldPoint().equals(worldPoint)) {
				return lootBlock;
			}
		}
		return null;
	}

	public void updateContainer(LootBlock lootBlock) {
		containerDataManager.updateData(lootBlock);
	}

	public LootBlock createContainer(WorldPoint worldPoint) {
		LootBlock lootBlock = new LootBlock(worldPoint);
		containerDataManager.putData(lootBlock);
		lootBlocks.add(lootBlock);
		return lootBlock;
	}

	public void removeContainer(LootBlock lootBlock) {
		containerDataManager.deleteData(lootBlock);
		lootBlocks.remove(lootBlock);
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
