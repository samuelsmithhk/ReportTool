package managers;

import cache.Cache;
import deal.Deal;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CacheManager {
    private static CacheManager cm;
    private Cache cache;

    private CacheManager(){}

    public static void initCacheManager()
    {
        if (cm == null) cm = new CacheManager();
    }

    public static CacheManager getCacheManager() throws Exception {
        if (cm == null) throw new Exception("CacheMananger needs to be instantiated");
        return cm;
    }

    public synchronized List<String> getAllColumns() {
        return new ArrayList<>(cache.getCols());
    }

    public synchronized List<String> getAllSourceSystems() {
        return new ArrayList<>(cache.getSourceSystems());
    }

    public void processDealUpdate(String sourceSystem, String directory, DateTime timestamp,
                                  Map<String, Deal> dealMap) {
        cache.processDealUpdate(sourceSystem, directory, timestamp, dealMap);
    }

    public DateTime getDirectoriesLastUpdated(String directory) {
        return cache.getDirectoriesLastUpdated(directory);
    }

    public Map<String, Deal> getDeals() {
        return cache.getDeals();
    }

    public Deal getDeal(String dealName) throws Cache.CacheException {
        return cache.getDeal(dealName);
    }

    public LocalDate getSnapshotDate() {
        return cache.getSnapshotDate();
    }

    public LocalDate getSourceSystemLastUpdated(String sourceSystem) {
        return cache.getSourceSystemLastUpdated(sourceSystem);
    }

    public void createNewCache() throws Exception {
        cache = new Cache();
        InputManager im = InputManager.getInputManager();
        im.loadNewInputsIfAny();
    }

    public void purgeOldData() {
        cache.purgeOldData();
    }
}
