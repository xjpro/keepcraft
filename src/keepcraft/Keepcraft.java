package keepcraft;

import keepcraft.command.*;
import keepcraft.data.*;
import keepcraft.data.models.User;
import keepcraft.data.models.WorldPoint;
import keepcraft.listener.*;
import keepcraft.services.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Keepcraft extends JavaPlugin {

	private static final Logger logger = Logger.getLogger("Minecraft");

	// Data managers
	private final Database database = new Database("keepcraft.db");
	private final UserDataManager userDataManager = new UserDataManager(database);
	private final PlotDataManager plotDataManager = new PlotDataManager(database);
	private final FactionSpawnDataManager factionSpawnManager = new FactionSpawnDataManager(database);
	private final ContainerDataManager containerDataManager = new ContainerDataManager(database);
	private final MapDataManager mapDataManager = new MapDataManager(database);

	private final Database statsDatabase = new Database("keepcraft_stats.db");
	private final UserStatsDataManager userStatsDataManager = new UserStatsDataManager(statsDatabase);
	private final UserConnectionDataManager userConnectionDataManager = new UserConnectionDataManager(statsDatabase);

	// Services
	private final UserService userService = new UserService(this, userDataManager, userStatsDataManager, userConnectionDataManager);
	private final PlotService plotService = new PlotService(plotDataManager);
	private final FactionSpawnService factionSpawnService = new FactionSpawnService(factionSpawnManager);
	private final ContainerService containerService = new ContainerService(containerDataManager, mapDataManager);
	private final ChatService chatService = new ChatService(userService);
	private final WorldModifierService worldModifierService = new WorldModifierService(plotService, factionSpawnService, containerService);
	private final SiegeService siegeService = new SiegeService(userService, plotService, chatService);
	private final RallyService rallyService = new RallyService(chatService);
	private final AnnouncementService announcementService = new AnnouncementService(chatService);
	private final RecipeService recipeService = new RecipeService();

	@Override
	public void onEnable() {
		Bukkit.getServer().setSpawnRadius(0);

		if (plotService.getPlots().size() == 0) {
			// Nothing has been set up
			setup();
		}

		Bukkit.getServer().getOnlinePlayers().forEach(player -> {
			// Ensure user is loaded in cache
			userService.loadOfflineUser(player.getName(), player.getAddress().getHostString());
		});

		announcementService.queueAnnoucements();
		recipeService.modifyRecipes(this.getServer());

		PluginManager manager = this.getServer().getPluginManager();

		manager.registerEvents(new UserListener(userService, plotService, factionSpawnService, chatService), this);
		manager.registerEvents(new ActionListener(userService, plotService), this);
		manager.registerEvents(new MovementListener(userService, plotService, chatService), this);
		manager.registerEvents(new SneakListener(userService), this);
		manager.registerEvents(new ChatListener(userService, chatService), this);
		manager.registerEvents(new CombatListener(userService), this);
		manager.registerEvents(new WorldEntityListener(), this);
		manager.registerEvents(new ExplosionListener(plotService, chatService), this);
		manager.registerEvents(new PlotAttackListener(userService, plotService, chatService), this);
		manager.registerEvents(new PlotProtectionListener(userService, plotService, chatService), this);
		manager.registerEvents(new OutpostListener(userService, plotService, worldModifierService, chatService), this);
		manager.registerEvents(new ContainerListener(userService, containerService, chatService), this);
		manager.registerEvents(new CraftItemListener(), this);
		manager.registerEvents(new StormListener(), this);
		manager.registerEvents(new StatsListener(userService, plotService), this);
		manager.registerEvents(new FishingListener(userService, chatService), this);

		// Start any tasks
		containerService.startDispensing();

		// Basic commands
		CommandListener basicCommandListener = new BasicCommandListener(userService, plotService, rallyService, chatService);
		String[] basicCommands = {"die", "who", "map", /*"rally",*/ "global"};
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
		AdminCommandListener adminCommandListener = new AdminCommandListener(userService, plotService, chatService);
		String[] adminCommands = {"promote", "demote", "delete", "setteam", "ptp", "dawn", "noon", "dusk"};
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

		// Container commands
		CommandListener containerCommandListener = new ContainerCommandListener(userService, plotService, containerService, chatService);
		String[] containerCommands = {"chest"};
		for (String command : containerCommands) {
			getCommand(command).setExecutor(containerCommandListener);
		}

		// Siege commands
//		CommandListener siegeCommandListener = new SiegeCommandListener(userService, siegeService, chatService);
//		String[] siegeCommands = {"cap", "capture"};
//		for (String siegeCommand : siegeCommands) {
//			getCommand(siegeCommand).setExecutor(siegeCommandListener);
//		}

		log(String.format("Keepcraft enabled on '%s'", getWorld().getName()));
	}

	@Override
	public void onDisable() {
		// Stop any tasks
		containerService.stopDispensing();
		announcementService.cancelQueuedAnnouncements();

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
		World world = worldModifierService.setupWorld(Keepcraft.getWorld());
		mapDataManager.createWorldRecord(world.getUID(), new WorldPoint(world.getSpawnLocation()));

		userService.distributeKnownUsers();
		userService.refreshCache();
		plotService.refreshCache();
		factionSpawnService.refreshCache();
		containerService.refreshCache();
		log(String.format("Successfully setup new map on '%s'", world.getName()));
	}

	public static void log(String text) {
		logger.info(String.format("[Keepcraft] %s", text));
	}

	public static void error(String text) {
		logger.severe(String.format("[Keepcraft] %s", text));
	}
}
