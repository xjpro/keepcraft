
package org.summit.keepcraft.listener;

import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.summit.keepcraft.Chat;
import org.summit.keepcraft.data.DataCache;
import org.summit.keepcraft.data.models.Direction;
import org.summit.keepcraft.data.models.Plot;
import org.summit.keepcraft.data.models.User;

/**
 *
 * @author Me
 */
public class ExplosionListener implements Listener
{
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityExplode(EntityExplodeEvent event) 
	{
        Location loc = event.getLocation();
        Plot plot = ListenerHelper.getIntersectedPlot(loc, new ArrayList<Plot>(DataCache.retrieveAll(Plot.class)));

        // But wait! no block damage for admin plots
        if(plot != null && (plot.isAdminProtected() || plot.isSpawnProtected()|| plot.intersectsAdminRadius(loc)))
        {
            event.setCancelled(true);
        }

        // Begin notification code
        if(plot != null)
        {
            Server server = Bukkit.getServer();
            Collection<User> allUsers = DataCache.retrieveAll(User.class);

            for(User user : allUsers)
            {
                if(plot == user.getCurrentPlot() && plot.isFactionProtected(user.getFaction()))
                {
                    // They need to be notified
                    Player p = server.getPlayer(user.getName());
                    Location locationTo = Direction.lookAt(p.getLocation(), loc);
                    String direction = Direction.getCardinalDirection(locationTo);
                    
                    double distance = p.getLocation().distance(loc);

                    if(distance > 30)
                    {
                        Chat.sendAlertMessage(user, "The rumble of an explosion echoes from the " + direction);
                    }
                    else if(distance > 12)
                    {
                        Chat.sendAlertMessage(user, "The roar of an explosion thunders from the " + direction);
                    }
                }
            }
        }
        // End notification code
    }
}
