
package org.summit.keepcraft.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.Bukkit;
import org.summit.keepcraft.Chat;
import org.summit.keepcraft.data.DataCache;
import org.summit.keepcraft.data.models.Plot;
import org.summit.keepcraft.data.models.PlotProtection;
import org.summit.keepcraft.data.models.User;
import org.summit.keepcraft.data.models.UserFaction;

/**
 *
 * @author Me
 */
public class Siege implements Runnable
{
    private final static double CAPTURE_BONUS_MODIFIER = 1.0;
    
    private final Plot plot;
    private final User initiatingUser;
    
    private int taskId;
    private boolean inProgress = false;
    private int remainingTime; // in seconds
    
    private final int attackingFaction;
    private final int defendingFaction;
    
    public Siege(Plot plot, User initiatingUser)
    {
        this.plot = plot;
        this.initiatingUser = initiatingUser;  
        
        attackingFaction = initiatingUser.getFaction();
        defendingFaction = plot.getProtection().getType();
    }
    
    public void setTaskId(int value) 
    {
        taskId = value;
    }
    
    public int getAttackingFaction()
    {
        return attackingFaction;
    }
    
    public int getDefendingFaction()
    {
        return defendingFaction;
    }
    
    @Override
    public void run() 
    {
        if(!inProgress)
        {
            begin();
        }
        else
        {
            List<User> attackers = new ArrayList<User>();
            Collection<User> users = DataCache.retrieveAll(User.class);
            for(User user : users)
            {
                if(user.getCurrentPlot() == plot && !user.isAdmin() && user.getFaction() == attackingFaction)
                {
                    // they are in the plot
                    attackers.add(user);
                }
            }
            
            // this should be occuring every 30 seconds, tick down
            double attackerBonus = Math.min(10, attackers.size() * CAPTURE_BONUS_MODIFIER); // 10 player is max
            //attackerBonus = Math.max(1, attackerBonus); // 1 is minimum value (no bonus)
            remainingTime -= 30 * attackerBonus;
                
            if(attackers.isEmpty()) // No attackers are in the area anymore
            {
                cancel();
            }
            else if(remainingTime <= 0) // Siege is over and we can switch control
            {
                finish();
            }
            else // Carry on
            {
                for(User attacker : attackers)
                {
                    Chat.sendAlertMessage(attacker, "Capture will complete in " + timeLeft(remainingTime) + " (" + (int) Math.round((attackerBonus-CAPTURE_BONUS_MODIFIER)*100) + "% force bonus)");
                }
            }
        }
    }
    
    private void begin()
    {
        remainingTime = plot.getProtection().getCaptureTime();
        Chat.sendPlotCaptureMessage(initiatingUser, "has begun capturing", plot, "(" + timeLeft(remainingTime) + ")");
        inProgress = true;
        
        PlotProtection protection = plot.getProtection();
        protection.setCaptureInProgress(true);
        plot.setSiege(this);
    }
    
    private void finish()
    {
        Chat.sendGlobalAlertMessage(UserFaction.asColoredString(attackingFaction) + Chat.Info + " has secured " + plot.getColoredName());
        Bukkit.getServer().getScheduler().cancelTask(taskId);
        
        PlotProtection protection = plot.getProtection();
        protection.setType(initiatingUser.getFaction());
        protection.setCaptureInProgress(false);
        plot.setSiege(null);
        DataCache.update(plot);
    }
    
    public void cancel()
    {
        Chat.sendGlobalAlertMessage(UserFaction.asColoredString(attackingFaction) + Chat.Info + " failed to capture " + plot.getColoredName());
        Bukkit.getServer().getScheduler().cancelTask(taskId);
        plot.setSiege(null);
        plot.getProtection().setCaptureInProgress(false);
    }
    
    public void cancel(User canceller)
    {
        Chat.sendPlotDefendMessage(canceller, "has defended", plot);
        Bukkit.getServer().getScheduler().cancelTask(taskId);
        plot.setSiege(null);
        plot.getProtection().setCaptureInProgress(false);
    }
    
    private String timeLeft(int seconds)
    {
        int minutesLeft = seconds / 60;
        int secondsLeft = seconds - (minutesLeft * 60);
        
        if(secondsLeft <= 0) return minutesLeft + "m";
        if(minutesLeft <= 0) return secondsLeft + "s";
        return minutesLeft + "m " + secondsLeft + "s";
    }
    
}
