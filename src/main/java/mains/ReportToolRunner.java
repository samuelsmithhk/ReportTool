package mains;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import export.Email;
import files.*;
import managers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parse.ParserConfig;
import webservice.HttpServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ReportToolRunner {

    private final Logger logger = LoggerFactory.getLogger(ReportToolRunner.class);

    public static void main(String[] args) {
        try {
            new ReportToolRunner();
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

        ParserManager.initParserConfigManager(loadParserConfigs());

        InputManager.initInputManager(new InputFileManager());
        TemplateManager.initTemplateManager
                (new TemplateFileManager(properties.get("templateDirectory")));
        QueryManager.initQueryManager
                (new QueryFileManager(properties.get("queryDirectory")));
        ScheduleManager.initScheduleManager
                (new ScheduleFileManager(properties.get("scheduleDirectory")));
        ExportManager.initExportManager
                (new ExportFileManager(properties.get("exportDirectory"), properties.get("processedMacroDirectory")));

        if (properties.get("emailAuthenticate").equals("true"))
            Email.initEmail(properties.get("emailHost"), Integer.valueOf(properties.get("emailPort")),
                    Boolean.valueOf(properties.get("emailSSL")), properties.get("emailFrom"),
                    properties.get("emailUsername"), properties.get("emailPassword"));
        else
            Email.initEmail(properties.get("emailHost"), Integer.valueOf(properties.get("emailPort")),
                    Boolean.valueOf(properties.get("emailSSL")), properties.get("emailFrom"));
    }

    private List<ParserConfig> loadParserConfigs() {
        logger.info("Loading parser configurations");

        List<ParserConfig> retList = Lists.newArrayList();

        try {
            String path = ReportToolRunner.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            String decodedPath = URLDecoder.decode(path, "UTF-8");
            decodedPath = decodedPath.replace("reportTool.jar", "");
            byte[] encodedJSON = Files.readAllBytes(Paths.get(decodedPath + "parserConfig.json"));
            String json = new String(encodedJSON, Charset.defaultCharset());

            retList = ParserConfig.loadConfigs(json);
        } catch (IOException e) {
            logger.error("ERROR: Error loading parser config file: " + e.getLocalizedMessage());
            System.exit(1);
        }

        return retList;
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
