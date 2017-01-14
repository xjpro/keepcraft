package org.summit.keepcraft.listener;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.summit.keepcraft.Chat;
import org.summit.keepcraft.Privilege;
import org.summit.keepcraft.data.DataCache;
import org.summit.keepcraft.data.models.Plot;
import org.summit.keepcraft.data.models.User;

public class BlockProtectionListener implements Listener 
{
    @EventHandler(priority = EventPriority.LOW)
	public void onBlockPlace(BlockPlaceEvent event)
	{
        Block block = event.getBlock();

        Player p = event.getPlayer();
		User user = DataCache.retrieve(User.class, p.getName());
        
        if(user.isAdmin()) return;
        
        // Not allowed blocks anywhere
        if(block.getType() == Material.ENCHANTMENT_TABLE)
        {
            event.setCancelled(true);
            event.setBuild(false);
        }
        
        Plot plot = ListenerHelper.getIntersectedPlot(block.getLocation(), new ArrayList<Plot>(DataCache.retrieveAll(Plot.class)));
        
        if(plot == null || plot.getProtection() == null) return;
        
        switch(event.getBlock().getType())
        {
            case FIRE:
                if(event.getBlockAgainst().getType() == Material.TNT) return; // allow fire on TNT
                break;
            case TNT:
                if(!plot.isAdminProtected() && !plot.isEventProtected()) return;
                break;
        }

		if(!canModify(user, block))
		{
			event.setCancelled(true);
            event.setBuild(false);
		}
        else
        {
            switch(event.getBlock().getType())
            {
                case CHEST:
                case STORAGE_MINECART:
                case POWERED_MINECART:
                case DISPENSER:
                case FURNACE:
                case IRON_BLOCK:
                case GOLD_BLOCK:
                case DIAMOND_BLOCK:
                case IRON_ORE:
                case GOLD_ORE:
                case DIAMOND_ORE:
                    if(!plot.isWithinChestLevel(block.getLocation()))
                    {
                        event.setCancelled(true);
                        event.setBuild(false);
                        p.sendMessage(Chat.Failure + "Containers and ore cannot be placed at this height");
                    }
                    break;
            }
        }
	}
	
    @EventHandler(priority = EventPriority.LOW)
	public void onBlockBreak(BlockBreakEvent event)
	{
		Block block = event.getBlock();
        
		switch(block.getType())
		{
		// Put materials here that can be broken no matter what
		case CROPS:
        case MELON_BLOCK:
        case PUMPKIN:
        case SUGAR_CANE_BLOCK:
        case TNT:
        case MELON_STEM:
        case RED_MUSHROOM:
        case BROWN_MUSHROOM:
        case VINE:
            event.setCancelled(false);
            return;
		}
		
		Player p = event.getPlayer();
		User user = DataCache.retrieve(User.class, p.getName());
		if(!canModify(user, block))
		{
			event.setCancelled(true);
		}
	}
	
	private boolean canModify(User user, Block targetBlock)
	{
		Plot plot = ListenerHelper.getIntersectedPlot(targetBlock.getLocation(), new ArrayList<Plot>(DataCache.retrieveAll(Plot.class)));

		if(!Privilege.canInteract(user, targetBlock.getLocation(), plot))
		{
			return false;
		}
		return true;
	}
}
