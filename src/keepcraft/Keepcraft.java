package keepcraft;

import keepcraft.data.*;
import keepcraft.data.models.*;
import keepcraft.listener.*;
import keepcraft.command.BasicCommandListener;
import keepcraft.command.AdminCommandListener;
import keepcraft.command.FactionCommandListener;
import keepcraft.command.SiegeCommandListener;
import keepcraft.command.PlotCommandListener;
import keepcraft.command.ChatCommandListener;
import keepcraft.command.CommandListener;

import java.util.logging.Logger;

import keepcraft.services.*;
import org.bukkit.Bukkit;
import keepcraft.data.models.ServerConditions;
import keepcraft.data.models.Plot;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import keepcraft.data.models.LootBlock;

public class Keepcraft extends JavaPlugin {

    private static final Logger logger = Logger.getLogger("Minecraft");

    // Data managers
    private final Database database = new Database("keepcraft.db");
    private final UserDataManager userDataManager = new UserDataManager(database);
    private final DataManager<Plot> plotDataManager = new PlotDataManager(database);
    private final DataManager<FactionSpawn> factionSpawnManager = new FactionSpawnDataManager(database);
    private final DataManager<LootBlock> lootBlockDataManager = new LootBlockDataManager(database);

    // Services
    private final UserService userService = new UserService();
    private final PlotService plotService = new PlotService();
    private final FactionSpawnService factionSpawnService = new FactionSpawnService();

    private World world;

    @Override
    public void onEnable() {

        world = WorldLoader.loadLatest();
        ServerConditions.init(this.getConfig());
        Bukkit.getServer().setSpawnRadius(0);

        PluginManager manager = this.getServer().getPluginManager();

        manager.registerEvents(new UserListener(userService, plotService, factionSpawnService), this);
        manager.registerEvents(new ActionListener(userService, plotService), this);
        manager.registerEvents(new ChatListener(userService), this);
        manager.registerEvents(new CombatListener(userService), this);
        manager.registerEvents(new WorldEntityListener(), this);
        manager.registerEvents(new ExplosionListener(userService, plotService), this);
        manager.registerEvents(new BlockProtectionListener(userService, plotService), this);
        //manager.registerEvents(new ChunkListener(), this);
        //manager.registerEvents(new LootBlockListener(), this);
        manager.registerEvents(new StormListener(), this);

        // Basic commands
        CommandListener basicCommandListener = new BasicCommandListener(userService, plotService);
        String[] basicCommands = {"die", "who", "map", "rally", "global"};
        for (String basicCommand : basicCommands) {
            getCommand(basicCommand).setExecutor(basicCommandListener);
        }

        // Chat commands
        CommandListener chatCommandListener = new ChatCommandListener(userService);
        String[] chatCommands = {"t", "r", "g"};
        for (String chatCommand : chatCommands) {
            getCommand(chatCommand).setExecutor(chatCommandListener);
        }

        // Admin commands
        AdminCommandListener adminCommandListener = new AdminCommandListener(userService, plotService);
        adminCommandListener.setWorld(world);
        String[] adminCommands = {"promote", "demote", "delete", "reset", "setspawn", "setfaction", "setradius",
            "plottp", "dawn", "noon", "dusk"};
        for (String adminCommand : adminCommands) {
            getCommand(adminCommand).setExecutor(adminCommandListener);
        }

        FactionCommandListener factionCommandListener = new FactionCommandListener(userService);
        String[] factionCommands = {"faction", "1", "2", "3"};
        for (String factionCommand : factionCommands) {
            getCommand(factionCommand).setExecutor(factionCommandListener);
        }

        // Plot commands
        CommandListener plotCommandListener = new PlotCommandListener(userService, plotService);
        String[] plotCommands = {"plot"};
        for (String plotCommand : plotCommands) {
            getCommand(plotCommand).setExecutor(plotCommandListener);
        }

//        // LootBlock commands
//        CommandListener lootBlockCommandListener = new LootBlockCommandListener();
//        String[] lootBlockCommands = {"lootblock"};
//        for (int i = 0; i < lootBlockCommands.length; i++) {
//            getCommand(lootBlockCommands[i]).setExecutor(lootBlockCommandListener);
//        }

        // Siege commands
        CommandListener siegeCommandListener = new SiegeCommandListener(userService);
        String[] siegeCommands = {"cap", "capture"};
        for (String siegeCommand : siegeCommands) {
            getCommand(siegeCommand).setExecutor(siegeCommandListener);
        }

        log("Keepcraft enabled");
    }

    @Override
    public void onDisable() {
    }

    public static Keepcraft instance() {
        return (Keepcraft) Bukkit.getPluginManager().getPlugin("Keepcraft");
    }

    public static World getWorld() {
        return instance().world;
    }

    public static Database getSqlLiteDatabase() { return instance().database; }

    public void reset() {
        Server server = Bukkit.getServer();

        boolean originallyWhiteListed = server.hasWhitelist();

        // Turn on white listing and remove everyone so nobody can join while reset is in progress
        server.setWhitelist(true);
		server.getWhitelistedPlayers().forEach(player -> {
            player.setWhitelisted(false);
        });

        // Kick everyone
        server.getOnlinePlayers().forEach(player -> {
            player.kickPlayer("Keepcraft is resetting, please rejoin in 15 seconds...");
        });

        // Clean database
        plotDataManager.truncate();
        factionSpawnManager.truncate();
        lootBlockDataManager.truncate();
        userDataManager.resetNonAdminUserData();

        WorldSetter setter = new WorldSetter(plotService, factionSpawnService);
        world = setter.reset(world);
        getConfig().set("spawn.world", world.getName());

        userService.refreshCache();
        plotService.refreshCache();
        factionSpawnService.refreshCache();

        // Restore state of white list
        server.setWhitelist(originallyWhiteListed);
        server.reloadWhitelist();
    }

    public static FileConfiguration config() {
        return instance().getConfig();
    }

    public static void log(String text) {
        logger.info(String.format("(KC) %s", text));
    }
    public static void error(String text) {
        logger.severe(String.format("(KC) %s", text));
    }
}
