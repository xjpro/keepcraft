package keepcraft.command;

import keepcraft.services.ChatService;
import keepcraft.services.PlotService;
import keepcraft.services.UserService;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import keepcraft.Privilege;
import keepcraft.Keepcraft;
import keepcraft.data.models.Plot;
import keepcraft.data.models.ServerConditions;
import keepcraft.data.models.User;
import keepcraft.data.models.UserFaction;
import keepcraft.data.models.UserPrivilege;

public class AdminCommandListener extends CommandListener {

    private World world = null;

    private final UserService userService;
    private final PlotService plotService;

    public AdminCommandListener(UserService userService, PlotService plotService) {
        this.userService = userService;
        this.plotService = plotService;
    }

    public void setWorld(World value) {
        world = value;
    }

    @Override
    protected boolean handle(String commandName, CommandSender commandSender, String[] args) {
        Player p = (Player) commandSender;
        User sender = userService.getOnlineUser(commandSender.getName());

        // Promote
        if (commandName.equals("promote") && args.length == 1) {
            if (commandSender.isOp() || Privilege.canPromote(sender)) {
                String targetName = args[0];
                User target = userService.getOnlineUser(targetName);

                if (target == null) {
                    commandSender.sendMessage(ChatService.Failure + "Requested user '" + targetName + "' does not exist");
                    return true;
                }

                int newPrivilege = target.getPrivilege() + 100;
                target.setPrivilege(newPrivilege);
                userService.updateUser(target);

                commandSender.sendMessage(ChatService.Success + "Promoted " + targetName + " to " + UserPrivilege.asString(newPrivilege));
                commandSender.getServer().getPlayer(targetName).sendMessage(ChatService.Change + "You were promoted to "
                        + UserPrivilege.asString(newPrivilege) + " status");
                return true;
            }
        } // Demote
        else if (commandName.equals("demote") && args.length == 1) {
            if (commandSender.isOp() || Privilege.canDemote(sender)) {
                String targetName = args[0];
                User target = userService.getOnlineUser(targetName);

                if (target == null) {
                    commandSender.sendMessage(ChatService.Failure + "Requested user '" + targetName + "' does not exist");
                    return true;
                }

                int newPrivilege = target.getPrivilege() - 100;
                target.setPrivilege(newPrivilege);
                userService.updateUser(target);

                commandSender.sendMessage(ChatService.Success + "Demoted " + targetName + " to " + UserPrivilege.asString(newPrivilege));
                commandSender.getServer().getPlayer(targetName).sendMessage(ChatService.Change + "You were demoted to "
                        + UserPrivilege.asString(newPrivilege) + " status");
                return true;
            }
        }
        // Set map radius
        else if (commandName.equals("setradius") && args.length == 1) {
            if (Privilege.canSetSpawn(sender)) {
                double radius;

                try {
                    radius = Double.parseDouble(args[0]);
                } catch (NumberFormatException e) {
                    // invalid input
                    commandSender.sendMessage(ChatService.Failure + "Radius must be a number");
                    return false;
                }

                ServerConditions.setMapRadius((int) radius);
                commandSender.sendMessage(ChatService.Success + "Set map radius to " + radius);
                return true;
            }
        } else if (commandName.equals("reset")) {
            Keepcraft.instance().reset();
            return true;
        }
        // Set a spawn
        else if (commandName.equals("setspawn") && args.length == 1) {
            if (Privilege.canSetSpawn(sender)) {
                int faction;

                try {
                    faction = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    // invalid input
                    commandSender.sendMessage(ChatService.Failure + "Faction must be an integer");
                    return false;
                }

                //ServerConditions.setSpawn(faction, p.getLocation());
                //commandSender.sendMessage(ChatService.Success + "Set faction " + faction + "'s spawn to " + p.getLocation());
                commandSender.sendMessage(ChatService.Failure + "Command disabled in favor of resetting the map");
                return true;
            }
        } // Set team
        else if (commandName.equals("setfaction") && args.length == 2) {
            if (Privilege.canModifyUserData(sender)) {
                String targetName = args[0];
                User target = userService.getOnlineUser(targetName);

                if (target == null) {
                    commandSender.sendMessage(ChatService.Failure + "Requested user '" + targetName + "' does not exist");
                    return true;
                }

                int faction;
                try {
                    faction = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    // invalid input
                    commandSender.sendMessage(ChatService.Failure + "Options for factions are 100 or 200");
                    return false;
                }

                target.setFaction(faction);
                userService.updateUser(target);

                commandSender.sendMessage(ChatService.Success + "Set " + targetName + " to faction " + UserFaction.asString(faction));
                commandSender.getServer().getPlayer(targetName).sendMessage(ChatService.Change + "Your faction was changed to " + UserFaction.asString(faction));
                return true;
            }
        } // Delete a user's record
        else if (commandName.equals("delete") && args.length == 1) {
            if (Privilege.canModifyUserData(sender)) {
                String targetName = args[0];

                User deleted = new User(0);
                deleted.setName(targetName.trim());
                boolean success = userService.removeUser(deleted);

                if (success) {
                    commandSender.sendMessage(ChatService.Success + "Deleted " + targetName);
                } else {
                    commandSender.sendMessage(ChatService.Failure + "Requested user '" + targetName + "' does not exist (case matters)");
                }

                return true;
            }
        } // teleport to a plot
        else if (commandName.equals("plottp") && args.length > 0) {
            if (Privilege.canModifyServerConditions(sender)) {
                Plot plot = null;
                try {
                    int orderNumber = Integer.parseInt(args[0]);
                    for (Plot possiblePlot : plotService.getPlots()) {
                        if (possiblePlot.getOrderNumber() == orderNumber) {// match
                            plot = possiblePlot;
                            break;
                        }
                    }
                } catch (NumberFormatException e) // not an int, do name
                {
                    String name = "";
                    for (int i = 0; i < args.length; i++) {
                        name += args[i] + " ";
                    }
                    plot = plotService.getPlot(name.trim());
                }

                if (plot == null) {
                    commandSender.sendMessage(ChatService.Failure + "Plot not found");
                } else {
                    p.teleport(plot.getLocation());
                }
            }
            return true;
        } // Make it dawn
        else if (commandName.equals("dawn") && args.length == 0) {
            if (Privilege.canModifyServerConditions(sender)) {
                if (world != null) {
                    world.setTime(0);
                }
            }
            return true;
        } // Make it noon
        else if (commandName.equals("noon") && args.length == 0) {
            if (Privilege.canModifyServerConditions(sender)) {
                if (world != null) {
                    world.setTime(5000);
                }
            }
            return true;
        } // Make it dusk
        else if (commandName.equals("dusk") && args.length == 0) {
            if (Privilege.canModifyServerConditions(sender)) {
                if (world != null) {
                    world.setTime(10000);
                }
            }
            return true;
        }

        if (sender.isAdmin()) {
            return false;
        } else {
            return true;
        }
    }
}
