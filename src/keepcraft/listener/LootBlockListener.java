//package keepcraft.listener;
//
//import java.util.ArrayList;
//import java.util.Collection;
//
//import keepcraft.services.ServiceCache;
//import keepcraft.services.UserService;
//import org.bukkit.Material;
//import org.bukkit.block.Block;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.EventPriority;
//import org.bukkit.event.Listener;
//import org.bukkit.event.block.BlockBreakEvent;
//import org.bukkit.event.block.BlockDamageEvent;
//import org.bukkit.event.block.BlockPlaceEvent;
//import keepcraft.services.ChatService;
//import keepcraft.data.DataCache;
//import keepcraft.data.models.LootBlock;
//import keepcraft.data.models.User;
//
//public class LootBlockListener implements Listener {
//
//    private UserService userService = ServiceCache.getUserService();
//
//    public LootBlockListener() {
//        Collection<LootBlock> lootBlocks = DataCache.retrieveAll(LootBlock.class);
//        for (LootBlock block : lootBlocks) {
//            block.startDispensing();
//        }
//    }
//
//    @EventHandler(priority = EventPriority.LOW)
//    public void onBlockPlace(BlockPlaceEvent event) {
//        Block placed = event.getBlock();
//        if (placed.getType() == Material.NETHER_WART_BLOCK) {
//            Player p = event.getPlayer();
//            User user = userService.getOnlineUser(p.getName());
//
//            if (user.isAdmin()) {
//                // create a loot dispenser chest
//                placed.setType(Material.CHEST);
//
//                LootBlock lootBlock = new LootBlock(0, placed);
//                DataCache.load(LootBlock.class, lootBlock);
//                lootBlock.startDispensing();
//
//                p.sendMessage(ChatService.Success + "Loot block placed");
//            }
//        }
//    }
//
//    @EventHandler(priority = EventPriority.LOW)
//    public void onBlockBreak(BlockBreakEvent event) {
//        Block broken = event.getBlock();
//        if (broken.getType() == Material.CHEST) {
//            Player p = event.getPlayer();
//            User user = userService.getOnlineUser(p.getName());
//
//            if (user.isAdmin()) {
//                // need a copy for thread safety
//                Collection<LootBlock> lootBlocks = new ArrayList<LootBlock>(DataCache.retrieveAll(LootBlock.class));
//                for (LootBlock block : lootBlocks) {
//                    if (block.getLocation().equals(broken.getLocation())) {
//                        block.stopDispensing();
//                        DataCache.delete(block);
//                        p.sendMessage(ChatService.Success + "Loot block destroyed");
//                    }
//                }
//            }
//        }
//    }
//
//    @EventHandler(priority = EventPriority.LOWEST)
//    public void onBlockDamage(BlockDamageEvent event) {
//        Block damaged = event.getBlock();
//        if (damaged.getType() == Material.CHEST) {
//            Player p = (Player) event.getPlayer();
//            User user = userService.getOnlineUser(p.getName());
//
//            if (user.isAdmin()) {
//                Collection<LootBlock> lootBlocks = new ArrayList<LootBlock>(DataCache.retrieveAll(LootBlock.class));
//                for (LootBlock block : lootBlocks) {
//                    if (block.getLocation().equals(damaged.getLocation())) {
//                        user.setTargetLootBlock(block);
//                        p.sendMessage(ChatService.Success + "Loot block targeted");
//                    }
//                }
//            }
//        }
//    }
//
//}
