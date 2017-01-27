package keepcraft.listener;

import keepcraft.Keepcraft;
import keepcraft.services.ChatService;
import keepcraft.services.PlotService;
import keepcraft.services.UserService;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import keepcraft.Privilege;
import keepcraft.data.models.Plot;
import keepcraft.data.models.PlotProtection;
import keepcraft.data.models.User;

public class ActionListener implements Listener {

    private final UserService userService;
    private final PlotService plotService;

    public ActionListener(UserService userService, PlotService plotService) {
        this.userService = userService;
        this.plotService = plotService;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        Location from = event.getFrom();

        Player p = event.getPlayer();
        User user = userService.getOnlineUser(p.getName());

        // Code for updating player's current plot
        Plot currentPlot = user.getCurrentPlot();
        Plot candidatePlot = plotService.getIntersectedPlot(p.getLocation());

        if (currentPlot != candidatePlot) {
            if (currentPlot != null && candidatePlot == null) {
                if (!currentPlot.isSpawnProtected()) {
                    p.sendMessage(ChatService.Info + "Leaving " + currentPlot.getColoredName());
                }
                user.setCurrentPlot(null);
            }

            if (candidatePlot != null) {
                if (!candidatePlot.isSpawnProtected() && (currentPlot == null || !currentPlot.isSpawnProtected())) {
                    p.sendMessage(ChatService.Info + "Entering " + candidatePlot.getColoredName());
                }
                user.setCurrentPlot(candidatePlot);
            }
        } else if (currentPlot != null && !currentPlot.isAdminProtected() && !currentPlot.isEventProtected()
                && currentPlot.getProtection().getPartialRadius() > 0) // we are in a plot with a partial radius
        {
            // if are going to intersects protected but didn't before
            if (currentPlot.isInTeamProtectedRadius(to) && !currentPlot.isInTeamProtectedRadius(from)) {
                p.sendMessage(ChatService.Info + "Entering " + candidatePlot.getColoredName() + " (Keep)");
            } // if we are not going to intersects protected but did before
            else if (!currentPlot.isInTeamProtectedRadius(to) && currentPlot.isInTeamProtectedRadius(from)) {
                p.sendMessage(ChatService.Info + "Leaving " + candidatePlot.getColoredName() + " (Keep)");
            }
        }
        // End plot update code

        currentPlot = user.getCurrentPlot();

        if ((currentPlot != null && currentPlot.isFactionProtected(user.getFaction()))
                || p.getGameMode() == GameMode.CREATIVE
                || event.getEventName().equals("PLAYER_TELEPORT")) {
            // Considers which are not subject to float prevention
            return;
        }

        // Begin invalid teleportation and float prevention
        if ((from.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
                && (p.getFallDistance() == 0.0F && p.getVelocity().getY() <= -0.6D)
                && (p.getLocation().getY() > 0.0D)) {
            Keepcraft.log(String.format("A float by %s was prevented", user.getName()));

            // Find the ground
            Location groundLocation = from.clone();
            while (groundLocation.getBlock().getType() == Material.AIR && groundLocation.getY() > 0) {
                groundLocation.add(0, -1, 0);
            }
            groundLocation.add(0, 1, 0); // Ground located
            p.teleport(groundLocation);
        }
        // End invalid teleportation and float prevention
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) {
            return; // Air
        }
        Block clicked = event.getClickedBlock();
        Material blockType = clicked.getType();

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
                } else if (!plot.isInTeamProtectedRadius(clicked.getLocation())
                        && !plot.isInAdminProtectedRadius(clicked.getLocation())
                        && plot.isInPartialRadius(clicked.getLocation())) {
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
        User user = userService.getOnlineUser(event.getPlayer().getName());
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
