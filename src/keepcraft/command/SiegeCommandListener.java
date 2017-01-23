package keepcraft.command;

import keepcraft.services.ServiceCache;
import keepcraft.services.UserService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import keepcraft.Chat;
import keepcraft.data.models.Plot;
import keepcraft.tasks.Siege;
import keepcraft.data.models.User;

public class SiegeCommandListener extends CommandListener {

    private UserService userService = ServiceCache.getUserService();

    @Override
    protected boolean handle(String commandName, CommandSender commandSender, String[] args) {
        User sender = userService.getOnlineUser(commandSender.getName());
        Plot currentPlot = sender.getCurrentPlot();

        if ((commandName.equalsIgnoreCase("cap") || commandName.equalsIgnoreCase("capture")) && args.length == 0) {
            Player p = (Player) commandSender;
            // Attempt to capture a plot

            if (currentPlot == null) {
                commandSender.sendMessage(Chat.Failure + "You are not in a plot");
                return true;
            }

            Siege existingSiege = currentPlot.getSiege();

            if (!currentPlot.getProtection().isCapturable() || currentPlot.getProtection().getTriggerRadius() == 0
                    || currentPlot.isAdminProtected() || currentPlot.isSpawnProtected()) {
                commandSender.sendMessage(Chat.Failure + "This area is not capturable");
                return true;
            } else if (currentPlot.isFactionProtected(sender.getFaction()) && existingSiege == null) {
                commandSender.sendMessage(Chat.Failure + "This area is already secured");
                return true;
            } else if (!currentPlot.intersectsTriggerRadius(p.getLocation())) {
                commandSender.sendMessage(Chat.Failure + "Move closer to the area's center");
                return true;
            } else {
                // We're in a plot, it can be triggered, and we're close to center
                if (existingSiege != null) {
                    if (existingSiege.getDefendingFaction() == sender.getFaction()) // the sender is a defender
                    {
                        existingSiege.cancel(sender);
                        return true;
                    } else if (existingSiege.getAttackingFaction() == sender.getFaction()) {
                        commandSender.sendMessage(Chat.Failure + "This area is already being captured");
                        return true;
                    } else // A third faction is attacking
                    {
                        existingSiege.cancel();
                        commandSender.sendMessage(Chat.Success + "You begin capturing " + currentPlot.getName());
                        startSiege(sender, currentPlot);
                        return true;
                    }
                } else {
                    // begin siege?
                    commandSender.sendMessage(Chat.Success + "You begin capturing " + currentPlot.getName());
                    startSiege(sender, currentPlot);
                    return true;
                }
            }
        }

        return false;
    }

    private void startSiege(User sender, Plot plot) {
        Siege siege = new Siege(plot, sender);

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Bukkit.getPluginManager().getPlugin("Keepcraft"), siege, 0, 600);
        // 20 tick value = 1 second
        // 1200 tick value = 1 minute
        siege.setTaskId(taskId);
    }

}
