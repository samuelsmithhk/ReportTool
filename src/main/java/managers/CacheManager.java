package managers;

import cache.Cache;
import deal.Deal;
import files.CacheFileManager;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CacheManager {
    private static CacheManager cm;
    private final CacheFileManager cfm;
    private final Cache cache;

    private CacheManager(CacheFileManager cfm) {
        this.cfm = cfm;
        this.cache = cfm.getCache();
    }

    public static void initCacheManager(CacheFileManager cfm) {
        if (cm == null) cm = new CacheManager(cfm);
    }

    public static CacheManager getCacheManager() throws Exception {
        if (cm == null) throw new Exception("QueryManager needs to be instantiated with instance of QueryFileManager");
        return cm;
    }

    public synchronized List<String> getAllColumns() {
        return new ArrayList<>(cache.getCols());
    }

    public void processDealUpdate(String sourceSystem, String directory, DateTime timestamp,
                                  Map<String, Deal> dealMap) {
        cache.processDealUpdate(sourceSystem, directory, timestamp, dealMap);
    }

    public void saveCache() {
        cfm.saveCache(cache);
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
}
