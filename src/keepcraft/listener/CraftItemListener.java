package keepcraft.listener;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;

public class CraftItemListener implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void onBrew(BrewEvent event) {
		BrewerInventory contents = event.getContents();
		switch (contents.getIngredient().getType()) {

			case LEGACY_NETHER_STALK: // makes the base "awkward" potion
			case LEGACY_SULPHUR: // allows throwing variants
			case LEGACY_DRAGONS_BREATH: // allows lingering variants

			case GHAST_TEAR: // regen
			case SPIDER_EYE: // poison
			case GOLDEN_CARROT: // night vision
			case LEGACY_RAW_FISH: // water breathing
			case MAGMA_CREAM: // fire resistance
				break;

			case FERMENTED_SPIDER_EYE: // weakness and also some upgrades
				// Allows converting some potions into others
//				for (ItemStack bottle : event.getContents().getStorageContents()) {
//					//if(bottle.getType())
//					// todo check if bottle is Potion of Night Vision and allow that
//				}
				event.setCancelled(true);
				break;

			case REDSTONE: // allows extended variants
			case GLOWSTONE_DUST: // allows level 2 variants
			default:
				event.setCancelled(true);
				break;
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onItemCrafted(CraftItemEvent event) {
		if (event.isCancelled()) return;

		ItemStack item = event.getCurrentItem();
		switch (item.getType()) {
			case LEGACY_WOOD_SPADE:
			case LEGACY_STONE_SPADE:
			case LEGACY_IRON_SPADE:
			case LEGACY_GOLD_SPADE:
			case LEGACY_DIAMOND_SPADE:
			case LEGACY_WOOD_PICKAXE:
			case STONE_PICKAXE:
			case IRON_PICKAXE:
			case LEGACY_GOLD_PICKAXE:
			case DIAMOND_PICKAXE:
			case LEGACY_WOOD_AXE:
			case STONE_AXE:
			case IRON_AXE:
			case LEGACY_GOLD_AXE:
			case DIAMOND_AXE:
				item.addEnchantment(Enchantment.DURABILITY, 3);
				item.addEnchantment(Enchantment.DIG_SPEED, 3);
				break;
			case LEGACY_WOOD_HOE:
			case STONE_HOE:
			case IRON_HOE:
			case LEGACY_GOLD_HOE:
			case DIAMOND_HOE:
				item.addEnchantment(Enchantment.DURABILITY, 3);
				break;
		}
	}
}
