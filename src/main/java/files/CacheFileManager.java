package files;

import cache.Cache;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CacheFileManager {

    private final Logger logger = LoggerFactory.getLogger(CacheFileManager.class);

    private final String cacheDirectory;
    private final int numberOfHistoricFiles;

    public CacheFileManager(String cacheDirectory, int numberOfHistoricFiles) {
        logger.info("Creating CacheFileManager");

        this.cacheDirectory = cacheDirectory;
        this.numberOfHistoricFiles = numberOfHistoricFiles;
    }

    public Cache getCache() {
        logger.info("Getting the cache");

        String cacheAddress = getLatestCache();

        if (cacheAddress == null) {
            logger.info("No previous cache found, generating new one");
            return Cache.createEmptyCache();
        }

        try {
            logger.info("Cache found, loading");
            byte[] encodedJSON = Files.readAllBytes(Paths.get(cacheAddress));
            String json = new String(encodedJSON, Charset.defaultCharset());

            return Cache.createLoadedCache(json);
        } catch (IOException e) {
            logger.error("No existing cache file found at " + cacheAddress + " creating new cache");
            return Cache.createEmptyCache();
        }
    }

    public void saveCache(Cache cache)  {
        logger.info("Saving cache");

        String toSave = Cache.serializeCache(cache.getDeals(), cache.getCols(), cache.getLastUpdated());

        String filename = new DateTime().toString(DateTimeFormat.forPattern("yyyyMMddHHmmss"));

        PrintWriter out;
        try {
            out = new PrintWriter(cacheDirectory + filename  + ".cache");
            out.print(toSave);
            out.close();
        } catch (FileNotFoundException e) {
            logger.error("Error saving cache file: " + e.getLocalizedMessage());
        }

        historicCleanUp();

    }

    public void historicCleanUp() {
        logger.info("Running historic cleanup of old cache files");

        File[] caches = getAllCaches();

        logger.info("There are " + caches.length + " cache files stored");

        if (caches.length > numberOfHistoricFiles) {
            logger.info("The number of cache files exceeds the limit, reducing cache files (limit is " +
                    numberOfHistoricFiles + ")");

            File earliestCache = caches[0];
            DateTime earliestTimestamp = getFileTimestamp(earliestCache);

            for (int i = 1; i < caches.length; i++) {
                File currentCache = caches[i];
                DateTime currentTimestamp = getFileTimestamp(currentCache);

                if (currentTimestamp.isBefore(earliestTimestamp)) {
                    earliestCache = currentCache;
                    earliestTimestamp = currentTimestamp;
                }
            }

            if (earliestCache.delete()) logger.info("Cache with timestamp " + earliestTimestamp + " removed.");
            else logger.error("Error deleted cache with timestamp " + earliestTimestamp);

            historicCleanUp();
        }
        else logger.info("Number of caches do not exceed limit (limit is " + numberOfHistoricFiles + ")");

    }

    public File[] getAllCaches() {
        logger.info("Getting all cache files");

        File dir = new File(cacheDirectory);

        return dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".cache");
            }
        });
    }

    public String getLatestCache() {
        logger.info("Trying to find last saved cache");

        File[] files = getAllCaches();

        if (files == null || files.length == 0) return null;

        logger.info("Previous saved caches found, identifying latest.");

        File latestFile = files[0];
        DateTime latestTimestamp = getFileTimestamp(latestFile);

        for (int i = 1; i < files.length ; i++) {
            File currentFile = files[i];
            DateTime currentTimestamp = getFileTimestamp(currentFile);

            if (currentTimestamp.isAfter(latestTimestamp)) {
                latestFile = currentFile;
                latestTimestamp = currentTimestamp;
            }
        }

        logger.info("Latest cache found: " + latestFile);
        return latestFile.getAbsolutePath();
    }

    public DateTime getFileTimestamp(File file) {
        logger.info("Identifying timestamp for file: " + file);
        return getFileTimestamp(file.getAbsolutePath());
    }

    public DateTime getFileTimestamp(String file) {
        logger.info("Parsing timestamp for string:" + file);
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        return formatter.parseDateTime(file.substring((file.length() - 20), (file.length() - 6)));
    }
}
