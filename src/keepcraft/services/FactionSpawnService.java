package keepcraft.services;

import keepcraft.data.FactionSpawnDataManager;
import keepcraft.data.models.FactionSpawn;

import java.util.Collection;

public class FactionSpawnService {

    private FactionSpawnDataManager factionSpawnDataManager = new FactionSpawnDataManager();
    private Collection<FactionSpawn> factionSpawns;

    FactionSpawnService() {
        refreshCache();
    }

    void refreshCache() {
        factionSpawns = factionSpawnDataManager.getAllData();
    }

    public FactionSpawn getFactionSpawn(int faction) {
        for(FactionSpawn spawn : factionSpawns) {
            if(spawn.getFactionValue() == faction) return spawn;
        }
        return null;
    }

    public FactionSpawn createFactionSpawn(FactionSpawn factionSpawn) {
        factionSpawnDataManager.putData(factionSpawn);
        factionSpawns.add(factionSpawn);
        return factionSpawn;
    }
}
