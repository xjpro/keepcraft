package keepcraft;

import keepcraft.data.models.Container;
import keepcraft.data.models.UserFaction;
import keepcraft.data.models.WorldPoint;
import keepcraft.services.ContainerService;
import keepcraft.services.FactionSpawnService;
import keepcraft.services.PlotService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

// -5036103636176790253 spawn in big ass canyons with small sky island near 175, 175, meanwhile -175,-175 is in ocean (a very bad spawn seed)
// 794682861 huge floating island near 175, 175
// -476567279232347522 horrible spawn for blue with base deep inside a mountain
public class WorldSetter {

	private final PlotService plotService;
	private final FactionSpawnService factionSpawnService;
	private final ContainerService containerService;
	public static final int TEAM_PLOT_RADIUS = 65;
	public static final int BASE_DISTANCE_FROM_CENTER = 250;
	public static final int CENTER_SPAWN_CLEARANCE = 4;
	public static final int WORLD_BORDER = 900;

	WorldSetter(PlotService plotService, FactionSpawnService factionSpawnService, ContainerService containerService) {
		this.plotService = plotService;
		this.factionSpawnService = factionSpawnService;
		this.containerService = containerService;
	}

	World setupWorld(World world) {
		boolean found = false;
		Location center = new Location(world, 0, 64, 0);
		Location redBase;
		Location blueBase;
		do {
			redBase = center.clone().add(-BASE_DISTANCE_FROM_CENTER, 0, 0);
			blueBase = center.clone().add(BASE_DISTANCE_FROM_CENTER, 0, 0);

			if (isAcceptableBiome(world.getBiome(redBase.getBlockX(), redBase.getBlockZ())) &&
					isAcceptableBiome(world.getBiome(blueBase.getBlockX(), blueBase.getBlockZ()))) {
				found = true;
			} else {
				Keepcraft.log(String.format("Unacceptable base biomes, going up +%s z units", BASE_DISTANCE_FROM_CENTER));
				center.add(0, 0, BASE_DISTANCE_FROM_CENTER);
			}
		} while (!found);

		world.setSpawnLocation(center.getBlockX(), world.getHighestBlockYAt(center), center.getBlockZ());
		world.getWorldBorder().setCenter(center);
		world.getWorldBorder().setSize(WORLD_BORDER);

		setBase(UserFaction.RED, redBase);
		setBase(UserFaction.BLUE, blueBase);
		prepareCenterTrench(center);
		return world;
	}

