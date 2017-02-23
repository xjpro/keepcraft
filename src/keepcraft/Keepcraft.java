package keepcraft;

import keepcraft.command.*;
import keepcraft.data.*;
import keepcraft.data.models.User;
import keepcraft.listener.*;

import java.util.logging.Logger;

import keepcraft.services.*;
import org.bukkit.Bukkit;
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

	private final Database statsDatabase = new Database("keepcraft_stats.db");
	private final UserStatsDataManager userStatsDataManager = new UserStatsDataManager(statsDatabase);

	// Services
	private final UserService userService = new UserService(userDataManager, userStatsDataManager);
	private final PlotService plotService = new PlotService(plotDataManager);
	private final FactionSpawnService factionSpawnService = new FactionSpawnService(factionSpawnManager);
	private final LootBlockService lootBlockService = new LootBlockService(this, lootBlockDataManager);
	private final ChatService chatService = new ChatService(userService);
	private final SiegeService siegeService = new SiegeService(userService, plotService, chatService);

	@Override
	public void onEnable() {
		Bukkit.getServer().setSpawnRadius(0);

		if (plotService.getPlots().size() == 0) {
			// Nothing has been set up
			setup();
		}

		Bukkit.getServer().getOnlinePlayers().forEach(player -> {
			// Ensure user is loaded in cache
			userService.loadOfflineUser(player.getName());
		});

		PluginManager manager = this.getServer().getPluginManager();

		manager.registerEvents(new UserListener(userService, plotService, factionSpawnService, chatService), this);
		manager.registerEvents(new ActionListener(userService, plotService), this);
		manager.registerEvents(new MovementListener(userService, plotService, chatService), this);
		manager.registerEvents(new ChatListener(userService, chatService), this);
		manager.registerEvents(new CombatListener(userService), this);
		manager.registerEvents(new WorldEntityListener(), this);
		manager.registerEvents(new ExplosionListener(plotService, chatService), this);
		manager.registerEvents(new PlotAttackListener(userService, plotService, chatService), this);
		manager.registerEvents(new PlotProtectionListener(userService, plotService, chatService), this);
		manager.registerEvents(new LootBlockListener(userService, lootBlockService, chatService), this);
		manager.registerEvents(new CraftItemListener(), this);
		manager.registerEvents(new StormListener(), this);
		manager.registerEvents(new StatsListener(userService, plotService), this);

		// Start any tasks
		lootBlockService.startDispensing();

		// Basic commands
		CommandListener basicCommandListener = new BasicCommandListener(userService, plotService, chatService);
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

		// Init commands
		CommandListener initCommandListener = new InitCommandListener(userService, chatService);
		String[] initCommands = {"join"};
		for (String command : initCommands) {
			getCommand(command).setExecutor(initCommandListener);
		}

		// Admin commands
		AdminCommandListener adminCommandListener = new AdminCommandListener(userService, plotService);
		String[] adminCommands = {"promote", "demote", "delete", "setfaction", "ptp", "dawn", "noon", "dusk"};
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

		// LootBlock commands
		CommandListener lootBlockCommandListener = new LootBlockCommandListener(userService, lootBlockService, chatService);
		String[] lootBlockCommands = {"lootblock"};
		for (String lootBlockCommand : lootBlockCommands) {
			getCommand(lootBlockCommand).setExecutor(lootBlockCommandListener);
		}

		// Siege commands
		CommandListener siegeCommandListener = new SiegeCommandListener(userService, siegeService, chatService);
		String[] siegeCommands = {"cap", "capture"};
		for (String siegeCommand : siegeCommands) {
			getCommand(siegeCommand).setExecutor(siegeCommandListener);
		}

		log(String.format("Keepcraft enabled on '%s'", getWorld().getName()));
	}

	@Override
	public void onDisable() {
		// Stop any tasks
		lootBlockService.startDispensing();

		// Save everybody's user data
		Bukkit.getServer().getOnlinePlayers().forEach(player -> {
			User user = userService.getOnlineUser(player.getName());
			if (user != null) {
				userDataManager.updateData(user);
			}
		});
	}

	public static Keepcraft getPlugin() {
		return (Keepcraft) Bukkit.getPluginManager().getPlugin("Keepcraft");
	}

	public static World getWorld() {
		return Bukkit.getWorld("world");
	}

	private void setup() {
		WorldSetter setter = new WorldSetter(plotService, factionSpawnService);
		World world = setter.setupWorld(Keepcraft.getWorld());
		userService.refreshCache();
		plotService.refreshCache();
		factionSpawnService.refreshCache();
		lootBlockService.refreshCache();
		log(String.format("Successfully setup map on '%s'", world.getName()));
	}

	public static void log(String text) {
		logger.info(String.format("[Keepcraft] %s", text));
	}

	public static void error(String text) {
		logger.severe(String.format("[Keepcraft] %s", text));
	}
}
