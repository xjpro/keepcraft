package keepcraft.data.models;

import keepcraft.Keepcraft;
import keepcraft.listener.OutpostListener;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Random;

public class Container {

    public enum ContainerPermission {
        PUBLIC(0),
        TEAM_NORMAL(1),
        TEAM_VETERAN(2),
        PRIVATE(3);

        private final int id;

        ContainerPermission(int id) {
            this.id = id;
        }

        public static ContainerPermission getContainerPermission(int id) {
            return Arrays.stream(ContainerPermission.values()).filter(containerPermission -> containerPermission.getId() == id).findFirst().orElse(null);
        }

        public int getId() {
            return id;
        }
    }

    public enum ContainerOutputType {
        NONE(0),
        BASE(1),
        WORLD_ALL(2),
        OUTPOST(3);

        private final int id;

        ContainerOutputType(int id) {
            this.id = id;
        }

        public static ContainerOutputType getContainerOutputType(int id) {
            return Arrays.stream(ContainerOutputType.values()).filter(outputType -> outputType.getId() == id).findFirst().orElse(null);
        }

        public int getId() {
            return id;
        }
    }

    private static Random Random = new Random();

    private final WorldPoint worldPoint;
    private ContainerPermission permission = ContainerPermission.PUBLIC;
    private ContainerOutputType outputType = ContainerOutputType.NONE;

    // Output in items generated per hour
    private int outputPerHour = 0;

    public Container(WorldPoint worldPoint) {
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

    public ContainerPermission getPermission() {
        return permission;
    }

    public void setPermission(ContainerPermission value) {
        permission = value;
    }

    public ContainerOutputType getOutputType() {
        return outputType;
    }

    public void setOutputType(ContainerOutputType value) {
        outputType = value;
    }

    public int getOutputPerHour() {
        return outputPerHour;
    }

    public void setOutputPerHour(int value) {
        outputPerHour = value;
    }

    public void dispense(double modifier) {
        Block block = getBlock();
        if (block == null || !(block.getState() instanceof InventoryHolder) || outputPerHour == 0) return;

        Chest chest = (Chest) block.getState();
        Inventory inventory = chest.getBlockInventory();

        // Say outputPerHour per hour is 75
        // We'll need to put (75/60) = 1.25 items into the chest per minute
        // It's obviously impossible to put fractions of items into the chest
        double outputWithModifier = outputPerHour * modifier;
        double fullOutputThisRun = outputWithModifier * 24;
        long integerOutputThisRun = (long) fullOutputThisRun; // So calculate the integer amount we can put in

        for (int i = 0; i < integerOutputThisRun; i++) {
            ItemStack item = null;
            double value = Random.nextDouble();

            // Items to facilitate sieging
            if (value <= 0.50) { // 50%
//				item = new ItemStack(Material.LEGACY_SULPHUR, 1); // sulfur, for tnt and siege blocks
            } else if (value <= 0.70) { // 20%
                item = new ItemStack(Material.ARROW, 1); // arrow
            }
            // Items to facilitate brewing
            else if (value <= 0.80) { // 10%
                // Needed for all potions (makes the base potions)
//				item = new ItemStack(Material.LEGACY_NETHER_STALK, 1); // nether stalk
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
//				item = new ItemStack(Material.LEGACY_DRAGONS_BREATH, 1);
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
            else if (value <= 0.97) { // 2%
                item = new ItemStack(Material.QUARTZ, 1);
            } else if (value <= 0.99) { // 2%
                item = new ItemStack(Material.GLOWSTONE, 1);
            }
            // Remainder (pork)
            else {
                item = new ItemStack(Material.COOKIE, 1);
            }

            if (item != null) {
                inventory.addItem(item);
            }
        }

        if (outputType == ContainerOutputType.BASE) {
            ItemStack outpostPlacementItem = new ItemStack(OutpostListener.OUTPOST_PLACEMENT_MATERIAL);
            ItemMeta itemMeta = outpostPlacementItem.getItemMeta();
            itemMeta.setDisplayName("Outpost Block");
            itemMeta.setLore(Arrays.asList("Place above sea level to", "create a new outpost", "for your team"));
            outpostPlacementItem.setItemMeta(itemMeta);
            inventory.addItem(outpostPlacementItem);
        }

        // Make a little smoke effect
        block.getWorld().playEffect(block.getRelative(BlockFace.UP).getLocation(), Effect.SMOKE, 4);
        block.getWorld().playEffect(block.getLocation(), Effect.CLICK1, 0);
    }
}
