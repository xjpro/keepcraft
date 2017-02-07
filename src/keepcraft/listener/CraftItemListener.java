package keepcraft.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;

public class CraftItemListener implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void onBrew(BrewEvent event) {
		BrewerInventory contents = event.getContents();
		switch (contents.getIngredient().getType()) {

			case NETHER_STALK: // makes the base "awkward" potion
			case SULPHUR: // allows throwing variants

			case GHAST_TEAR: // regen
			case SPIDER_EYE: // poison
			case GOLDEN_CARROT: // night vision
			case RAW_FISH: // water breathing
			case MAGMA_CREAM: // fire resistance
				break;

			case FERMENTED_SPIDER_EYE: // weakness and also some upgrades
				// Allows converting some potions into others
				for (ItemStack bottle : event.getContents().getStorageContents()) {
					//if(bottle.getType())
					// todo check if bottle is Potion of Night Vision and allow that
				}
				event.setCancelled(true);
				break;

			case REDSTONE: // allows extended variants
			case GLOWSTONE_DUST: // allows level 2 variants
			default:
				event.setCancelled(true);
				break;
		}
	}
}