	private void setBase(UserFaction faction, Location location) {
		Keepcraft.log(String.format("Setting up %s faction...", faction.getName()));

		World world = location.getWorld();

		// Find good spawn location
		Location goodSpawnLocation = location.clone();
		goodSpawnLocation.setY(76);
		while (!world.getBlockAt(goodSpawnLocation.getBlockX(), goodSpawnLocation.getBlockY(), goodSpawnLocation.getBlockZ()).getType().isSolid()) {
			goodSpawnLocation.add(0, -1, 0);
		}
		goodSpawnLocation.add(0, 5, 0); // Get above terrain

		prepareBaseArea(goodSpawnLocation, TEAM_PLOT_RADIUS + 15);
		prepareSpawnArea(goodSpawnLocation);
		plotService.createTeamPlot(new WorldPoint(goodSpawnLocation), faction, TEAM_PLOT_RADIUS);

		// Go in air one block and center on block so spawn is not buried
		factionSpawnService.createFactionSpawn(faction, new WorldPoint(goodSpawnLocation.clone().add(0, 1, 0)));
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
			// Fill in lower areas with bedrock
			else if (y < center.getBlockY() - 12) {
				block.setType(Material.BEDROCK);
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
		int platformTopY = platformBottomY + CENTER_SPAWN_CLEARANCE;

		WorldHelper.inSquare(spawnLocation.getBlockX(), spawnLocation.getBlockZ(), 1, 150, 2, (x, y, z) -> {
			if (y < platformBottomY || y == platformTopY) {
				// Make huge cylinder from END_BRICKS to spawn location
				if (x == center.getX() && z == center.getZ()) {
					if (y == platformTopY) {
						// Hole above
						world.getBlockAt(x, y, z).setType(Material.AIR);
					} else {
						// Thread of blocks for the win condition
						world.getBlockAt(x, y, z).setType(Material.DIAMOND_BLOCK);
					}
				} else {
					// Not in center, make ender stone
					world.getBlockAt(x, y, z).setType(Material.END_BRICKS);
				}
			} else if (y > platformTopY) {
				world.getBlockAt(x, y, z).setType(Material.AIR);
			} else {
				world.getBlockAt(x, y, z).setType(Material.AIR); // set things to air by default
				// Build hollow area
				// North wall
				world.getBlockAt(spawnLocation.getBlockX() - 2, y, spawnLocation.getBlockZ() + 2).setType(Material.END_BRICKS);
				world.getBlockAt(spawnLocation.getBlockX() - 1, y, spawnLocation.getBlockZ() + 2).setType(Material.END_BRICKS);
				world.getBlockAt(spawnLocation.getBlockX() + 1, y, spawnLocation.getBlockZ() + 2).setType(Material.END_BRICKS);
				world.getBlockAt(spawnLocation.getBlockX() + 2, y, spawnLocation.getBlockZ() + 2).setType(Material.END_BRICKS);
				// East wall
				world.getBlockAt(spawnLocation.getBlockX() + 2, y, spawnLocation.getBlockZ() + 2).setType(Material.END_BRICKS);
				world.getBlockAt(spawnLocation.getBlockX() + 2, y, spawnLocation.getBlockZ() + 1).setType(Material.END_BRICKS);
				world.getBlockAt(spawnLocation.getBlockX() + 2, y, spawnLocation.getBlockZ() - 1).setType(Material.END_BRICKS);
				world.getBlockAt(spawnLocation.getBlockX() + 2, y, spawnLocation.getBlockZ() - 2).setType(Material.END_BRICKS);
				// South wall
				world.getBlockAt(spawnLocation.getBlockX() - 2, y, spawnLocation.getBlockZ() - 2).setType(Material.END_BRICKS);
				world.getBlockAt(spawnLocation.getBlockX() - 1, y, spawnLocation.getBlockZ() - 2).setType(Material.END_BRICKS);
				world.getBlockAt(spawnLocation.getBlockX() + 1, y, spawnLocation.getBlockZ() - 2).setType(Material.END_BRICKS);
				world.getBlockAt(spawnLocation.getBlockX() + 2, y, spawnLocation.getBlockZ() - 2).setType(Material.END_BRICKS);
				// West wall
				world.getBlockAt(spawnLocation.getBlockX() - 2, y, spawnLocation.getBlockZ() + 2).setType(Material.END_BRICKS);
				world.getBlockAt(spawnLocation.getBlockX() - 2, y, spawnLocation.getBlockZ() + 1).setType(Material.END_BRICKS);
				world.getBlockAt(spawnLocation.getBlockX() - 2, y, spawnLocation.getBlockZ() - 1).setType(Material.END_BRICKS);
				world.getBlockAt(spawnLocation.getBlockX() - 2, y, spawnLocation.getBlockZ() - 2).setType(Material.END_BRICKS);

//				if(y == platformBottomY) {
//					world.getBlockAt(spawnLocation.getBlockX(), y, spawnLocation.getBlockZ() + 2).setType(Material.WOODEN_DOOR);
//					world.getBlockAt(spawnLocation.getBlockX(), y, spawnLocation.getBlockZ() - 2).setType(Material.WOODEN_DOOR);
//					world.getBlockAt(spawnLocation.getBlockX() + 2, y, spawnLocation.getBlockZ()).setType(Material.WOODEN_DOOR);
//					world.getBlockAt(spawnLocation.getBlockX() - 2, y, spawnLocation.getBlockZ()).setType(Material.WOODEN_DOOR);
//				}
			}
		});


		// Outputting container at center of base
		Block chestBlock = center.getRelative(BlockFace.DOWN);
		chestBlock.setType(Material.CHEST);

		// Create beacon
		Block beaconBlock = center.getRelative(BlockFace.DOWN, 2);
		beaconBlock.setType(Material.BEACON);
		beaconBlock.getRelative(0, -1, 1).setType(Material.IRON_BLOCK);
		beaconBlock.getRelative(1, -1, 1).setType(Material.IRON_BLOCK);
		beaconBlock.getRelative(1, -1, 0).setType(Material.IRON_BLOCK);
		beaconBlock.getRelative(1, -1, -1).setType(Material.IRON_BLOCK);
		beaconBlock.getRelative(0, -1, -1).setType(Material.IRON_BLOCK);
		beaconBlock.getRelative(-1, -1, -1).setType(Material.IRON_BLOCK);
		beaconBlock.getRelative(-1, -1, 0).setType(Material.IRON_BLOCK);
		beaconBlock.getRelative(-1, -1, 1).setType(Material.IRON_BLOCK);

		Container baseLootContainer = containerService.createContainer(new WorldPoint(chestBlock.getLocation()));
		baseLootContainer.setOutputType(Container.ContainerOutputType.BASE);
		baseLootContainer.setOutputPerHour(7);
		baseLootContainer.setPermission(Container.ContainerPermission.TEAM_VETERAN);
		containerService.updateContainer(baseLootContainer);

		// Drop pick axe so players can dig out if necessary
		world.dropItem(center.getLocation().add(0, 1, 0), new ItemStack(Material.WOOD_PICKAXE, 1));
	}

	private void prepareCenterTrench(Location center) {
		Random random = new Random();
		World world = center.getWorld();
		for (int z = center.getBlockZ() - (WORLD_BORDER / 2); z <= center.getBlockZ() + (WORLD_BORDER / 2); z++) {
			for (int x = center.getBlockX() - 5; x <= center.getBlockX() + 5; x++) {
				if (x == 0 || x % 5 != 0 || random.nextDouble() > 0.15) {
					for (int y = 10; y < 150; y++) {
						world.getBlockAt(x, y, z).setType(Material.AIR);
					}
				}
			}
		}
	}

	private boolean isAcceptableBiome(Biome biome) {
		switch (biome) {
			case FOREST:
			case FOREST_HILLS:
			case BIRCH_FOREST:
			case BIRCH_FOREST_HILLS:
			case ROOFED_FOREST:
			case DESERT:
			case DESERT_HILLS:
			case SAVANNA:
			case PLAINS:
			case ICE_FLATS: // ice plains
			case TAIGA:
			case REDWOOD_TAIGA:
			case TAIGA_COLD:
			case TAIGA_HILLS:
			case MESA:
				return true;
		}
		return false;
	}
}
