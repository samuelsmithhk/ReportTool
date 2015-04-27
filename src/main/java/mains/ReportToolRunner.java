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
    private final ServiceManager sm;

    private ReportToolRunner() throws Exception {
        logger.info("Initializing mains.ReportToolRunner");

        ServiceManager.initServiceManager();
        sm = ServiceManager.getServiceManager();
        sm.isReady(false);
        sm.setStatus("Initializing server");
        HttpServer server = new HttpServer(8088);
        server.start();

        init();

        sm.setStatus("Starting schedule");
        ScheduleManager scm = ScheduleManager.getScheduleManager();
        scm.startSchedule();
        logger.info("Ready");
        sm.setStatus("Ready");
        sm.isReady(true);
    }

    public static void main(String[] args) {
        try {
            new ReportToolRunner();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void init() throws Exception {
        logger.info("Running mains.ReportToolRunner");

        Map<String, String> properties = loadProperties();

        sm.setStatus("Creating cache");
        CacheFileManager cfm = new CacheFileManager(properties.get("cacheDirectory"),
                Integer.valueOf(properties.get("numberOfHistoricFiles")));
        CacheManager.initCacheManager(cfm);

        sm.setStatus("Loading parser config");
        ParserManager.initParserConfigManager(loadParserConfigs());

        sm.setStatus("Init input manager");
        InputManager.initInputManager(new InputFileManager());

        sm.setStatus("Init template manager");
        TemplateManager.initTemplateManager
                (new TemplateFileManager(properties.get("templateDirectory")));

        sm.setStatus("Init query manager");
        QueryManager.initQueryManager
                (new QueryFileManager(properties.get("queryDirectory")));

        sm.setStatus("Init schedule manager");
        ScheduleManager.initScheduleManager
                (new ScheduleFileManager(properties.get("scheduleDirectory")));

        sm.setStatus("Init export manager");
        ExportManager.initExportManager
                (new ExportFileManager(properties.get("exportDirectory"), properties.get("processedMacroDirectory")));


        sm.setStatus("Init email manager");
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
            if (System.getProperty("os.name").toLowerCase().contains("win"))
                decodedPath = decodedPath.replaceFirst("/", "");

            byte[] encodedJSON = Files.readAllBytes(Paths.get(decodedPath + "parserConfig.json"));
            String json = new String(encodedJSON, Charset.defaultCharset());

            retList = ParserConfig.loadConfigs(json);
        } catch (IOException e) {
            logger.error("ERROR: Error loading parser config file: {}", e.getMessage());
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
            logger.error("ERROR: Error loading properties file: {}", e.getMessage());
            System.exit(1);
        }

        return retMap;
    }


}
