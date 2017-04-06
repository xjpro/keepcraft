package keepcraft.services;

import keepcraft.Keepcraft;
import keepcraft.WorldHelper;
import keepcraft.data.models.Container;
import keepcraft.data.models.UserTeam;
import keepcraft.data.models.WorldPoint;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

// -5036103636176790253 spawn in big ass canyons with small sky island near 175, 175, meanwhile -175,-175 is in ocean (a very bad spawn seed)
// 794682861 huge floating island near 175, 175
// -476567279232347522 horrible spawn for blue with base deep inside a mountain
public class WorldModifierService {

	private final PlotService plotService;
	private final FactionSpawnService factionSpawnService;
	private final ContainerService containerService;
	public static final int TEAM_PLOT_RADIUS = 65;
	public static final int BASE_DISTANCE_FROM_CENTER = 250;
	private static final int CENTER_SPAWN_CLEARANCE = 4;
	private static final int CENTER_SPAWN_HEIGHT = 15;
	private static final int WORLD_BORDER = 900;
	private static final Random random = new Random();

	public WorldModifierService(PlotService plotService, FactionSpawnService factionSpawnService, ContainerService containerService) {
		this.plotService = plotService;
		this.factionSpawnService = factionSpawnService;
		this.containerService = containerService;
	}

	public World setupWorld(World world) {
		boolean found = false;
		Location center = new Location(world, 0, 64, 0);
		Location redBase;
		Location blueBase;
		do {
			redBase = center.clone().add(-BASE_DISTANCE_FROM_CENTER, 0, 0);
			blueBase = center.clone().add(BASE_DISTANCE_FROM_CENTER, 0, 0);
			int lowestYInRedCircle = WorldHelper.getLowestYInCircle(world, redBase.getBlockX(), redBase.getBlockZ(), TEAM_PLOT_RADIUS + 10);
			int lowestYInBlueCircle = WorldHelper.getLowestYInCircle(world, blueBase.getBlockX(), blueBase.getBlockZ(), TEAM_PLOT_RADIUS + 10);
			redBase.setY(lowestYInRedCircle + CENTER_SPAWN_HEIGHT);
			blueBase.setY(lowestYInBlueCircle + CENTER_SPAWN_HEIGHT);

			int minimumY = world.getSeaLevel() - 10;
			if (isAcceptableBiome(world.getBiome(redBase.getBlockX(), redBase.getBlockZ())) && lowestYInRedCircle > minimumY &&
					isAcceptableBiome(world.getBiome(blueBase.getBlockX(), blueBase.getBlockZ())) && lowestYInBlueCircle > minimumY) {
				found = true;
			} else {
				Keepcraft.log(String.format("Unacceptable base biomes, going up +%s z units", BASE_DISTANCE_FROM_CENTER));
				center.add(0, 0, BASE_DISTANCE_FROM_CENTER);
			}
		} while (!found);

		world.setSpawnLocation(center.getBlockX(), world.getHighestBlockYAt(center), center.getBlockZ());
		world.getWorldBorder().setCenter(center);
		world.getWorldBorder().setSize(WORLD_BORDER);

		setBase(UserTeam.RED, redBase);
		setBase(UserTeam.BLUE, blueBase);
		prepareCenterTrench(center);
		return world;
	}

	private void setBase(UserTeam userTeam, Location location) {
		Keepcraft.log(String.format("Setting up %s team...", userTeam.getName()));

		prepareBaseArea(location, TEAM_PLOT_RADIUS + 10);
		prepareSpawnArea(location, true);
		plotService.createTeamPlot(new WorldPoint(location), userTeam, TEAM_PLOT_RADIUS);

		// Go in air one block and center on block so spawn is not buried
		factionSpawnService.createFactionSpawn(userTeam, new WorldPoint(location.clone().add(0, 1, 0)));
	}

