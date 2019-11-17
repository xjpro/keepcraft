package keepcraft.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityInteractEvent;

public class WorldEntityListener implements Listener {

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEndermanPickup(EntityChangeBlockEvent event) {
		if (event.getEntityType() == EntityType.ENDERMAN) {
			// This is annoying
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityInteract(EntityInteractEvent event) {
		if (event.getEntity() instanceof Creature) {
			Block clicked = event.getBlock();
			Material blockType = clicked.getType();
			switch (blockType) {
				// Just stop creatures from activating stone switches
				case STONE_BUTTON:
				case STONE_PRESSURE_PLATE:
				case LEVER:
					event.setCancelled(true);
					break;
			}
		}
	}

}
