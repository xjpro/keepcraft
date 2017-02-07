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

	private final int id;
	private final Block block;
	private int status = 1;
	private int type = 1;

	// Output in items generated per hour
	private double outputPerHour = 60;
	// Fractional output from previous run
	private double leftoverOutput = 0;
	// Id of the Bukkit repeating task performing output
	private int dispenseTaskId = 0;

	public LootBlock(int id, Block block) {
		this.id = id;
		this.block = block;
	}

	public int getId() {
		return id;
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

			if (value <= 0.20) {
				item = new ItemStack(Material.ROTTEN_FLESH, 1);
			} else if (value <= 0.50) {
				item = new ItemStack(Material.SULPHUR, 1);
			} else if (value <= 0.65) {
				item = new ItemStack(Material.ARROW, 1);
			} else if (value <= 0.70) {
				item = new ItemStack(Material.BONE, 1);
			} else if (value <= 0.75) {
				item = new ItemStack(Material.STRING, 1);
			} else if (value <= 0.77) {
				item = new ItemStack(Material.FERMENTED_SPIDER_EYE, 1); // spider eye
			} else if (value <= 0.79) {
				item = new ItemStack(Material.SPIDER_EYE, 1); // spider eye (not from nether)
			} else if (value <= 0.81) {
				item = new ItemStack(Material.GOLD_NUGGET, 1); // golden nugget
			} else if (value <= 0.83) {
				item = new ItemStack(Material.GHAST_TEAR, 1); // ghast tear
			} else if (value <= 0.85) {
				item = new ItemStack(Material.BLAZE_ROD, 1); // blaze rod
			} else if (value <= 0.86) {
				item = new ItemStack(Material.GLOWSTONE, 1);
			} else if (value <= 0.87) {
				item = new ItemStack(Material.NETHERRACK, 1);
			} else if (value <= 0.88) {
				item = new ItemStack(Material.INK_SACK, 1, (short) 4); // ink
			} else if (value <= 0.92) {
				item = new ItemStack(Material.NETHER_BRICK, 1); // nether block
			} else if (value <= 0.97) {
				item = new ItemStack(Material.NETHER_STALK, 1); // nether stalk
			} else if (value <= 0.99) {
				item = new ItemStack(Material.CLAY_BALL, 1);
			} else {
				item = new ItemStack(Material.PORK, 1);
			}

			inventory.addItem(item);
		}

		// Make a little smoke effect
		block.getWorld().playEffect(block.getRelative(BlockFace.UP).getLocation(), Effect.SMOKE, 4);
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
