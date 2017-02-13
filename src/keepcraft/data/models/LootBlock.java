package keepcraft.data.models;

import keepcraft.Keepcraft;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class LootBlock implements Runnable {

	private static Random Random = new Random();

	private final Block block;
	private int status = 1;
	private int type = 1;

	// Output in items generated per hour
	private double outputPerHour = 60;
	// Fractional output from previous run
	private double leftoverOutput = 0;
	// Id of the Bukkit repeating task performing output
	private int dispenseTaskId = 0;

	public LootBlock(Block block) {
		this.block = block;
	}

	public Block getBlock() {
		return block;
	}

	public Location getLocation() {
		return block.getLocation();
	}

	public Chunk getChunk() {
		return block.getChunk();
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int value) {
		status = value;
	}

	public int getType() {
		return type;
	}

	public void setType(int value) {
		type = value;
	}

	public double getOutputPerHour() {
		return outputPerHour;
	}

	public void setOutputPerHour(double value) {
		outputPerHour = value;
	}

	@Override
	// Runs every minute
	public void run() {
		if (block.getType() != Material.CHEST || outputPerHour == 0) return;

		Chest chest = (Chest) block.getState();
		Inventory inventory = chest.getBlockInventory();

		// Say outputPerHour per hour is 75
		// We'll need to put (75/60) = 1.25 items into the chest per minute
		// It's obviously impossible to put fractions of items into the chest
		double fullOutputThisRun = (outputPerHour / 60) + leftoverOutput;
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
			}
			// Items that come in grinders
			else if (value <= 0.92) { // 1%
				item = new ItemStack(Material.BONE, 1);
			} else if (value <= 0.93) { // 1%
				item = new ItemStack(Material.STRING, 1);
			} else if (value <= 0.95) { // 2%
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

	public void startDispensing() {
		stopDispensing();
		dispenseTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Keepcraft.getPlugin(), this, 1200, 1200);
	}

	public void stopDispensing() {
		if (dispenseTaskId != 0) {
			Bukkit.getScheduler().cancelTask(dispenseTaskId);
			dispenseTaskId = 0;
		}
	}
}
