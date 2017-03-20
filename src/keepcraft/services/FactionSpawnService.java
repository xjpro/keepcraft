package keepcraft.services;

import keepcraft.data.FactionSpawnDataManager;
import keepcraft.data.models.FactionSpawn;
import keepcraft.data.models.UserTeam;
import keepcraft.data.models.WorldPoint;

import java.util.HashMap;

public class FactionSpawnService {

	private final FactionSpawnDataManager factionSpawnDataManager;
	private HashMap<Integer, FactionSpawn> factionSpawns;

	public FactionSpawnService(FactionSpawnDataManager factionSpawnDataManager) {
		this.factionSpawnDataManager = factionSpawnDataManager;
		refreshCache();
	}

	public void refreshCache() {
		factionSpawns = new HashMap<>();
		for (FactionSpawn spawn : factionSpawnDataManager.getAllData()) {
			factionSpawns.put(spawn.getFactionValue(), spawn);
		}
	}

	public FactionSpawn getFactionSpawn(UserTeam faction) {
		return factionSpawns.get(faction.getId());
	}

	public FactionSpawn createFactionSpawn(UserTeam faction, WorldPoint worldPoint) {
		FactionSpawn factionSpawn = new FactionSpawn(faction.getId(), worldPoint);
		factionSpawnDataManager.putData(factionSpawn);
		factionSpawns.put(factionSpawn.getFactionValue(), factionSpawn);
		return factionSpawn;
	}
}
