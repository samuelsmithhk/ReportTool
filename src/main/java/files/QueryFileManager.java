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
import query.QueryResult;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
 * Created by samuelsmith on 13/11/2014.
 */
public class QueryFileManager {

    Logger logger = LoggerFactory.getLogger(QueryFileManager.class);

    private final Cache cache;

    private final String queryDirectory;
    private final List<Query> queries;

    public QueryFileManager(Cache cache, String queryDirectory) {
        logger.info("Creating Query File Manager");

        this.queryDirectory = queryDirectory;
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

        JsonArray headersJSON = o.getAsJsonArray("headers"), headerGroupsJSON = o.getAsJsonArray("headerGroups");

        for (int i = 0; i < headersJSON.size(); i++) {
            String header = headersJSON.get(i).getAsString();
            JsonArray headerGroupJSON = headerGroupsJSON.get(i).getAsJsonArray();

            String[] headerGroup = new String[headerGroupJSON.size()];

            for (int x = 0; x < headerGroupJSON.size(); x++) {
                headerGroup[x] = headerGroupJSON.get(x).getAsString();
            }

            qb = qb.withColumns(header, headerGroup);
        }

        String filterColumn = o.get("filterColumn").getAsString(), filterValue = o.get("filterValue").getAsString();

        qb = qb.setFilter(filterColumn, filterValue);

        JsonElement groupByJSON = o.get("groupBy");
        if (groupByJSON != null) qb = qb.setGroupBy(groupByJSON.getAsString());

        qb = qb.setSortBy(o.get("sortBy").getAsString());

        return qb.build();
    }

    public List<QueryResult> executeQueries() {
        logger.info("Executing queries");

        List<QueryResult> retList = Lists.newArrayList();

        for (Query q : queries) {
            logger.info("Executing query: " + q);
            retList.add(QueryExecutor.executeQuery(cache, q));
        }

        return retList;
    }

}
