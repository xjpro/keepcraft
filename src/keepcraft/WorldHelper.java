package keepcraft;

public class WorldHelper {

    public interface BlockModifier {
        public abstract void modify(int x, int y, int z);
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
}
