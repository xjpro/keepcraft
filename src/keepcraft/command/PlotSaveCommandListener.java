package keepcraft.command;

import keepcraft.WorldHelper;
import keepcraft.data.models.*;
import keepcraft.services.PlotService;
import keepcraft.services.UserService;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

import java.io.PrintWriter;

public class PlotSaveCommandListener extends CommandListener {

    private final UserService userService;
    private final PlotService plotService;

    public PlotSaveCommandListener(UserService userService, PlotService plotService) {
        this.userService = userService;
        this.plotService = plotService;
    }

    @Override
    protected boolean handle(String commandName, CommandSender commandSender, String[] args) {
        User sender = userService.getOnlineUser(commandSender.getName());
        if (!sender.isAdmin()) return false;

        Plot currentPlot = sender.getCurrentPlot();
        Location center = currentPlot.getWorldPoint().asLocation();
        int radius = (int) currentPlot.getRadius();

        if (commandName.equalsIgnoreCase("saveplot")) {
            World world = center.getWorld();

            PrintWriter writer;
            try {
                writer = new PrintWriter("the-file-name.txt", "UTF-8");
            } catch (Exception exception) {
                return false;
            }

            StringBuffer blockString = new StringBuffer();
            WorldHelper.inCircle(center.getBlockX(), center.getBlockZ(), 1, 256, radius, (x, y, z) -> {
                Block block = world.getBlockAt(x, y, z);
                Location location = block.getLocation();
                blockString
                        .append(location.getBlockX())
                        .append(",")
                        .append(location.getBlockY())
                        .append(",")
                        .append(location.getBlockZ())
                        .append(",")
                        .append(block.getType().getId());
                writer.println(blockString);
            });
            writer.close();
        } else if (commandName.equalsIgnoreCase("loadplot")) {
        }


        return false;
    }

}
