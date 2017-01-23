package keepcraft.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import keepcraft.data.models.*;

public abstract class DataCache {

    private static Map<String, User> userCache;
    private static Map<Object, Plot> plotCache;
    private static Map<Object, FactionSpawn> factionSpawnCache;
    private static Map<Object, LootBlock> lootBlockCache;

    private static UserDataManager userDataManager;
    private static DataManager<Plot> plotDataManager;
    private static DataManager<FactionSpawn> factionSpawnDataManager;
    private static DataManager<LootBlock> lootBlockDataManager;

    public static void init(UserDataManager userManager, DataManager<Plot> plotManager, DataManager<FactionSpawn> factionSpawnManager, DataManager<LootBlock> lootBlockManager) {
        userDataManager = userManager;
        plotDataManager = plotManager;
        factionSpawnDataManager = factionSpawnManager;
        lootBlockDataManager = lootBlockManager;

        userCache = new HashMap<>();
        plotCache = plotDataManager.getAllData();
        factionSpawnCache = factionSpawnDataManager.getAllData();
        lootBlockCache = lootBlockDataManager.getAllData();
    }

    /**
     * In order for the cache to work, data must be loaded in before it's
     * needed.
     *
     * @param type              Type of object to cache
     * @param key               Key of object
     * @param pairedWorldObject An object from the game world that pairs with
     *                          the database data
     */
    public static <T> void load(Class<T> type, Object value) {

        if (value == null) {
            return;
        }

        if (type == User.class) {
            String name = (String) value;
            userCache.put(name.toLowerCase(), userDataManager.getData(name));
        } else if (type == Plot.class) {
            // all plots should always be cached, so this must be a create command?
            Plot plot = (Plot) value;
            plotDataManager.putData(plot);
            plotCache = plotDataManager.getAllData();
        } else if (type == FactionSpawn.class) {
            FactionSpawn spawn = (FactionSpawn) value;
            factionSpawnDataManager.putData(spawn);
            factionSpawnCache = factionSpawnDataManager.getAllData();
        } else if (type == LootBlock.class) {
            // always loaded, so put
            LootBlock block = (LootBlock) value;
            lootBlockDataManager.putData(block);
            lootBlockCache = lootBlockDataManager.getAllData();
        }
    }

    /**
     * Good practice to unload data that's no longer in use.
     *
     * @param type
     * @param key
     */
    public static <T> void unload(Class<T> type, Object key) {

        if (key == null) {
            return;
        }

        if (type == User.class) {
            String nameKey = (String) key;
            User user = userCache.remove(nameKey.toLowerCase());

            if (user != null) {
                // Final save
                userDataManager.updateData(user);
            }
        } else if (type == Plot.class) {
            plotCache.remove(key);
        } else if (type == FactionSpawn.class) {
            factionSpawnCache.remove(key);
        } else if (type == LootBlock.class) {
            lootBlockCache.remove(key);
        }
    }

    public static <T> boolean exists(Class<T> type, Object key) {
        if (key == null) {
            return false;
        }

        if (type == User.class) {
            String nameKey = (String) key;
            return userDataManager.exists(nameKey);
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public static <T> T retrieve(Class<T> type, Object key) {
        if (key == null) {
            return null;
        }

        if (type == User.class) {
            String nameKey = (String) key;
            return (T) userCache.get(nameKey.toLowerCase());
        } else if (type == Plot.class) {
            if (key instanceof String) {
                String name = (String) key;
                // we have a name, search through the entire cache
                for (Plot plot : plotCache.values()) {
                    if (plot.getName().equalsIgnoreCase(name)) {
                        return (T) plot;
                    }
                }
            } else {
                // should be an Integer (base key type)
                return (T) plotCache.get(key);
            }
        } else if (type == FactionSpawn.class) {
            return (T) factionSpawnCache.get(key);
        } else if (type == LootBlock.class) {
            return (T) lootBlockCache.get(key);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> Collection<T> retrieveAll(Class<T> type) {
        if (type == User.class) {
            return (Collection<T>) userCache.values();
        } else if (type == Plot.class) {
            return (Collection<T>) plotCache.values();
        } else if (type == FactionSpawn.class) {
            return (Collection<T>) factionSpawnCache.values();
        } else if (type == LootBlock.class) {
            return (Collection<T>) lootBlockCache.values();
        }

        return null;
    }

    public static boolean update(Object value) {
        if (value instanceof User) {
            User user = (User) value;

            if (!exists(User.class, user.getName())) {
                return false;
            }

            userDataManager.updateData(user);
        } else if (value instanceof Plot) {
            Plot plot = (Plot) value;
            plotDataManager.updateData(plot);
        } else if (value instanceof FactionSpawn) {
            factionSpawnDataManager.updateData((FactionSpawn) value);
        } else if (value instanceof LootBlock) {
            LootBlock plot = (LootBlock) value;
            lootBlockDataManager.updateData(plot);
        }

        return true;
    }

    public static boolean delete(Object value) {
        if (value instanceof User) {
            User user = (User) value;

            if (!exists(User.class, user.getName())) {
                return false;
            }

            unload(User.class, user.getName());
            userDataManager.deleteData(user);
        } else if (value instanceof Plot) {
            Plot plot = (Plot) value;
            unload(Plot.class, plot.getId());
            plotDataManager.deleteData(plot);
        } else if (value instanceof FactionSpawn) {
            FactionSpawn factionSpawn = (FactionSpawn) value;
            unload(Plot.class, factionSpawn.getFactionValue());
            factionSpawnDataManager.deleteData(factionSpawn);
        } else if (value instanceof LootBlock) {
            LootBlock block = (LootBlock) value;
            unload(LootBlock.class, block.getId());
            lootBlockDataManager.deleteData(block);
        }

        return true;
    }

    public static void clear() {
        lootBlockCache.clear();
        plotCache.clear();
        factionSpawnCache.clear();
        userCache.clear();
    }

}
