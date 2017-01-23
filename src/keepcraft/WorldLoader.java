package keepcraft;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.FilenameFilter;
import java.util.stream.Stream;


public abstract class WorldLoader {
	public static World loadLatest() {
		File file = new File(".");
		String[] allDirectories = file.list((current, name) -> new File(current, name).isDirectory());
		String lastWorldDirectory = Stream.of(allDirectories)
				.filter(dir -> dir.matches("^world(\\d+|$)"))
				.reduce((a, b) -> b) // reduce to last element
				.get();

		return Bukkit.getServer().createWorld(new WorldCreator(lastWorldDirectory));
	}
}
