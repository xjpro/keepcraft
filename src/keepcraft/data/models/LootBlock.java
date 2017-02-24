package keepcraft.data.models;

import keepcraft.Keepcraft;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Random;

public class LootBlock {

	public enum ContainerType {
		PUBLIC(0),
		TEAM_STARTER(1),
		TEAM_NORMAL(2),
		TEAM_VETERAN(3),
		PRIVATE(4);

		private final int id;

		ContainerType(int id) {
			this.id = id;
		}

		public static ContainerType getContainerType(int id) {
			return Arrays.stream(ContainerType.values()).filter(containerType -> containerType.getId() == id).findFirst().orElse(null);
		}

		public int getId() {
			return id;
		}
	}

	private static Random Random = new Random();

	private final WorldPoint worldPoint;
	private int status = 1;
	private ContainerType type = ContainerType.PUBLIC;

	// Output in items generated per hour
	private int outputPerHour = 0;
	// Fractional output from previous run
	private double leftoverOutput = 0;

	public LootBlock(WorldPoint worldPoint) {
		this.worldPoint = worldPoint;
	}

	public WorldPoint getWorldPoint() {
		return worldPoint;
	}

	public Block getBlock() {
		return Keepcraft.getWorld().getBlockAt(worldPoint.asLocation());
	}

	public Chunk getChunk() {
		return getBlock().getChunk();
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int value) {
		status = value;
	}

	public ContainerType getType() {
		return type;
	}

	public void setType(ContainerType value) {
		type = value;
	}

	public int getOutputPerHour() {
		return outputPerHour;
	}

	public void setOutputPerHour(int value) {
		outputPerHour = value;
	}

	public void dispense() {
		Block block = getBlock();
		if (block == null || !(block.getState() instanceof InventoryHolder) || outputPerHour == 0) return;

		Chest chest = (Chest) block.getState();
		Inventory inventory = chest.getBlockInventory();

		// Say outputPerHour per hour is 75
		// We'll need to put (75/60) = 1.25 items into the chest per minute
		// It's obviously impossible to put fractions of items into the chest
		double fullOutputThisRun = (outputPerHour / 60.0) + leftoverOutput;
		long integerOutputThisRun = (long) fullOutputThisRun; // So calculate the integer amount we can put in
		leftoverOutput = fullOutputThisRun - integerOutputThisRun; // And save the remainder to be used in the next run

		for (int i = 0; i < integerOutputThisRun; i++) {
			ItemStack item;
			double value = Random.nextDouble();

			// Items to facilitate sieging
			if (value <= 0.20) { // 20%
				item = new ItemStack(Material.GOLD_NUGGET, 1); // gold nuggets, to make magma blocks
			} else if (value <= 0.50) { // 30%
				item = new ItemStack(Material.SULPHUR, 1); // sulfur, for tnt
			} else if (value <= 0.70) { // 20%
				item = new ItemStack(Material.ARROW, 1); // arrow
			}
			// Items to facilitate brewing
			else if (value <= 0.80) { // 10%
				// Needed for all potions (makes the base potions)
				item = new ItemStack(Material.NETHER_STALK, 1); // nether stalk
			} else if (value <= 0.83) { // 3%
				// Need to make brewing stand and as fuel
				item = new ItemStack(Material.BLAZE_ROD, 1); // blaze rod
			} else if (value <= 0.86) { // 3%
				// Makes regen potion
				item = new ItemStack(Material.GHAST_TEAR, 1); // ghast tear
			} else if (value <= 0.89) { // 3%
				// Makes poison potion
				item = new ItemStack(Material.SPIDER_EYE, 1); // spider eye
			} else if (value <= 0.91) { // 2%
				// Makes night vision & invis potions
				item = new ItemStack(Material.BROWN_MUSHROOM, 1); // used for creating fermented spider eyes
			} else if (value <= 0.92) { // 1%
				// Makes lingering potions
				item = new ItemStack(Material.DRAGONS_BREATH, 1);
			}
			// Items that come in grinders
			else if (value <= 0.93) { // 1%
				item = new ItemStack(Material.BONE, 1);
			} else if (value <= 0.94) { // 1%
				item = new ItemStack(Material.STRING, 1);
			} else if (value <= 0.95) { // 1%
				item = new ItemStack(Material.SLIME_BALL, 1);
			}
			// Items that allow building with nether materials
			else if (value <= 0.96) { // 1%
				item = new ItemStack(Material.QUARTZ, 1);
			}
			// Remainder 4% (pork)
			else {
				item = new ItemStack(Material.PORK, 1);
			}

			inventory.addItem(item);
		}

		// Make a little smoke effect
		block.getWorld().playEffect(block.getRelative(BlockFace.UP).getLocation(), Effect.SMOKE, 4);
		block.getWorld().playEffect(block.getLocation(), Effect.CLICK1, 0);
	}
}
