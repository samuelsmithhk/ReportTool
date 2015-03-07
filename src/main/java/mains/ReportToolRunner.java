package mains;

import cache.Cache;
import com.google.common.collect.Maps;
import export.SheetGenerator;
import files.*;
import managers.CacheManager;
import managers.ExportManager;
import managers.QueryManager;
import managers.TemplateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import query.QueryResult;
import webservice.HttpServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ReportToolRunner {

    private final Logger logger = LoggerFactory.getLogger(ReportToolRunner.class);

    public static void main(String[] args) {
        ReportToolRunner rtr = new ReportToolRunner();
        try {
            rtr.run(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private final CacheFileManager cfm;
    private final InputFileManager ifm;
    private final MappingFileManager mfm;
    private final TemplateFileManager tfm;
    private final QueryFileManager qfm;
    private final ExportFileManager efm;
    private final Cache cache;

    private ReportToolRunner() {
        logger.info("Initializing ReportToolRunner");
        Map<String, String> properties = loadProperties();

        cfm = new CacheFileManager(properties.get("cacheDirectory"),
                Integer.valueOf(properties.get("numberOfHistoricFiles")));
        cache = cfm.getCache();
        CacheManager.initCacheManager(cache);

        ifm = new InputFileManager(cache, properties.get("everestDirectory"), properties.get("dealCentralDirectory"));
        mfm = new MappingFileManager(properties.get("mappingDirectory"));
        tfm = new TemplateFileManager(properties.get("templateDirectory"));
        TemplateManager.initTemplateManager(tfm);
        qfm = new QueryFileManager(properties.get("queryDirectory"));
        QueryManager.initQueryManager(qfm, cache);
        efm = new ExportFileManager(properties.get("exportDirectory"));
        ExportManager.initExportManager(efm);

    }

    private void run(String[] queriesToRun) throws Exception {
        logger.info("Running ReportToolRunner");

        HttpServer server = new HttpServer(8088);
        server.start();

        /**
        if (ifm.newInputs()) {

            List<InputPair> newInputs = ifm.parseNewInputs(mfm);

            for (InputPair input : newInputs) {
                logger.info("Processing update: " + input);
                cache.processDealUpdate(input.timestamp, input.dealMap);
            }

            if (newInputs.size() > 0) cfm.saveCache(cache);
        }

        List<QueryResult> results = null;
        if ((qfm.loadQueries()) || (queriesToRun.length > 0)) results = qfm.executeQueries(queriesToRun);

        if (results == null) logger.info("No queries executed");
        else for (QueryResult r : results) efm.writeExport(r.queryName, SheetGenerator.generateSheet(r, tfm),
                r.hasTemplate, r.outputTimestamp);

        logger.info("Run completed");

         **/
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
