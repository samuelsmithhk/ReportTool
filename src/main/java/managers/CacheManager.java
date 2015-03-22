package managers;

import cache.Cache;
import deal.Deal;
import files.CacheFileManager;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CacheManager {
    private static CacheManager cm;

    public static void initCacheManager(CacheFileManager cfm) {
        if (cm == null) cm = new CacheManager(cfm);
    }

    public static CacheManager getCacheManager() throws Exception {
        if (cm == null) throw new Exception("QueryManager needs to be instantiated with instance of QueryFileManager");
        return cm;
    }

    private final CacheFileManager cfm;
    private final Cache cache;

    private CacheManager(CacheFileManager cfm) {
        this.cfm = cfm;
        this.cache = cfm.getCache();
    }

    public synchronized List<String> getAllColumns() {
        return new ArrayList<String>(cache.getCols());
    }

    public void processDealUpdate(DateTime timestamp, Map<String, Deal> dealMap) {
        cache.processDealUpdate(timestamp, dealMap);
    }

    public void saveCache() {
        cfm.saveCache(cache);
    }

    public DateTime getLastUpdated() {
        return cache.getLastUpdated();
    }

    public Map<String, Deal> getDeals() {
        return cache.getDeals();
    }

    public Deal getDeal(String dealName) throws Cache.CacheException {
        return cache.getDeal(dealName);
    }

    public LocalDate getSnapshotDate() {
        String cacheName = cfm.getLatestCache();
        cacheName = cacheName.substring(cacheName.length() - 20, cacheName.length() - 12);
        return LocalDate.parse(cacheName,
                DateTimeFormat.forPattern("yyyyMMdd"));
    }
}
