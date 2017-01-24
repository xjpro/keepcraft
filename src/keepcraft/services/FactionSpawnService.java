package keepcraft.services;

import keepcraft.data.FactionSpawnDataManager;
import keepcraft.data.models.FactionSpawn;

import java.util.HashMap;

public class FactionSpawnService {

    private FactionSpawnDataManager factionSpawnDataManager = new FactionSpawnDataManager();
    private HashMap<Integer, FactionSpawn> factionSpawns;

    FactionSpawnService() {
        refreshCache();
    }

    void refreshCache() {
        factionSpawns = new HashMap<>();
        for(FactionSpawn spawn : factionSpawnDataManager.getAllData()) {
            factionSpawns.put(spawn.getFactionValue(), spawn);
        }
    }

    public FactionSpawn getFactionSpawn(int faction) {
        return factionSpawns.get(faction);
    }

    public FactionSpawn createFactionSpawn(FactionSpawn factionSpawn) {
        factionSpawnDataManager.putData(factionSpawn);
        factionSpawns.put(factionSpawn.getFactionValue(), factionSpawn);
        return factionSpawn;
    }
}
