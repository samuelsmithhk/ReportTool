package files;

import cache.Cache;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import query.Query;
import query.QueryExecutor;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

/**
 * Created by samuelsmith on 13/11/2014.
 */
public class QueryFileManager {

    Logger logger = LoggerFactory.getLogger(QueryFileManager.class);

    private final Cache cache;

    private final String queryDirectory, outputDirectory;
    private final List<Query> queries;

    public QueryFileManager(Cache cache, String queryDirectory, String outputDirectory) {
        logger.info("Creating Query File Manager");

        this.queryDirectory = queryDirectory;
        this.outputDirectory = outputDirectory;

        this.queries = Lists.newArrayList();
        this.cache = cache;
    }

    public boolean loadQueries() {
        logger.info("Loading queries");

        File dir = new File(queryDirectory);
        File[] queryFiles = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                logger.info(name);
                return name.endsWith(".query");
            }
        });

        logger.info("Found " + queryFiles.length + " queries");

        if (queryFiles.length == 0) return false;

        for (File f : queryFiles) {
            logger.info("Parsing query: " + f.getName());

            try {
                byte[] encodedJSON = Files.readAllBytes(f.toPath());
                String json = new String(encodedJSON, Charset.defaultCharset());

                queries.add(parseQuery(f.getName().substring(0, f.getName().indexOf(".query")), json));
            } catch (IOException e) {
                logger.error("Error parsing query " + f.getName() + ": " + e.getLocalizedMessage());
            }
        }

        return true;
    }

    private Query parseQuery(String name, String json) {
        JsonParser parser = new JsonParser();
        JsonObject o = (JsonObject) parser.parse(json);

        Query.QueryBuilder qb = new Query.QueryBuilder(name);

        JsonArray columnsJSON = o.getAsJsonArray("columns");

        for (JsonElement c : columnsJSON) {
            qb = qb.withColumn(c.getAsString());
        }

        String filterColumn = o.get("filterColumn").getAsString(), filterValue = o.get("filterValue").getAsString();

        qb = qb.setFilter(filterColumn, filterValue);
        qb = qb.setGroupBy(o.get("groupBy").getAsString());

        return qb.build();
    }

    public void executeQueries() {
        logger.info("Executing queries");
        for (Query q : queries) {
            logger.info("Executing query: " + q);
            writeQueryResult(q.name, QueryExecutor.executeQuery(cache, q));
        }
    }

    private void writeQueryResult(String filename, String result) {
        logger.info("Writing query result");

        PrintWriter out = null;
        try {
            out = new PrintWriter(outputDirectory + filename + ".json");
            out.print(result);
            out.close();
        } catch (FileNotFoundException e) {
            logger.error("Error saving query result file: " + e.getLocalizedMessage());
            if (out != null) out.close();
        }

    }

}
