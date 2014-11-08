package files;

import cache.Cache;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by samuelsmith on 08/11/2014.
 */
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

            return Cache.createLoadedCache(json, getFileTimestamp(cacheAddress));
        } catch (IOException e) {
            logger.error("No existing cache file found at " + cacheAddress + " creating new cache");
            return Cache.createEmptyCache();
        }
    }

    public void saveCache() {
        //TODO: Implement this
        logger.info("Saving cache");
    }

    private String getLatestCache() {
        logger.info("Trying to find last saved cache");
        File dir = new File(cacheDirectory);
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".cache");
            }
        });

        if (files.length == 0) return null;

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
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
        return formatter.parseDateTime(file.substring((file.length() - 14), (file.length() - 6)));
    }
}
