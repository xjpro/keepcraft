package keepcraft;

import keepcraft.data.models.FactionSpawn;
import keepcraft.data.models.UserFaction;
import keepcraft.services.FactionSpawnService;
import keepcraft.services.PlotService;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

// -5036103636176790253 spawn in big ass canyons with small sky island near 175, 175, meanwhile -175,-175 is in ocean (a very bad spawn seed)
// 794682861 huge floating island near 175, 175
public class WorldSetter {

	private final PlotService plotService;
	private final FactionSpawnService factionSpawnService;
	private final int TEAM_PLOT_RADIUS = 75;

	public WorldSetter(PlotService plotService, FactionSpawnService factionSpawnService) {
		this.plotService = plotService;
		this.factionSpawnService = factionSpawnService;
	}

	public World setupWorld(World world) {
		setBase(UserFaction.FactionRed, new Location(world, 175.5, 64, 175.5));
		setBase(UserFaction.FactionBlue, new Location(world, -175.5, 64, -175.5));
		return world;
	}

	private void setBase(int faction, Location location) {
		Keepcraft.log(String.format("Setting up %s faction...", UserFaction.getName(faction)));
		prepareBaseArea(location, TEAM_PLOT_RADIUS + 25);

		World world = location.getWorld();

		// Find good spawn location
		Location goodSpawnLocation = location.clone();
		goodSpawnLocation.setY(76);
		while (!world.getBlockAt(goodSpawnLocation.getBlockX(), goodSpawnLocation.getBlockY(), goodSpawnLocation.getBlockZ()).getType().isSolid()) {
			goodSpawnLocation.add(0, -1, 0);
		}
		goodSpawnLocation.add(0, 5, 0); // Get above terrain

		prepareSpawnArea(goodSpawnLocation);
		plotService.createTeamPlot(null, goodSpawnLocation, faction, TEAM_PLOT_RADIUS);

		// Go in air one block and center on block so spawn is not buried
		FactionSpawn spawn = new FactionSpawn(faction, goodSpawnLocation.clone().add(0, 1, 0));
		factionSpawnService.createFactionSpawn(spawn);
	}

	private void prepareBaseArea(Location center, int radius) {
		WorldHelper.inCircle(center.getBlockX(), center.getBlockZ(), 1, 135, radius, (x, y, z) -> {
			Block block = center.getWorld().getBlockAt(x, y, z);
			Material type = block.getType();

			// Prevent generated sky islands in plot area
			if (y > 90) {
				if ((type == Material.DIRT || type == Material.STONE || type == Material.GRASS || type == Material.COAL_ORE)
						&& !block.getRelative(BlockFace.DOWN).getType().isSolid()) {
					block.setType(Material.AIR);
				}
			}
			// Remove water below 63 in plot area
			else if (y < 63 && (type == Material.STATIONARY_WATER || type == Material.WATER)) {
				if (y < 58) {
					block.setType(Material.STONE);
				} else {
					block.setType(Material.GRASS);
				}
			}
		});
	}

	private void prepareSpawnArea(Location spawnLocation) {
		World world = spawnLocation.getWorld();
		Block center = world.getBlockAt(spawnLocation);

		int platformBottomY = spawnLocation.getBlockY();
		int platformTopY = platformBottomY + 3;

		WorldHelper.inCircle(spawnLocation.getBlockX(), spawnLocation.getBlockZ(), 1, 150, 3, (x, y, z) -> {
			if (y < platformBottomY || y == platformTopY) {
				// Make huge cylinder from ENDER_STONE to spawn location
				world.getBlockAt(x, y, z).setType(Material.ENDER_STONE);
				if (y == platformTopY && x == center.getX() && z == center.getZ()) {
					// Hole for beacon light
					world.getBlockAt(x, y, z).setType(Material.AIR);
				}
			} else if (y > platformTopY) {
				world.getBlockAt(x, y, z).setType(Material.AIR);
			} else {
				world.getBlockAt(x, y, z).setType(Material.AIR); // set things to air by default
				// Build hollow area
				// North wall
				world.getBlockAt(spawnLocation.getBlockX() - 1, y, spawnLocation.getBlockZ() + 2).setType(Material.ENDER_STONE);
				world.getBlockAt(spawnLocation.getBlockX() + 1, y, spawnLocation.getBlockZ() + 2).setType(Material.ENDER_STONE);
				// East wall
				world.getBlockAt(spawnLocation.getBlockX() + 2, y, spawnLocation.getBlockZ() + 1).setType(Material.ENDER_STONE);
				world.getBlockAt(spawnLocation.getBlockX() + 2, y, spawnLocation.getBlockZ() - 1).setType(Material.ENDER_STONE);
				// South wall
				world.getBlockAt(spawnLocation.getBlockX() - 1, y, spawnLocation.getBlockZ() - 2).setType(Material.ENDER_STONE);
				world.getBlockAt(spawnLocation.getBlockX() + 1, y, spawnLocation.getBlockZ() - 2).setType(Material.ENDER_STONE);
				// West wall
				world.getBlockAt(spawnLocation.getBlockX() - 2, y, spawnLocation.getBlockZ() + 1).setType(Material.ENDER_STONE);
				world.getBlockAt(spawnLocation.getBlockX() - 2, y, spawnLocation.getBlockZ() - 1).setType(Material.ENDER_STONE);

//				if(y == platformBottomY) {
//					world.getBlockAt(spawnLocation.getBlockX(), y, spawnLocation.getBlockZ() + 2).setType(Material.WOODEN_DOOR);
//					world.getBlockAt(spawnLocation.getBlockX(), y, spawnLocation.getBlockZ() - 2).setType(Material.WOODEN_DOOR);
//					world.getBlockAt(spawnLocation.getBlockX() + 2, y, spawnLocation.getBlockZ()).setType(Material.WOODEN_DOOR);
//					world.getBlockAt(spawnLocation.getBlockX() - 2, y, spawnLocation.getBlockZ()).setType(Material.WOODEN_DOOR);
//				}
			}
		});

		center.getRelative(BlockFace.DOWN).setType(Material.BEACON);
		world.dropItem(center.getLocation().add(0, 1, 0), new ItemStack(Material.WOOD_PICKAXE, 1));
	}
}
