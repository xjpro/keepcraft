package keepcraft.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import keepcraft.services.ChatService;
import keepcraft.services.PlotService;
import keepcraft.services.UserService;
import org.bukkit.Bukkit;
import keepcraft.data.models.Plot;
import keepcraft.data.models.PlotProtection;
import keepcraft.data.models.User;
import keepcraft.data.models.UserFaction;

public class Siege implements Runnable {

    private final static double CAPTURE_BONUS_MODIFIER = 1.0;
    private final UserService userService;
    private final PlotService plotService;
    private final ChatService chatService;

    private final Plot plot;
    private final User initiatingUser;

    private int taskId;
    private boolean inProgress = false;
    private int remainingTime; // in seconds

    private final int attackingFaction;
    private final int defendingFaction;

    // TODO refactor this into a siegeService
    public Siege(UserService userService, PlotService plotService, ChatService chatService, Plot plot, User initiatingUser) {
        this.userService = userService;
        this.plotService = plotService;
        this.chatService = chatService;
        this.plot = plot;
        this.initiatingUser = initiatingUser;

        attackingFaction = initiatingUser.getFaction();
        defendingFaction = plot.getProtection().getType();
    }

    public void setTaskId(int value) {
        taskId = value;
    }

    public int getAttackingFaction() {
        return attackingFaction;
    }

    public int getDefendingFaction() {
        return defendingFaction;
    }

    @Override
    public void run() {
        if (!inProgress) {
            begin();
        } else {
            List<User> attackers = new ArrayList<>();
            Collection<User> users = userService.getOnlineUsers();
            for (User user : users) {
                if (user.getCurrentPlot() == plot && !user.isAdmin() && user.getFaction() == attackingFaction) {
                    // they are in the plot
                    attackers.add(user);
                }
            }

            // this should be occuring every 30 seconds, tick down
            double attackerBonus = Math.min(10, attackers.size() * CAPTURE_BONUS_MODIFIER); // 10 player is max
            //attackerBonus = Math.max(1, attackerBonus); // 1 is minimum value (no bonus)
            remainingTime -= 30 * attackerBonus;

            if (attackers.isEmpty()) // No attackers are in the area anymore
            {
                cancel();
            } else if (remainingTime <= 0) // Siege is over and we can switch control
            {
                finish();
            } else // Carry on
            {
                for (User attacker : attackers) {
                    chatService.sendAlertMessage(attacker, "Capture will complete in " + timeLeft(remainingTime) + " (" + (int) Math.round((attackerBonus - CAPTURE_BONUS_MODIFIER) * 100) + "% force bonus)");
                }
            }
        }
    }

    private void begin() {
        remainingTime = plot.getProtection().getCaptureTime();
        chatService.sendPlotCaptureMessage(initiatingUser, "has begun capturing", plot, "(" + timeLeft(remainingTime) + ")");
        inProgress = true;

        PlotProtection protection = plot.getProtection();
        protection.setCaptureInProgress(true);
        plot.setSiege(this);
    }

    private void finish() {
        chatService.sendGlobalAlertMessage(UserFaction.asColoredString(attackingFaction) + ChatService.Info + " has secured " + plot.getColoredName());
        Bukkit.getServer().getScheduler().cancelTask(taskId);

        PlotProtection protection = plot.getProtection();
        protection.setType(initiatingUser.getFaction());
        protection.setCaptureInProgress(false);
        plot.setSiege(null);
        plotService.updatePlot(plot);
    }

    public void cancel() {
        chatService.sendGlobalAlertMessage(UserFaction.asColoredString(attackingFaction) + ChatService.Info + " failed to capture " + plot.getColoredName());
        Bukkit.getServer().getScheduler().cancelTask(taskId);
        plot.setSiege(null);
        plot.getProtection().setCaptureInProgress(false);
    }

    public void cancel(User canceller) {
        chatService.sendPlotDefendMessage(canceller, "has defended", plot);
        Bukkit.getServer().getScheduler().cancelTask(taskId);
        plot.setSiege(null);
        plot.getProtection().setCaptureInProgress(false);
    }

    private String timeLeft(int seconds) {
        int minutesLeft = seconds / 60;
        int secondsLeft = seconds - (minutesLeft * 60);

        if (secondsLeft <= 0) {
            return minutesLeft + "m";
        }
        if (minutesLeft <= 0) {
            return secondsLeft + "s";
        }
        return minutesLeft + "m " + secondsLeft + "s";
    }

}
