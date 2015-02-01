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
import query.*;

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
        JsonObject o = parser.parse(json).getAsJsonObject();

        Query.QueryBuilder qb = new Query.QueryBuilder(name);

        JsonElement templateJSON = o.get("template");
        if (templateJSON != null) {
            String template = templateJSON.getAsString().trim();
            if (!(template.equals("")) && !(template.equals("false")))
                qb.setTemplate(template);
        }

        JsonElement outputTimestampJSON = o.get("outputTimestamp");
        if (outputTimestampJSON != null) {
            boolean outputTimestamp = outputTimestampJSON.getAsBoolean();
            qb.setOutputTimestamp(outputTimestamp);
        }

        JsonArray sheetsArray = o.get("sheets").getAsJsonArray();
        for (JsonElement sheet : sheetsArray) {
            JsonObject sheetO = sheet.getAsJsonObject();

            JsonElement sheetNameJSON = sheetO.get("sheetName");
            String sheetName = (sheetNameJSON != null) ? sheetNameJSON.getAsString() : "Results";
            Query.QuerySheet.QuerySheetBuilder qsb = new Query.QuerySheet.QuerySheetBuilder(sheetName);

            JsonElement isHiddenJSON = sheetO.get("hidden");
            boolean isHidden = (isHiddenJSON != null) ? isHiddenJSON.getAsBoolean() : false;
            qsb.setIsHidden(isHidden);

            JsonArray headersJSON = sheetO.getAsJsonArray("headers"),
                    headerGroupsJSON = sheetO.getAsJsonArray("headerGroups");

            for (int i = 0; i < headersJSON.size(); i++) {
                String header = headersJSON.get(i).getAsString();
                JsonArray headerGroupJSON = headerGroupsJSON.get(i).getAsJsonArray();

                String[] headerGroup = new String[headerGroupJSON.size()];

                for (int x = 0; x < headerGroupJSON.size(); x++) {
                    headerGroup[x] = headerGroupJSON.get(x).getAsString();
                }

                qsb = qsb.withColumns(header, headerGroup);
            }

            String filterColumn = sheetO.get("filterColumn").getAsString(),
                    filterValue = sheetO.get("filterValue").getAsString();

            qsb = qsb.setFilter(filterColumn, filterValue);

            JsonElement groupByJSON = sheetO.get("groupBy");
            if (groupByJSON != null) qsb = qsb.setGroupBy(groupByJSON.getAsString());

            qsb = qsb.setSortBy(sheetO.get("sortBy").getAsString());

            qb.addSheet(qsb.build());
        }

        JsonElement calculatedColumnsJSON = o.get("calculatedColumns");
        if (calculatedColumnsJSON != null) {
            JsonArray calculatedColumnsArray = calculatedColumnsJSON.getAsJsonArray();
            for (JsonElement ccJSON : calculatedColumnsArray) {
                try {
                    JsonObject ccO = ccJSON.getAsJsonObject();

                    String reference = ccO.get("reference").getAsString();

                    String header = ccO.get("header").getAsString();

                    JsonObject condition = ccO.get("condition").getAsJsonObject();
                    String firstHalf = condition.get("firstHalf").getAsString();
                    String operator = condition.get("operator").getAsString();
                    String secondHalf = condition.get("secondHalf").getAsString();
                    CalculatedColumn cc = new CalculatedColumn(header, firstHalf, operator, secondHalf);

                    qb.addCalculatedColumn(reference, cc);
                } catch (Exception e) {
                    logger.error("Unable to construct calculated column: " + e.getMessage(), e);
                }
            }
        }

        JsonElement mappedColumnsJSON = o.get("mappedColumns");
        if (mappedColumnsJSON != null) {
            JsonArray mappedColumnsArray = mappedColumnsJSON.getAsJsonArray();
            for (JsonElement mcJSON : mappedColumnsArray) {
                JsonObject mcO = mcJSON.getAsJsonObject();
                String reference = mcO.get("reference").getAsString();
                String original = mcO.get("original").getAsString();
                String header = mcO.get("header").getAsString();
                MappedColumn mc = new MappedColumn(original, header);

                qb.addMappedColumn(reference, mc);

            }
        }


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
