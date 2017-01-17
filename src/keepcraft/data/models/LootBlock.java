package keepcraft.data.models;

import java.util.Calendar;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import keepcraft.Keepcraft;

/**
 *
 * @author Me
 */
public class LootBlock implements Runnable {

    private static class LootBlockScheduler implements Runnable {

        private final LootBlock target;

        public LootBlockScheduler(LootBlock lootblock) {
            target = lootblock;
        }

        @Override
        public void run() {
            target.startDispensing(true);
        }
    }

    private static Random random = new Random();
    private final BukkitScheduler scheduler;

    private final int id;
    private final Block block;
    private int status = 1;
    private int type = 1;
    private double output = 1;

    private int dispenseTaskId = 0;
    private int delayedTaskId = 0;

    public LootBlock(int id, Block block) {
        this.id = id;
        this.block = block;
        this.scheduler = Bukkit.getScheduler();
    }

    public int getId() {
        return id;
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

    public double getOutput() {
        return output;
    }

    public void setOutput(double value) {
        output = value;
    }

    @Override
    public void run() {
        if (block.getType() != Material.CHEST) {
            return;
        }

        Chest chest = (Chest) block.getState();
        Inventory inv = chest.getBlockInventory();

        for (int i = 0; i < (output * 60); i++) {
            ItemStack item = null;
            double value = random.nextDouble();

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
                item = new ItemStack(Material.INK_SACK, 1, (short) 4); // lapis
            } else if (value <= 0.92) {
                item = new ItemStack(Material.NETHER_BRICK, 1); // nether block
            } else if (value <= 0.97) {
                item = new ItemStack(Material.NETHER_STALK, 1); // nether stalk
            } else if (value <= 0.99) {
                item = new ItemStack(Material.CLAY_BALL, 1);
            } else {
                item = new ItemStack(Material.PORK, 1);
            }

            // Excess items are not put in by default
            inv.addItem(item);
        }

        World w = chest.getWorld();
        w.playEffect(block.getRelative(BlockFace.UP).getLocation(), Effect.SMOKE, 4);

        dispenseTaskId = 0; // We are done with this dispensing round

        // This will call startDispensing in 5 minutes, ensures we don't get any immeadiate repeats
        delayedTaskId = scheduler.scheduleSyncDelayedTask(Keepcraft.instance(), new LootBlockScheduler(this), 1200 * 10);
    }

    public void startDispensing() {
        if (dispenseTaskId == 0 && delayedTaskId == 0) // if this is false we're currently running
        {
            int minutesIntoHour = Calendar.getInstance().get(Calendar.MINUTE);
            int remainingMinutes = 60 - minutesIntoHour;
            Keepcraft.log("There are " + remainingMinutes + "m remaining in the hour");

            // start a task 1200 * remaining minutes when lootblock will dipsense
            long nextDispense = 1200 * (remainingMinutes + 1 + random.nextInt(2));
            dispenseTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(
                    Bukkit.getPluginManager().getPlugin("Keepcraft"), this, nextDispense);
        }
    }

    private void startDispensing(boolean isDelayedTask) {
        if (isDelayedTask) {
            delayedTaskId = 0;
        }
        startDispensing();
    }

    public void stopDispensing() {
        if (delayedTaskId != 0) {
            scheduler.cancelTask(delayedTaskId);
            delayedTaskId = 0;
        }

        if (dispenseTaskId != 0) {
            scheduler.cancelTask(dispenseTaskId);
            dispenseTaskId = 0;
        }
    }

}
