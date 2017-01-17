package keepcraft.listener;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import keepcraft.data.DataCache;
import keepcraft.data.models.LootBlock;

public class ChunkListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        for (LootBlock lootBlock : DataCache.retrieveAll(LootBlock.class)) {
            if (chunk == lootBlock.getChunk()) {
                // Leave this chunk in the game world so the loot chest it contains 
                // continues to recieve loot
                event.setCancelled(true);
            }
        }
    }

}
