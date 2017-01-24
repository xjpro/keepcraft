package keepcraft.listener;

import keepcraft.Keepcraft;
import keepcraft.services.PlotService;
import keepcraft.services.ServiceCache;
import keepcraft.services.UserService;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import keepcraft.Chat;
import keepcraft.Privilege;
import keepcraft.data.models.Plot;
import keepcraft.data.models.PlotProtection;
import keepcraft.data.models.User;

public class ActionListener implements Listener {

    private UserService userService = ServiceCache.getUserService();
    private PlotService plotService = ServiceCache.getPlotService();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        Location from = event.getFrom();

        Player p = event.getPlayer();
        User user = userService.getOnlineUser(p.getName());

        // Code for updating player's current plot
        Plot current = user.getCurrentPlot();
        Plot candidate = plotService.getIntersectedPlot(p.getLocation());

        if (current != candidate) {
            if (current != null && candidate == null) {
                if (!current.isSpawnProtected()) {
                    p.sendMessage(Chat.Info + "Leaving " + current.getColoredName());
                }
                user.setCurrentPlot(null);
            }

            if (candidate != null) {
                if (!candidate.isSpawnProtected() && (current == null || !current.isSpawnProtected())) {
                    p.sendMessage(Chat.Info + "Entering " + candidate.getColoredName());
                }
                user.setCurrentPlot(candidate);
            }
        } else if (current != null && !current.isAdminProtected() && !current.isEventProtected()
                && current.getProtection().getPartialRadius() > 0) // we are in a plot with a partial radius
        {
            // if are going to intersects protected but didn't before
            if (current.intersectsProtectedRadius(to) && !current.intersectsProtectedRadius(from)) {
                p.sendMessage(Chat.Info + "Entering " + candidate.getColoredName() + " (Keep)");
            } // if we are not going to intersects protected but did before
            else if (!current.intersectsProtectedRadius(to) && current.intersectsProtectedRadius(from)) {
                p.sendMessage(Chat.Info + "Leaving " + candidate.getColoredName() + " (Keep)");
            }
        }
        // End plot update code

        current = user.getCurrentPlot();

        if ((current != null && current.isFactionProtected(user.getFaction()))
                || p.getGameMode() == GameMode.CREATIVE
                || event.getEventName().equals("PLAYER_TELEPORT")) {
            // Break out early conditions
        } else {
            Location lastLocation = user.getLastLocation();
            if (lastLocation == null) {
                lastLocation = from;
            }

            // Begin TNT ignition code
            Block belowBlock = p.getWorld().getBlockAt(lastLocation.getBlockX(), lastLocation.getBlockY() - 1, lastLocation.getBlockZ());

            if (belowBlock.getType() == Material.TNT) {
                // Replace with a primed TNT entity
                p.getWorld().spawn(belowBlock.getLocation(), TNTPrimed.class);
                belowBlock.setType(Material.AIR);
            }
            // End TNT ignition

            // Begin teleportation and block jump prevention
            if ((lastLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
                    && (p.getFallDistance() == 0.0F && p.getVelocity().getY() <= -0.6D)
                    && (p.getLocation().getY() > 0.0D)) {
                Keepcraft.log(String.format("A float by %s was prevented", user.getName()));

                // Find the ground
                Location blockLocation = lastLocation;
                while (blockLocation.getBlock().getType() == Material.AIR
                        && (blockLocation.getY() > 0.0D)) {
                    blockLocation.setY(blockLocation.getY() - 1.0D);
                }
                blockLocation.setY(blockLocation.getY() + 1.0D); // Block location now is ground

                p.teleport(blockLocation);
                user.setLastLocation(blockLocation);
                return;
            }
            // End tp and block jump prevention
        }

        user.setLastLocation(to);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) {
            return; // Air	
        }
        Block clicked = event.getClickedBlock();
        Material blockType = clicked.getType();

        // Notch decided to make right click with flint not set TNT on fire anymore.
        // This block of code replaces that functionality.
        if (blockType == Material.TNT
                && event.getItem().getType() == Material.FLINT_AND_STEEL
                && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            // Instead, set block on fire
            BlockFace[] facesToCheck = {BlockFace.UP, BlockFace.DOWN,
                    BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};
            for (BlockFace face : facesToCheck) {
                Block neighbor = clicked.getRelative(face);
                if (neighbor.getType().equals(Material.AIR)) {
                    neighbor.setType(Material.FIRE);
                    return;
                }
            }
        }
        // End TNT fire hack

        Plot plot = plotService.getIntersectedPlot(clicked.getLocation());
        if (plot == null || plot.getProtection() == null) {
            return;
        }

        Player p = event.getPlayer();
        User user = userService.getOnlineUser(p.getName());

        switch (blockType) {
            // Put the things we need to check against in here. Which are switches. Don't need
            // to check for any block types that don't have secondary functions because WorldListener
            // will check those.
            case STONE_BUTTON:
            case STONE_PLATE:
            case LEVER:
            case TORCH:
            case REDSTONE_TORCH_ON:
            case REDSTONE_TORCH_OFF:
            case PAINTING:
            case SIGN:
                if (plot.getProtection().getType() == PlotProtection.EVENT) {
                    // Do nothing, it's allowed
                } else if (!plot.intersectsProtectedRadius(clicked.getLocation())
                        && !plot.intersectsAdminRadius(clicked.getLocation())
                        && plot.intersectsPartialRadius(clicked.getLocation())) {
                    // Do nothing, it's allowed
                } else if (!Privilege.canInteract(user, clicked.getLocation(), plot)) {
                    if (nearDoor(clicked) && !blockType.equals(Material.STONE_PLATE)) {
                        // Get rid of it, it's blocking TNT placement near a door
                        clicked.setType(Material.AIR);
                    }
                    event.setCancelled(true);
                }
                break;
            case DISPENSER:
                if (plot.getProtection().getType() == PlotProtection.EVENT && !user.isAdmin()) {
                    event.setCancelled(true);
                }
                break;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Player p = event.getPlayer();
        User user = userService.getOnlineUser(p.getName());
        Plot plot = plotService.getIntersectedPlot(event.getBlockClicked().getLocation());
        if (!Privilege.canInteract(user, event.getBlockClicked().getLocation(), plot)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (event.getBucket() == Material.LAVA_BUCKET) {
            event.setCancelled(true);
            return;
        }

        Player p = event.getPlayer();
        User user = userService.getOnlineUser(p.getName());
        Plot plot = plotService.getIntersectedPlot(event.getBlockClicked().getLocation());
        if (!Privilege.canInteract(user, event.getBlockClicked().getLocation(), plot)) {
            event.setCancelled(true);
        }
    }

    private boolean nearDoor(Block target) {
        for (BlockFace face : BlockFace.values()) {
            if (target.getRelative(face).getType().equals(Material.IRON_DOOR_BLOCK)) {
                return true;
            }
        }
        return false;
    }

}
