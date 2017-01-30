package keepcraft;

import keepcraft.data.*;
import keepcraft.listener.*;
import keepcraft.command.BasicCommandListener;
import keepcraft.command.AdminCommandListener;
import keepcraft.command.FactionCommandListener;
import keepcraft.command.SiegeCommandListener;
import keepcraft.command.PlotCommandListener;
import keepcraft.command.ChatCommandListener;
import keepcraft.command.CommandListener;

import java.util.Set;
import java.util.logging.Logger;

import keepcraft.services.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Keepcraft extends JavaPlugin {

	private static final Logger logger = Logger.getLogger("Minecraft");

	// Data managers
	private final Database database = new Database("keepcraft.db");
	private final UserDataManager userDataManager = new UserDataManager(database);
	private final PlotDataManager plotDataManager = new PlotDataManager(database);
	private final FactionSpawnDataManager factionSpawnManager = new FactionSpawnDataManager(database);
	private final LootBlockDataManager lootBlockDataManager = new LootBlockDataManager(database);

	// Services
	private final UserService userService = new UserService(userDataManager);
	private final PlotService plotService = new PlotService(plotDataManager);
	private final FactionSpawnService factionSpawnService = new FactionSpawnService(factionSpawnManager);
	private final ChatService chatService = new ChatService(userService);

	private World world;

	@Override
	public void onEnable() {
		Bukkit.getServer().setSpawnRadius(0);

		world = WorldLoader.loadLatest();
		if (plotService.getPlots().size() == 0) {
			// Nothing has been set up
			reset();
		}

		PluginManager manager = this.getServer().getPluginManager();

		manager.registerEvents(new UserListener(userService, plotService, factionSpawnService), this);
		manager.registerEvents(new ActionListener(userService, plotService), this);
		manager.registerEvents(new ChatListener(userService, chatService), this);
		manager.registerEvents(new CombatListener(userService), this);
		manager.registerEvents(new WorldEntityListener(), this);
		manager.registerEvents(new ExplosionListener(plotService), this);
		manager.registerEvents(new PlotAttackListener(userService, plotService, chatService), this);
		manager.registerEvents(new BlockProtectionListener(userService, plotService, chatService), this);
		//manager.registerEvents(new ChunkListener(), this);
		//manager.registerEvents(new LootBlockListener(), this);
		manager.registerEvents(new StormListener(), this);

		// Basic commands
		CommandListener basicCommandListener = new BasicCommandListener(userService, plotService);
		String[] basicCommands = {"die", "who", "map", "rally", "global"};
		for (String basicCommand : basicCommands) {
			getCommand(basicCommand).setExecutor(basicCommandListener);
		}

		// ChatService commands
		CommandListener chatCommandListener = new ChatCommandListener(userService, chatService);
		String[] chatCommands = {"t", "r", "g"};
		for (String chatCommand : chatCommands) {
			getCommand(chatCommand).setExecutor(chatCommandListener);
		}

		// Admin commands
		AdminCommandListener adminCommandListener = new AdminCommandListener(userService, plotService);
		String[] adminCommands = {"promote", "demote", "delete", "reset", "setspawn", "setfaction", "plottp", "dawn", "noon", "dusk"};
		for (String adminCommand : adminCommands) {
			getCommand(adminCommand).setExecutor(adminCommandListener);
		}

		FactionCommandListener factionCommandListener = new FactionCommandListener(userService, chatService);
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
		CommandListener siegeCommandListener = new SiegeCommandListener(userService, plotService, chatService);
		String[] siegeCommands = {"cap", "capture"};
		for (String siegeCommand : siegeCommands) {
			getCommand(siegeCommand).setExecutor(siegeCommandListener);
		}

		log(String.format("Keepcraft enabled on world %s", world.getName()));
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

	public void reset() {
		Server server = Bukkit.getServer();

		boolean originallyWhiteListed = server.hasWhitelist();

		// Turn on white listing and remove everyone so nobody can join while reset is in progress
		server.setWhitelist(true);
		Set<OfflinePlayer> whitelistedPlayers = server.getWhitelistedPlayers();
		whitelistedPlayers.forEach(player -> {
			player.setWhitelisted(false);
		});

		// Remove all ops
		Set<OfflinePlayer> operators = server.getOperators();
		operators.forEach(player -> {
			player.setOp(false);
		});

		// Kick everyone
		server.getOnlinePlayers().forEach(player -> {
			player.kickPlayer("Keepcraft is resetting, please rejoin in 20 seconds...");
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

		// Restore state of white list and ops
		operators.forEach(player -> {
			player.setOp(true);
		});
		whitelistedPlayers.forEach(player -> {
			player.setWhitelisted(true);
		});
		server.setWhitelist(originallyWhiteListed);
		log("Successfully reset map on world " + world.getName());
	}

	public static void log(String text) {
		logger.info(String.format("(KC) %s", text));
	}

	public static void error(String text) {
		logger.severe(String.format("(KC) %s", text));
	}
}
