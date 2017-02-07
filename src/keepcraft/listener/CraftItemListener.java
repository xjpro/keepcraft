package keepcraft.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.BrewerInventory;

public class CraftItemListener implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void onBrew(BrewEvent event) {
		BrewerInventory contents = event.getContents();
		switch (contents.getIngredient().getType()) {
			case NETHER_STALK: // makes the base potion
			case GHAST_TEAR: // regen
			case SPIDER_EYE: // poison
			case GOLDEN_CARROT: // night vision
			case FERMENTED_SPIDER_EYE: // night vision -> invisibility
				break;
			default:
				event.setCancelled(true);
		}
	}
}
