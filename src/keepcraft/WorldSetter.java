package keepcraft;

import java.util.*;

import keepcraft.data.models.FactionSpawn;
import keepcraft.data.models.UserFaction;
import keepcraft.services.FactionSpawnService;
import keepcraft.services.PlotService;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class WorldSetter {

	private final PlotService plotService;
	private final FactionSpawnService factionSpawnService;
	private final int TEAM_PLOT_RADIUS = 80;

	public WorldSetter(PlotService plotService, FactionSpawnService factionSpawnService) {
		this.plotService = plotService;
		this.factionSpawnService = factionSpawnService;
	}

	public World reset(World currentWorld) {
		Server server = Bukkit.getServer();

		String currentWorldNameNumber = currentWorld.getName().replace("world", "");
		int currentWorldNumber = currentWorldNameNumber.length() == 0 ? 0 : Integer.parseInt(currentWorldNameNumber);

		// Create new world
		WorldCreator creator = (new WorldCreator("world" + (currentWorldNumber + 1)))
				.seed(new Random().nextInt())
				.type(WorldType.NORMAL)
				.environment(World.Environment.NORMAL);
		World newWorld = server.createWorld(creator);

		// Setup new world with plots
		setBase(UserFaction.FactionRed, new Location(newWorld, 175.5, 64, 175.5));
		setBase(UserFaction.FactionBlue, new Location(newWorld, -175.5, 64, -175.5));

		newWorld.save();

		// Unload old world
		server.unloadWorld(currentWorld, true); // save & unload old world

		return newWorld;
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
		WorldHelper.inCircle(center.getBlockX(), center.getBlockZ(), 1, 150, radius, (x, y, z) -> {
			Block block = center.getWorld().getBlockAt(x, y, z);
			Material type = block.getType();

			// Flatten above 90
			if (y > 90) {
				block.setType(Material.AIR);
			}
			// Remove water at 62
			else if (y <= 62 && (type == Material.STATIONARY_WATER || type == Material.WATER)) {
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

		WorldHelper.inCircle(spawnLocation.getBlockX(), spawnLocation.getBlockZ(), 1, platformTopY, 3, (x, y, z) -> {
			if (y < platformBottomY || y == platformTopY) {
				// Make huge cylinder from bedrock to spawn location
				world.getBlockAt(x, y, z).setType(Material.BEDROCK);
			} else {
				world.getBlockAt(x, y, z).setType(Material.AIR); // set things to air by default
				// Build hollow area
				// North wall
				world.getBlockAt(spawnLocation.getBlockX() - 1, y, spawnLocation.getBlockZ() + 2).setType(Material.BEDROCK);
				world.getBlockAt(spawnLocation.getBlockX() + 1, y, spawnLocation.getBlockZ() + 2).setType(Material.BEDROCK);
				// East wall
				world.getBlockAt(spawnLocation.getBlockX() + 2, y, spawnLocation.getBlockZ() + 1).setType(Material.BEDROCK);
				world.getBlockAt(spawnLocation.getBlockX() + 2, y, spawnLocation.getBlockZ() - 1).setType(Material.BEDROCK);
				// South wall
				world.getBlockAt(spawnLocation.getBlockX() - 1, y, spawnLocation.getBlockZ() - 2).setType(Material.BEDROCK);
				world.getBlockAt(spawnLocation.getBlockX() + 1, y, spawnLocation.getBlockZ() - 2).setType(Material.BEDROCK);
				// West wall
				world.getBlockAt(spawnLocation.getBlockX() - 2, y, spawnLocation.getBlockZ() + 1).setType(Material.BEDROCK);
				world.getBlockAt(spawnLocation.getBlockX() - 2, y, spawnLocation.getBlockZ() - 1).setType(Material.BEDROCK);

//				if(y == platformBottomY) {
//					world.getBlockAt(spawnLocation.getBlockX(), y, spawnLocation.getBlockZ() + 2).setType(Material.WOODEN_DOOR);
//					world.getBlockAt(spawnLocation.getBlockX(), y, spawnLocation.getBlockZ() - 2).setType(Material.WOODEN_DOOR);
//					world.getBlockAt(spawnLocation.getBlockX() + 2, y, spawnLocation.getBlockZ()).setType(Material.WOODEN_DOOR);
//					world.getBlockAt(spawnLocation.getBlockX() - 2, y, spawnLocation.getBlockZ()).setType(Material.WOODEN_DOOR);
//				}
			}
		});


		center.getRelative(BlockFace.DOWN).setType(Material.BEACON);
	}
}
