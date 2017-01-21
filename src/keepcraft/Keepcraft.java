package keepcraft;

import keepcraft.listener.UserListener;
import keepcraft.listener.ChatListener;
import keepcraft.listener.WorldEntityListener;
import keepcraft.listener.StormListener;
import keepcraft.listener.BlockProtectionListener;
import keepcraft.listener.CombatListener;
import keepcraft.listener.LootBlockListener;
import keepcraft.listener.ChunkListener;
import keepcraft.listener.ExplosionListener;
import keepcraft.listener.ActionListener;
import keepcraft.data.UserDataManager;
import keepcraft.data.Database;
import keepcraft.data.DataCache;
import keepcraft.data.LootBlockDataManager;
import keepcraft.data.PlotDataManager;
import keepcraft.data.DataManager;
import keepcraft.command.BasicCommandListener;
import keepcraft.command.AdminCommandListener;
import keepcraft.command.FactionCommandListener;
import keepcraft.command.SiegeCommandListener;
import keepcraft.command.PlotCommandListener;
import keepcraft.command.ChatCommandListener;
import keepcraft.command.CommandListener;
import keepcraft.command.LootBlockCommandListener;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import keepcraft.data.models.ServerConditions;
import keepcraft.data.models.Plot;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import keepcraft.data.models.LootBlock;

public class Keepcraft extends JavaPlugin {

    private static Keepcraft instance = null;

    private static final Logger logger = Logger.getLogger("Minecraft");
    private final Database database = new Database("keepcraft.db");
    private final UserDataManager userDataManager = new UserDataManager(database);
    private final DataManager<Plot> plotDataManager = new PlotDataManager(database);
    private final DataManager<LootBlock> lootBlockDataManager = new LootBlockDataManager(database);
    private World world;

    @Override
    public void onEnable() {
        DataCache.init(userDataManager, plotDataManager, lootBlockDataManager);

        WorldLoader.loadLatest(this.getServer());

        world = this.getServer().getWorlds()
                .stream()
                .filter((predicate) -> {
                    return !predicate.getName().contains("_");
                })
                .sorted((o1, o2) -> {
                    String world1Number = o1.getName().replace("world", "");
                    String world2Number = o2.getName().replace("world", "");
                    int w1 = world1Number.equals("") ? 0 : Integer.parseInt(world1Number);
                    int w2 = world2Number.equals("") ? 0 : Integer.parseInt(world2Number);
                    if (w1 == w2) {
                        return 0;
                    } else if (w1 < w2) {
                        return 1;
                    }
                    return -1;
                })
                .findFirst()
                .get();

        ServerConditions.init(this.getConfig(), world);
        Bukkit.getServer().setSpawnRadius(0);

        PluginManager manager = this.getServer().getPluginManager();

        manager.registerEvents(new UserListener(), this);
        manager.registerEvents(new ActionListener(), this);
        manager.registerEvents(new ChatListener(), this);
        manager.registerEvents(new CombatListener(), this);
        manager.registerEvents(new WorldEntityListener(), this);
        manager.registerEvents(new ExplosionListener(), this);
        manager.registerEvents(new BlockProtectionListener(), this);
        manager.registerEvents(new ChunkListener(), this);
        manager.registerEvents(new LootBlockListener(), this);
        manager.registerEvents(new StormListener(), this);

        // Basic commands
        CommandListener basicCommandListener = new BasicCommandListener();
        String[] basicCommands = {"die", "who", "map", "rally", "global"};
        for (int i = 0; i < basicCommands.length; i++) {
            getCommand(basicCommands[i]).setExecutor(basicCommandListener);
        }

        // Chat commands
        CommandListener chatCommandListener = new ChatCommandListener();
        String[] chatCommands = {"t", "r", "g"};
        for (int i = 0; i < chatCommands.length; i++) {
            getCommand(chatCommands[i]).setExecutor(chatCommandListener);
        }

        // Admin commands
        AdminCommandListener adminCommandListener = new AdminCommandListener();
        adminCommandListener.setWorld(world);
        String[] adminCommands = {"promote", "demote", "delete", "reset", "setspawn", "setfaction", "setradius",
            "plottp", "dawn", "noon", "dusk"};
        for (String adminCommand : adminCommands) {
            getCommand(adminCommand).setExecutor(adminCommandListener);
        }

        FactionCommandListener factionCommandListener = new FactionCommandListener();
        String[] factionCommands = {"faction", "1", "2", "3"};
        for (int i = 0; i < factionCommands.length; i++) {
            getCommand(factionCommands[i]).setExecutor(factionCommandListener);
        }

        // Plot commands
        CommandListener plotCommandListener = new PlotCommandListener();
        String[] plotCommands = {"plot"};
        for (int i = 0; i < plotCommands.length; i++) {
            getCommand(plotCommands[i]).setExecutor(plotCommandListener);
        }

        // LootBlock commands
        CommandListener lootBlockCommandListener = new LootBlockCommandListener();
        String[] lootBlockCommands = {"lootblock"};
        for (int i = 0; i < lootBlockCommands.length; i++) {
            getCommand(lootBlockCommands[i]).setExecutor(lootBlockCommandListener);
        }

        // Siege commands
        CommandListener siegeCommandListener = new SiegeCommandListener();
        String[] siegeCommands = {"cap", "capture"};
        for (int i = 0; i < siegeCommands.length; i++) {
            getCommand(siegeCommands[i]).setExecutor(siegeCommandListener);
        }

        instance = this;
        log("Keepcraft enabled");
    }

    @Override
    public void onDisable() {
    }

    public static Keepcraft instance() {
        return instance;
    }

    public World getWorld() {
        return world;
    }

    public void reset() {
        WorldSetter setter = new WorldSetter();
        world = setter.reset(world);
        ServerConditions.init(this.getConfig(), world);
    }

    public static FileConfiguration config() {
        return instance.getConfig();
    }

    public static void log(String text) {
        logger.info(String.format("(KC) %s", text));
    }
}
