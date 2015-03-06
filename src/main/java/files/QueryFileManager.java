package files;

import cache.Cache;
import com.google.common.collect.Lists;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import query.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;


public class QueryFileManager {

    Logger logger = LoggerFactory.getLogger(QueryFileManager.class);

    private final String queryDirectory;

    private boolean hasUpdate;

    public QueryFileManager(String queryDirectory) {
        logger.info("Creating Query File Manager");
        this.queryDirectory = queryDirectory;
        hasUpdate = true;
    }

    public synchronized List<Query> loadQueries() {
        logger.info("Loading queries");
        List<Query> queries = Lists.newArrayList();

        File dir = new File(queryDirectory);
        File[] queryFiles = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                logger.info(name);
                return name.endsWith(".query");
            }
        });

        logger.info("Found " + queryFiles.length + " queries");

        if (queryFiles.length == 0) return Lists.newArrayList();

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Query.class, new Query.QuerySerializer());
        Gson gson = builder.create();

        for (File f : queryFiles) {
            logger.info("Parsing query: " + f.getName());

            try {
                byte[] encodedJSON = Files.readAllBytes(f.toPath());
                String json = new String(encodedJSON, Charset.defaultCharset());

                queries.add(gson.fromJson(json, Query.class));
            } catch (IOException e) {
                logger.error("Error parsing query " + f.getName() + ": " + e.getLocalizedMessage());
            }
        }

        hasUpdate = false;
        return queries;
    }


    /*public List<QueryResult> executeQueries(String[] queriesToRun) {
        logger.info("Executing queries");

        List<QueryResult> retList = Lists.newArrayList();

        if (queriesToRun.length == 0)
            for (Query q : queries) {
                    logger.info("Executing query: " + q);
                    retList.add(QueryExecutor.executeQuery(cache, q));
            }
        else
            for (String s : queriesToRun)

                    for (Query q : queries)
                        if (q.name.equals(s)) {
                            logger.info("Executing query: " + q);
                            retList.add(QueryExecutor.executeQuery(cache, q));
                            break;
                        }
        return retList;
    } */

    public boolean hasUpdate() {
        return hasUpdate;
    }
}
