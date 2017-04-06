package keepcraft;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class WorldHelper {

	public interface BlockModifier {
		void modify(int x, int y, int z);
	}

	public static void inCircle(int centerX, int centerZ, int startY, int endY, int radius, BlockModifier modifier) {
		for (int x = centerX - radius; x <= centerX; x++) {
			for (int z = centerZ - radius; z <= centerZ; z++) {
				if ((x - centerX) * (x - centerX) + (z - centerZ) * (z - centerZ) <= radius * radius) {
					int otherX = centerX - (x - centerX);
					int otherZ = centerZ - (z - centerZ);
					// (x, z), (x, otherZ), (otherX , z), (otherX, otherZ) are in the circle
					for (int y = startY; y <= endY; y++) {
						modifier.modify(x, y, z);
						modifier.modify(x, y, otherZ);
						modifier.modify(otherX, y, z);
						modifier.modify(otherX, y, otherZ);
					}
				}
			}
		}
	}

	public static void onCircle(int centerX, int centerZ, int startY, int endY, int radius, BlockModifier modifier) {
		for (int degree = 0; degree <= 360; degree++) {
			double x = centerX + radius * Math.cos(degree * Math.PI / 180);
			double z = centerZ + radius * Math.sin(degree * Math.PI / 180);
			for (int y = startY; y <= endY; y++) {
				modifier.modify((int) x, y, (int) z);
			}
		}
	}

	public static void inSquare(int centerX, int centerZ, int startY, int endY, int radius, BlockModifier modifier) {
		for (int x = centerX - radius; x <= centerX + radius; x++) {
			for (int z = centerZ - radius; z <= centerZ + radius; z++) {
				for (int y = startY; y <= endY; y++) {
					modifier.modify(x, y, z);
				}
			}
		}
	}

	public static int getLowestYInCircle(World world, int centerX, int centerZ, int radius) {
		final int[] currentY = {128};
		inCircle(centerX, centerZ, 0, 0, radius, (x, y, z) -> {
			Block highestBlockAt = world.getHighestBlockAt(x, z);
			if (highestBlockAt.getRelative(BlockFace.UP).getType() != Material.WATER &&
					highestBlockAt.getLocation().getBlockY() < currentY[0]) {
				currentY[0] = highestBlockAt.getLocation().getBlockY();
			}
		});
		return currentY[0];
	}
}
