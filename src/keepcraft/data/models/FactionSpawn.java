package keepcraft.data.models;

public class FactionSpawn {

	private final int factionValue;
	private final WorldPoint worldPoint;

	public FactionSpawn(int factionValue, WorldPoint worldPoint) {
		this.factionValue = factionValue;
		this.worldPoint = worldPoint;
	}

	public int getFactionValue() {
		return factionValue;
	}

	public WorldPoint getWorldPoint() {
		return worldPoint;
	}
}
