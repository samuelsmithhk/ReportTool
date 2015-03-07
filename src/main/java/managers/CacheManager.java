package managers;

import cache.Cache;
import files.CacheFileManager;

import java.util.ArrayList;
import java.util.List;

public class CacheManager {
    private static CacheManager cm;

    public static void initCacheManager(Cache cache) {
        if (cm == null) cm = new CacheManager(cache);
    }

    public static CacheManager getCacheManager() throws Exception {
        if (cm == null) throw new Exception("QueryManager needs to be instantiated with instance of QueryFileManager");
        return cm;
    }

    private final Cache cache;

    private CacheManager(Cache cache) {
        this.cache = cache;
    }

    public synchronized List<String> getAllColumns() {
        return new ArrayList<String>(cache.getCols());
    }

}
