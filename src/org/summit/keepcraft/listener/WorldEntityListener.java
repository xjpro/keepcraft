package org.summit.keepcraft.listener;

import java.util.Random;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityInteractEvent;

public class WorldEntityListener implements Listener
{	
    private final Random random = new Random();
    private double spawnChance = 0.6;

    /*@EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent event)
	{
        if(event.getSpawnReason() == SpawnReason.NATURAL)
        {
            if(random.nextDouble() > spawnChance)
            {
                event.setCancelled(true);
            }
        }
	}*/
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEndermanPickup(EntityChangeBlockEvent event)
    {
        event.setCancelled(true); // This is annoying
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
	public void onEntityInteract(EntityInteractEvent event)
	{
		if(event.getEntity() instanceof Creature)
		{
			Block clicked = event.getBlock();
			Material blockType = clicked.getType();
			switch(blockType)
			{
			// Just stop creatures from activating stone switches
			case STONE_BUTTON:
			case STONE_PLATE:
			case LEVER:
				event.setCancelled(true);
				break;
			}
		}
	}
    
}
