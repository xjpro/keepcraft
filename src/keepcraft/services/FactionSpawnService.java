package keepcraft.services;

import keepcraft.data.FactionSpawnDataManager;
import keepcraft.data.models.FactionSpawn;

import java.util.Collection;
import java.util.Optional;

public class FactionSpawnService {

    private FactionSpawnDataManager factionSpawnDataManager = new FactionSpawnDataManager();
    private Collection<FactionSpawn> factionSpawns;

    FactionSpawnService() {
        refreshCache();
    }

    void refreshCache() {
        factionSpawns = factionSpawnDataManager.getAllData().values();
    }

    public FactionSpawn getFactionSpawn(int faction) {
        Optional<FactionSpawn> factionSpawnOrNull = factionSpawns.stream().filter(spawn -> spawn.getFactionValue() == faction).findFirst();
        return factionSpawnOrNull.isPresent() ? factionSpawnOrNull.get() : null;
    }

    public FactionSpawn createFactionSpawn(FactionSpawn factionSpawn) {
        factionSpawnDataManager.putData(factionSpawn);
        factionSpawns.add(factionSpawn);
        return factionSpawn;
    }
}
