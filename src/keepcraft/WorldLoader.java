package keepcraft;

import org.bukkit.Server;

import java.io.File;
import java.io.FilenameFilter;
import java.util.stream.Stream;


public abstract class WorldLoader {
	public static void loadLatest(Server server) {
		File file = new File(".");
		String[] allDirectories = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});

		String lastWorldDirectory = Stream.of(allDirectories)
				.filter(dir -> dir.matches("^world(\\d+|$)"))
				.reduce((a, b) -> b) // reduce to last element
				.get();

		Keepcraft.log(lastWorldDirectory);
	}
}
