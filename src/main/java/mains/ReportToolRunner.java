package mains;

import cache.Cache;
import com.google.common.collect.Maps;
import files.CacheFileManager;
import files.InputFileManager;
import files.InputPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by samuelsmith on 08/11/2014.
 */
public class ReportToolRunner {

    private final Logger logger = LoggerFactory.getLogger(ReportToolRunner.class);

    public static void main(String[] args) {
        ReportToolRunner rtr = new ReportToolRunner();
        try {
            rtr.run();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private final Map<String, String> properties;
    private final CacheFileManager cfm;
    private final InputFileManager ifm;
    private final Cache cache;

    private ReportToolRunner() {
        logger.info("Initializing ReportToolRunner");
        properties = loadProperties();

        cfm = new CacheFileManager(properties.get("cacheDirectory"),
                Integer.valueOf(properties.get("numberOfHistoricFiles")));

        cache = cfm.getCache();
        ifm = new InputFileManager(cache, properties.get("inputDirectory"));

    }

    private void run() throws Exception {
        logger.info("Running ReportToolRunner");

        if (ifm.newInputs()) {

            List<InputPair> newInputs = ifm.parseNewInputs();

            for (InputPair input : newInputs) {
                logger.info("Processing update: " + input);
                cache.processDealUpdate(input.timestamp, input.dealMap);
            }

            if (newInputs.size() > 0) cfm.saveCache(cache);
        }
    }

    private Map<String, String> loadProperties() {
        logger.info("Loading properties");

        Map<String, String> retMap = Maps.newHashMap();

        Properties properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream("config.properties"));

            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                retMap.put(key, value);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return retMap;
    }

}
