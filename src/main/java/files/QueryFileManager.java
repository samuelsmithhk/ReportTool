package files;

import com.google.common.collect.Maps;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import query.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Map;

public class QueryFileManager {

    private final Logger logger = LoggerFactory.getLogger(QueryFileManager.class);

    private final String queryDirectory;

    private final Gson gson;

    private boolean hasUpdate;

    public QueryFileManager(String queryDirectory) {
        logger.info("Creating Query File Manager");
        this.queryDirectory = queryDirectory;
        hasUpdate = true;
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Query.class, new Query.QuerySerializer());
        gson = builder.create();
    }

    public synchronized Map<String, Query> loadQueries() {
        logger.info("Loading queries");
        Map<String, Query> queries = Maps.newHashMap();

        File dir = new File(queryDirectory);
        File[] queryFiles = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".query");
            }
        });

        logger.info("Found {} queries", queryFiles.length);

        if (queryFiles.length == 0) return Maps.newHashMap();

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Query.class, new Query.QuerySerializer());
        Gson gson = builder.create();

        for (File f : queryFiles) {
            logger.info("Parsing query: {}", f.getName());

            try {
                byte[] encodedJSON = Files.readAllBytes(f.toPath());
                String json = new String(encodedJSON, Charset.defaultCharset());
                Query q = gson.fromJson(json, Query.class);
                queries.put(q.name, q);
            } catch (IOException e) {
                logger.error("Error parsing query {}: {}",f.getName(), e.getMessage(), e);
            }
        }

        hasUpdate = false;
        return queries;
    }

    public synchronized void saveQuery(Query newQuery) {
        String json = gson.toJson(newQuery);

        PrintWriter out;
        try {
            out = new PrintWriter(queryDirectory + newQuery.name + ".query");
            out.print(json);
            out.close();
            hasUpdate = true;
        } catch (FileNotFoundException e) {
            logger.error("Error saving query file: {}", e.getMessage());
        }
    }

    public synchronized void removeQuery(String queryName) {
        File toRemove = new File(queryDirectory + queryName + ".query");
        if (toRemove.delete()) hasUpdate = true;
    }

    public boolean hasUpdate() {
        return hasUpdate;
    }


}
