package mains;

import com.google.common.collect.Maps;
import files.*;
import managers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webservice.HttpServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Properties;

public class ReportToolRunner {

    private final Logger logger = LoggerFactory.getLogger(ReportToolRunner.class);

    public static void main(String[] args) {
        try {
            ReportToolRunner rtr = new ReportToolRunner();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private ReportToolRunner() throws Exception {
        logger.info("Initializing mains.ReportToolRunner");

        HttpServer server = new HttpServer(8088);
        server.start();

        init();

        ScheduleManager sm = ScheduleManager.getScheduleManager();
        sm.startSchedule();
    }

    private void init() throws Exception {
        logger.info("Running mains.ReportToolRunner");

        Map<String, String> properties = loadProperties();

        CacheFileManager cfm = new CacheFileManager(properties.get("cacheDirectory"),
                Integer.valueOf(properties.get("numberOfHistoricFiles")));
        CacheManager.initCacheManager(cfm);

        InputManager.initInputManager
                (new InputFileManager(properties.get("everestDirectory"), properties.get("dealCentralDirectory")));
        MappingManager.initMappingManager
                (new MappingFileManager(properties.get("mappingDirectory")));
        TemplateManager.initTemplateManager
                (new TemplateFileManager(properties.get("templateDirectory")));
        QueryManager.initQueryManager
                (new QueryFileManager(properties.get("queryDirectory")));
        ScheduleManager.initScheduleManager
                (new ScheduleFileManager(properties.get("scheduleDirectory")));
        ExportManager.initExportManager
                (new ExportFileManager(properties.get("exportDirectory")));
    }

    private Map<String, String> loadProperties() {
        logger.info("Loading properties");

        Map<String, String> retMap = Maps.newHashMap();

        Properties properties = new Properties();
        try {
            String path = ReportToolRunner.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            String decodedPath = URLDecoder.decode(path, "UTF-8");
            decodedPath = decodedPath.replace("reportTool.jar", "");
            properties.load(new FileInputStream(decodedPath + "config.properties"));

            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                retMap.put(key, value);
            }

        } catch (IOException e) {
            logger.error("ERROR: Error loading properties file: " + e.getLocalizedMessage());
            System.exit(1);
        }

        return retMap;
    }


}
