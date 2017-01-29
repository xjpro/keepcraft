package keepcraft;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.util.stream.Stream;


public abstract class WorldLoader {
	public static World loadLatest() {
		File file = new File(".");
		String[] allDirectories = file.list((current, name) -> new File(current, name).isDirectory());
		String lastWorldDirectory = Stream.of(allDirectories)
				.filter(dir -> dir.matches("^world(\\d+|$)"))
				.sorted((w1, w2) -> {
					String world1StringNumber = w1.replace("world", "");
					int world1Number = world1StringNumber.length() == 0 ? 0 : Integer.parseInt(world1StringNumber);

					String world2StringNumber = w2.replace("world", "");
					int world2Number = world2StringNumber.length() == 0 ? 0 : Integer.parseInt(world2StringNumber);

					if(world1Number > world2Number) return -1;
					if(world1Number < world2Number) return 1;
					return 0;
				})
				.findFirst()
				.get();

		return Bukkit.getServer().createWorld(new WorldCreator(lastWorldDirectory));
	}
}
