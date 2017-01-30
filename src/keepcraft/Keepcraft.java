package keepcraft;

import keepcraft.data.*;
import keepcraft.data.models.User;
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
	//private final LootBlockDataManager lootBlockDataManager = new LootBlockDataManager(database);

	// Services
	private final UserService userService = new UserService(userDataManager);
	private final PlotService plotService = new PlotService(plotDataManager);
	private final FactionSpawnService factionSpawnService = new FactionSpawnService(factionSpawnManager);
	private final ChatService chatService = new ChatService(userService);

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
		String[] adminCommands = {"promote", "demote", "delete", "setspawn", "setfaction", "plottp", "dawn", "noon", "dusk"};
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

		log(String.format("Keepcraft enabled on world '%s'", getWorld().getName()));
	}

	@Override
	public void onDisable() {
		// Save everybody's user data
		Bukkit.getServer().getOnlinePlayers().forEach(player -> {
			User user = userService.getOnlineUser(player.getName());
			if (user != null) {
				userDataManager.updateData(user);
			}
		});
	}

	public static Keepcraft instance() {
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
		log(String.format("Successfully setup map on world '%s", world.getName()));
	}

	public static void log(String text) {
		logger.info(String.format("(KC) %s", text));
	}

	public static void error(String text) {
		logger.severe(String.format("(KC) %s", text));
	}
}
