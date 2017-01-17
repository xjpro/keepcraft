package keepcraft.command;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import keepcraft.Chat;
import keepcraft.Keepcraft;
import keepcraft.Privilege;
import keepcraft.data.DataCache;
import keepcraft.data.models.Plot;
import keepcraft.data.models.PlotProtection;
import keepcraft.data.models.User;
import keepcraft.data.models.UserFaction;
import keepcraft.data.models.UserPrivilege;
import keepcraft.data.models.WorldPoint;

public class PlotCommandListener extends CommandListener {

    @Override
    protected boolean handle(String commandName, CommandSender commandSender, String[] args) {
        User sender = DataCache.retrieve(User.class, commandSender.getName());
        int privilege = sender.getPrivilege();

        Plot currentPlot = sender.getCurrentPlot();

        if (commandName.equalsIgnoreCase("plot")) {
            // Create a plot
            if (args.length > 1 && args[0].equals("create")) {
                // Create plot
                if (privilege == UserPrivilege.ADMIN) {
                    Player p = (Player) commandSender;
                    Location loc = p.getLocation();
                    String name = "";
                    for (int i = 1; i < args.length; i++) {
                        name += args[i] + " ";
                    }

                    Plot plot = new Plot();
                    plot.setLocation(new WorldPoint("main", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                    plot.setRadius(Plot.DEFAULT_RADIUS);
                    plot.setName(name.trim());
                    plot.setSetterId(sender.getId());

                    PlotProtection protection = new PlotProtection(-1);
                    protection.setType(PlotProtection.ADMIN);
                    protection.setAdminRadius(Plot.DEFAULT_RADIUS);
                    protection.setProtectedRadius(Plot.DEFAULT_RADIUS);
                    protection.setTriggerRadius(Plot.DEFAULT_TRIGGER_RADIUS);
                    protection.setChestLevel(Plot.DEFAULT_CHEST_LEVEL);
                    protection.setCapturable(false);

                    plot.setProtection(protection);

                    DataCache.load(Plot.class, plot);
                    commandSender.sendMessage(Chat.Success + "A new plot has been created");
                    return true;
                }
            } // Past this point we should be in a plot, so break out early if not
            else if (currentPlot == null) {
                commandSender.sendMessage(Chat.Failure + "You are not in a plot");
                return true;
            } // Get info
            else if (args.length == 0) {
                String[] messages = currentPlot.getInfo().split("\n");
                for (int i = 0; i < messages.length; i++) {
                    commandSender.sendMessage(Chat.RequestedInfo + messages[i]);
                }
                return true;
            } // Reset center
            else if (args[0].equalsIgnoreCase("center")) {
                if (privilege == UserPrivilege.ADMIN) {
                    Player p = (Player) commandSender;
                    Location loc = p.getLocation();

                    currentPlot.setLocation(new WorldPoint(loc));
                    DataCache.update(currentPlot);

                    commandSender.sendMessage(Chat.Success + "Plot center set to " + loc);
                    return true;
                }
            } // Set base radius
            else if (args[0].equals("radius")) {
                if (args.length == 2) {
                    if (privilege == UserPrivilege.ADMIN) {
                        int radius;
                        try {
                            radius = Integer.parseInt(args[1]);
                        } catch (Exception e) {
                            // invalid input
                            return false;
                        }
                        currentPlot.setRadius(radius);
                        DataCache.update(currentPlot);

                        commandSender.sendMessage(Chat.Success + "Plot radius set to " + currentPlot.getRadius());
                        return true;
                    }
                } else if (args[1].equalsIgnoreCase("protected") && args.length == 3) {
                    if (privilege == UserPrivilege.ADMIN) {
                        int radius;
                        try {
                            radius = Integer.parseInt(args[2]);
                        } catch (Exception e) {
                            Keepcraft.log("Error while changing protected radius: " + e.getMessage());
                            return false;
                        }
                        currentPlot.getProtection().setProtectedRadius(radius);
                        DataCache.update(currentPlot);

                        commandSender.sendMessage(Chat.Success + "Plot protected radius set to " + currentPlot.getProtection().getProtectedRadius());
                        return true;
                    }
                } else if (args[1].equalsIgnoreCase("partial") && args.length == 3) {
                    if (privilege == UserPrivilege.ADMIN) {
                        int radius;
                        try {
                            radius = Integer.parseInt(args[2]);
                        } catch (Exception e) {
                            Keepcraft.log("Error while changing partial: " + e.getMessage());
                            return false;
                        }
                        currentPlot.getProtection().setPartialRadius(radius);
                        DataCache.update(currentPlot);

                        commandSender.sendMessage(Chat.Success + "Plot partial radius set to " + currentPlot.getProtection().getPartialRadius());
                        return true;
                    }
                } else if (args[1].equalsIgnoreCase("admin") && args.length == 3) {
                    if (privilege == UserPrivilege.ADMIN) {
                        int radius;
                        try {
                            radius = Integer.parseInt(args[2]);
                        } catch (Exception e) {
                            Keepcraft.log("Error while changing admin radius: " + e.getMessage());
                            return false;
                        }
                        currentPlot.getProtection().setAdminRadius(radius);
                        DataCache.update(currentPlot);

                        commandSender.sendMessage(Chat.Success + "Plot admin radius set to " + currentPlot.getProtection().getAdminRadius());
                        return true;
                    }
                } else if (args[1].equalsIgnoreCase("trigger") && args.length == 3) {
                    if (privilege == UserPrivilege.ADMIN) {
                        int radius;
                        try {
                            radius = Integer.parseInt(args[2]);
                        } catch (Exception e) {
                            Keepcraft.log("Error while changing trigger radius: " + e.getMessage());
                            return false;
                        }
                        currentPlot.getProtection().setTriggerRadius(radius);
                        DataCache.update(currentPlot);

                        commandSender.sendMessage(Chat.Success + "Plot trigger radius set to " + currentPlot.getProtection().getTriggerRadius());
                        return true;
                    }
                }
            } // end radius
            else if (args[0].equalsIgnoreCase("chest") && args.length == 2) {
                if (privilege == UserPrivilege.ADMIN) {
                    int chestLevel;
                    try {
                        chestLevel = Integer.parseInt(args[1]);
                    } catch (Exception e) {
                        Keepcraft.log("Error while changing chest level: " + e.getMessage());
                        return false;
                    }
                    currentPlot.getProtection().setChestLevel(chestLevel);
                    DataCache.update(currentPlot);

                    commandSender.sendMessage(Chat.Success + "Plot chest level set to " + currentPlot.getProtection().getChestLevel());
                    return true;
                }
            } // Capturable setting
            else if (args[0].equals("capture") && args.length > 1) {
                if (args[1].equalsIgnoreCase("type")) {
                    if (Privilege.canModifyPlotData(sender, currentPlot)) {
                        boolean capturable;
                        try {
                            capturable = Boolean.parseBoolean(args[2]);
                        } catch (Exception e) {
                            // invalid input
                            Keepcraft.log("Error while changing plot capturable flag: " + e.getMessage());
                            return false;
                        }

                        currentPlot.getProtection().setCapturable(capturable);
                        DataCache.update(currentPlot);

                        commandSender.sendMessage(Chat.Success + "Plot capturable flag set to " + capturable);
                        return true;
                    }
                } else if (args[1].equalsIgnoreCase("time")) {
                    if (Privilege.canModifyPlotData(sender, currentPlot)) {
                        int captureSeconds;
                        try {
                            captureSeconds = Integer.parseInt(args[2]);
                        } catch (Exception e) {
                            Keepcraft.log("Error while changing plot capture time: " + e.getMessage());
                            // invalid input
                            return false;
                        }

                        currentPlot.getProtection().setCaptureTime(captureSeconds);
                        DataCache.update(currentPlot);

                        commandSender.sendMessage(Chat.Success + "Plot capture time set to " + captureSeconds + " seconds");
                        return true;
                    }
                }
            } else if (args[0].equals("type") && args.length > 1) {
                // Set it as protection faction
                if (args[1].equals("faction") && args.length == 3) {
                    if (privilege == UserPrivilege.ADMIN) {
                        int faction;
                        try {
                            faction = Integer.parseInt(args[2]);
                        } catch (Exception e) {
                            // invalid input
                            Keepcraft.log("Error while setting plot type to faction: " + e.getMessage());
                            return false;
                        }
                        currentPlot.getProtection().setType(faction);
                        DataCache.update(currentPlot);

                        commandSender.sendMessage(Chat.Success + "Plot protection set to faction " + UserFaction.asString(faction));
                        return true;
                    }
                } // Set it as protection admin
                else if (args[1].equals("admin") && args.length == 2) {
                    if (privilege == UserPrivilege.ADMIN) {
                        currentPlot.getProtection().setType(PlotProtection.ADMIN);
                        DataCache.update(currentPlot);

                        commandSender.sendMessage(Chat.Success + "Plot protection set to admin");
                        return true;
                    }
                } // Set it as protection admin
                else if (args[1].equals("event") && args.length == 2) {
                    if (privilege == UserPrivilege.ADMIN) {
                        currentPlot.getProtection().setType(PlotProtection.EVENT);
                        DataCache.update(currentPlot);

                        commandSender.sendMessage(Chat.Success + "Plot protection set to event");
                        return true;
                    }
                } // Set it as protection spawn
                else if (args[1].equals("spawn") && args.length == 2) {
                    if (privilege == UserPrivilege.ADMIN) {
                        currentPlot.getProtection().setType(PlotProtection.SPAWN);
                        DataCache.update(currentPlot);

                        commandSender.sendMessage(Chat.Success + "Plot protection set to spawn");
                        return true;
                    }
                } // Set it as protection admin
                else if (args[1].equals("public") && args.length == 2) {
                    if (Privilege.canModifyPlotData(sender, currentPlot)) {
                        currentPlot.getProtection().setType(PlotProtection.PUBLIC);
                        DataCache.update(currentPlot);

                        commandSender.sendMessage(Chat.Success + "Plot protection set to public");
                        return true;
                    }
                }
            } // Rename plot
            else if (args[0].equals("name") && args.length > 1) {
                if (Privilege.canModifyPlotData(sender, currentPlot)) {
                    String name = "";
                    for (int i = 1; i < args.length; i++) {
                        name += args[i] + " ";
                    }

                    currentPlot.setName(name.trim());
                    DataCache.update(currentPlot);
                    commandSender.sendMessage(Chat.Success + "Plot renamed to " + currentPlot.getName());
                    return true;
                }
            } // Set plot's order number
            else if (args[0].equals("order") && args.length == 2) {
                if (Privilege.canModifyPlotData(sender, currentPlot)) {
                    int orderNumber;
                    try {
                        orderNumber = Integer.parseInt(args[1]);
                    } catch (Exception e) {
                        Keepcraft.log("Error while setting plot order number: " + e.getMessage());
                        return false;
                    }

                    currentPlot.setOrderNumber(orderNumber);
                    DataCache.update(currentPlot);
                    commandSender.sendMessage(Chat.Success + "Plot order number set to " + currentPlot.getOrderNumber());
                    return true;
                }
            } // Delete to default
            else if (args[0].equals("delete")) {
                if (Privilege.canModifyPlotData(sender, currentPlot)) {
                    DataCache.delete(currentPlot);
                    commandSender.sendMessage(Chat.Success + "Plot deleted");
                    return true;
                }
            }
        }

        return false;
    }

}