	private void prepareBaseArea(Location center, int radius) {
		WorldHelper.inCircle(center.getBlockX(), center.getBlockZ(), 1, 135, radius, (x, y, z) -> {
			Block block = center.getWorld().getBlockAt(x, y, z);
			// Remove all blocks in upper area
			if (y > center.getBlockY() - CENTER_SPAWN_HEIGHT) {
				block.setType(Material.AIR);
			}
			// One layer of grass
			else if (y == center.getBlockY() - CENTER_SPAWN_HEIGHT) {
				block.setType(Material.GRASS);
			}
			// Fill in lower areas with bedrock
			else if (y < center.getBlockY() - CENTER_SPAWN_HEIGHT) {
				block.setType(Material.BEDROCK);
			}
		});
	}

	public void prepareSpawnArea(Location spawnLocation, boolean isBase) {
		World world = spawnLocation.getWorld();
		Block center = world.getBlockAt(spawnLocation);

		int platformBottomY = spawnLocation.getBlockY();
		int platformTopY = platformBottomY + CENTER_SPAWN_CLEARANCE;

		WorldHelper.inSquare(spawnLocation.getBlockX(), spawnLocation.getBlockZ(), spawnLocation.getBlockY() - CENTER_SPAWN_HEIGHT, 128, 2, (x, y, z) -> {
			if (y < platformBottomY || y == platformTopY) {
				// Make huge cylinder from END_BRICKS to spawn location
				if (x == center.getX() && z == center.getZ()) {
					if (y == platformTopY) {
						// Hole above
						world.getBlockAt(x, y, z).setType(Material.AIR);
					} else {
						// Thread of blocks for the win condition
						world.getBlockAt(x, y, z).setType(Material.END_BRICKS);
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

		// Water drops
		// North
		world.getBlockAt(spawnLocation.getBlockX() - 1, spawnLocation.getBlockY() - CENTER_SPAWN_HEIGHT, spawnLocation.getBlockZ() + 3).setType(Material.STATIONARY_WATER);
		world.getBlockAt(spawnLocation.getBlockX(), spawnLocation.getBlockY() - CENTER_SPAWN_HEIGHT, spawnLocation.getBlockZ() + 3).setType(Material.STATIONARY_WATER);
		world.getBlockAt(spawnLocation.getBlockX() + 1, spawnLocation.getBlockY() - CENTER_SPAWN_HEIGHT, spawnLocation.getBlockZ() + 3).setType(Material.STATIONARY_WATER);
		// East
		world.getBlockAt(spawnLocation.getBlockX() + 3, spawnLocation.getBlockY() - CENTER_SPAWN_HEIGHT, spawnLocation.getBlockZ() - 1).setType(Material.STATIONARY_WATER);
		world.getBlockAt(spawnLocation.getBlockX() + 3, spawnLocation.getBlockY() - CENTER_SPAWN_HEIGHT, spawnLocation.getBlockZ()).setType(Material.STATIONARY_WATER);
		world.getBlockAt(spawnLocation.getBlockX() + 3, spawnLocation.getBlockY() - CENTER_SPAWN_HEIGHT, spawnLocation.getBlockZ() + 1).setType(Material.STATIONARY_WATER);
		// South
		world.getBlockAt(spawnLocation.getBlockX() - 1, spawnLocation.getBlockY() - CENTER_SPAWN_HEIGHT, spawnLocation.getBlockZ() - 3).setType(Material.STATIONARY_WATER);
		world.getBlockAt(spawnLocation.getBlockX(), spawnLocation.getBlockY() - CENTER_SPAWN_HEIGHT, spawnLocation.getBlockZ() - 3).setType(Material.STATIONARY_WATER);
		world.getBlockAt(spawnLocation.getBlockX() + 1, spawnLocation.getBlockY() - CENTER_SPAWN_HEIGHT, spawnLocation.getBlockZ() - 3).setType(Material.STATIONARY_WATER);
		// West
		world.getBlockAt(spawnLocation.getBlockX() - 3, spawnLocation.getBlockY() - CENTER_SPAWN_HEIGHT, spawnLocation.getBlockZ() - 1).setType(Material.STATIONARY_WATER);
		world.getBlockAt(spawnLocation.getBlockX() - 3, spawnLocation.getBlockY() - CENTER_SPAWN_HEIGHT, spawnLocation.getBlockZ()).setType(Material.STATIONARY_WATER);
		world.getBlockAt(spawnLocation.getBlockX() - 3, spawnLocation.getBlockY() - CENTER_SPAWN_HEIGHT, spawnLocation.getBlockZ() + 1).setType(Material.STATIONARY_WATER);


		// Create beacon
		Block beaconBlock = center.getRelative(BlockFace.DOWN, 1);
		beaconBlock.setType(Material.BEACON);
		beaconBlock.getRelative(0, -1, 1).setType(Material.IRON_BLOCK);
		beaconBlock.getRelative(1, -1, 1).setType(Material.IRON_BLOCK);
		beaconBlock.getRelative(1, -1, 0).setType(Material.IRON_BLOCK);
		beaconBlock.getRelative(1, -1, -1).setType(Material.IRON_BLOCK);
		beaconBlock.getRelative(0, -1, -1).setType(Material.IRON_BLOCK);
		beaconBlock.getRelative(-1, -1, -1).setType(Material.IRON_BLOCK);
		beaconBlock.getRelative(-1, -1, 0).setType(Material.IRON_BLOCK);
		beaconBlock.getRelative(-1, -1, 1).setType(Material.IRON_BLOCK);

		// Create chest
		Location chestLocation = spawnLocation.clone();
		chestLocation.setY(platformTopY);
		prepareSpawnChest(world.getBlockAt(chestLocation), isBase);

		// Create wall
		prepareSpawnWall(spawnLocation);
	}

	private void prepareSpawnChest(Block chestBlock, boolean isBase) {
		chestBlock.setType(Material.CHEST);

		// Add some initial material
		if (isBase) {
			Chest chest = (Chest) chestBlock.getState();
			Inventory inventory = chest.getBlockInventory();

			int startingStoneStacks = 8;
			for (int i = 0; i < startingStoneStacks; i++) {
				inventory.addItem(new ItemStack(Material.STONE, 64));
			}

			int startingCobbleStacks = 2;
			for (int i = 0; i < startingCobbleStacks; i++) {
				inventory.addItem(new ItemStack(Material.COBBLESTONE, 64));
			}

			int startingWoodStacks = 1;
			for (int i = 0; i < startingWoodStacks; i++) {
				inventory.addItem(new ItemStack(Material.LOG, 64));
			}
		}

		// Init outputting container
		Container baseLootContainer = containerService.createContainer(new WorldPoint(chestBlock.getLocation()));
		baseLootContainer.setOutputType(isBase ? Container.ContainerOutputType.BASE : Container.ContainerOutputType.OUTPOST);
		baseLootContainer.setOutputPerHour(15);
		baseLootContainer.setPermission(Container.ContainerPermission.TEAM_VETERAN);
		containerService.updateContainer(baseLootContainer);
	}

	private void prepareSpawnWall(Location spawnLocation) {
		World world = spawnLocation.getWorld();
		// Build ugly stone wall around spawn tower
		WorldHelper.onCircle(spawnLocation.getBlockX(), spawnLocation.getBlockZ(), spawnLocation.getBlockY() - CENTER_SPAWN_HEIGHT, spawnLocation.getBlockY() - CENTER_SPAWN_HEIGHT + 3, 11, (x, y, z) -> {
			if (y == spawnLocation.getBlockY() - CENTER_SPAWN_HEIGHT + 3) {
				if (random.nextDouble() > 0.8) {
					world.getBlockAt(x, y, z).setType(Material.COBBLESTONE);
				}
			} else {
				if (random.nextDouble() > 0.9) {
					world.getBlockAt(x, y, z).setType(Material.MOSSY_COBBLESTONE);
				} else {
					world.getBlockAt(x, y, z).setType(Material.COBBLESTONE);
				}
			}
		});
	}

	private void prepareCenterTrench(Location center) {
		World world = center.getWorld();
		for (int z = center.getBlockZ() - (WORLD_BORDER / 2); z <= center.getBlockZ() + (WORLD_BORDER / 2); z++) {
			for (int x = center.getBlockX() - 7; x <= center.getBlockX() + 7; x++) {
				if (x == 0 || x % 7 != 0 || random.nextDouble() > 0.15) {
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
