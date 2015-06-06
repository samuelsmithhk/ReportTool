package query;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class Query {

    private static transient Logger logger = LoggerFactory.getLogger(Query.class);

    public final List<QuerySheet> sheets;
    public final Map<String, CalculatedColumn> calculatedColumns;
    public final Map<String, MappedColumn> mappedColumns;
    public final String name, templateName;
    public final boolean hasTemplate, outputTimestamp;

    private Query(QueryBuilder qb) {
        this.sheets = qb.sheets;
        this.calculatedColumns = qb.calculatedColumns;
        this.mappedColumns = qb.mappedColumns;
        this.name = qb.name;
        this.templateName = qb.templateName;
        this.hasTemplate = qb.hasTemplate;
        this.outputTimestamp = qb.outputTimestamp;
    }

    @Override
    public String toString() {
        return name;
    }

    public SpecialColumn getSpecialColumn(String reference) throws SpecialColumn.SpecialColumnException {
        if (reference.startsWith("=")) {
            reference = reference.substring(1);
            if (calculatedColumns.containsKey(reference)) return calculatedColumns.get(reference);
            throw new SpecialColumn.SpecialColumnException("Column does not exist: =" + reference);
        } else if (reference.startsWith("$")) {
            reference = reference.substring(1);
            if (mappedColumns.containsKey(reference)) return mappedColumns.get(reference);
            throw new SpecialColumn.SpecialColumnException("Column does not exist: $" + reference);
        }
        throw new SpecialColumn.SpecialColumnException("Invalid reference: " + reference);
    }


    public static class QueryBuilder {
        List<QuerySheet> sheets;
        Map<String, CalculatedColumn> calculatedColumns;
        Map<String, MappedColumn> mappedColumns;
        String name, templateName;
        boolean hasTemplate, outputTimestamp;

        public QueryBuilder(String name) {
            this.name = name;
            this.sheets = Lists.newLinkedList();
            this.calculatedColumns = Maps.newHashMap();
            this.mappedColumns = Maps.newHashMap();
            this.templateName = null;
            this.hasTemplate = false;
            this.outputTimestamp = false;
        }

        public QueryBuilder addSheet(QuerySheet sheet) {
            this.sheets.add((sheet));
            return this;
        }

        public QueryBuilder addCalculatedColumn(String columnName, CalculatedColumn column) {
            this.calculatedColumns.put(columnName, column);
            return this;
        }

        public QueryBuilder addMappedColumn(String columnName, MappedColumn column) {
            this.mappedColumns.put(columnName, column);
            return this;
        }

        public QueryBuilder setTemplate(String templateName) {
            this.templateName = templateName;
            this.hasTemplate = true;
            return this;
        }

        public QueryBuilder setOutputTimestamp(boolean outputTimestamp) {
            this.outputTimestamp = outputTimestamp;
            return this;
        }

        public Query build() {
            return new Query(this);
        }
    }


    public static class QuerySheet {
        public final List<Header> headers;
        public final String sheetName, groupBy, filterColumn, filterValue, sortBy, ssPriority;
        public final boolean isHidden, allowFallback;

        private final QuerySheetBuilder qsb;

        private QuerySheet(QuerySheetBuilder qsb) {
            this.sheetName = qsb.sheetName;
            this.headers = qsb.headers;
            this.groupBy = qsb.groupBy;
            this.filterColumn = qsb.filterColumn;
            this.filterValue = qsb.filterValue;
            this.sortBy = qsb.sortBy;
            this.isHidden = qsb.isHidden;
            this.ssPriority = qsb.ssPriority;
            this.allowFallback = qsb.allowFallback;

            this.qsb = qsb;
        }

        public QuerySheetBuilder toQuerySheetBuilder() {
            return qsb;
        }

        public static class QuerySheetBuilder {
            private List<Header> headers;
            private String sheetName, groupBy, filterColumn, filterValue, sortBy, ssPriority;
            private boolean isHidden, allowFallback;

            public QuerySheetBuilder(String sheetName) {
                this.sheetName = sheetName;
                headers = null;
                groupBy = null;
                filterColumn = null;
                filterValue = null;
                sortBy = null;
                isHidden = false;
                ssPriority = null;
            }

            public QuerySheetBuilder withColumn(String header, String column) {
                if (this.headers == null) this.headers = Lists.newLinkedList();

                for (Header h : headers)
                    if (h.isThisHeader(header)) {
                        h.addSub(column);
                        return this;
                    }

                Header _header = new Header(header);
                _header.addSub(column);
                this.headers.add(_header);
                return this;
            }

            public QuerySheetBuilder setFilter(String filterColumn, String filterValue) {
                this.filterColumn = filterColumn;
                this.filterValue = filterValue;
                return this;
            }

            public QuerySheetBuilder setIsHidden(boolean isHidden) {
                this.isHidden = isHidden;
                return this;
            }

            public QuerySheetBuilder withSSPriority(String ssPriority) {
                this.ssPriority = ssPriority;
                return this;
            }


            public QuerySheet build() {
                return new QuerySheet(this);
            }

            public String getSheetName() {
                return sheetName;
            }

            public String[] getHeaders() {
                if (headers != null) {
                    String[] retStringArray = new String[headers.size()];
                    for (int i = 0; i < headers.size(); i++) {
                        retStringArray[i] = headers.get(i).header;
                    }
                    return retStringArray;
                }
                return new String[0];
            }

            public String[] getColumnsForHeader(String header) {
                if (header == null) return new String[0];
                if (headers != null) {
                    for (Header h : headers) {
                        if (h.isThisHeader(header)) return h.subs.toArray(new String[h.subs.size()]);
                    }
                    return new String[0];
                }
                return new String[0];
            }

            public String getSortBy() {
                return sortBy;
            }

            public QuerySheetBuilder setSortBy(String sortBy) {
                this.sortBy = sortBy;
                return this;
            }

            public String getFilterColumn() {
                return filterColumn;
            }

            public String getFilterValue() {
                return filterValue;
            }

            public String getGroupBy() {
                return groupBy;
            }

            public QuerySheetBuilder setGroupBy(String groupBy) {
                this.groupBy = groupBy;
                return this;
            }

            public QuerySheetBuilder setAllowFallback(boolean allowFallback) {
                this.allowFallback = allowFallback;
                return this;
            }
        }

        public static class Header {
            public final String header;
            public List<String> subs;

            public Header(String header) {
                this.header = header;
                subs = Lists.newArrayList();
            }

            public void addSub(String sub) {
                subs.add(sub);
            }

            public boolean isThisHeader(String test) {
                return header.equals(test);
            }

            public void overwriteSub(String subToOverwrite, String newValue) {
                for (int i = 0; i < subs.size(); i++) {
                    if (subs.get(i).equals(subToOverwrite)) {
                        subs.set(i, newValue);
                    }
                }
            }

            public Header copy(){
                Header retHeader = new Header(header);
                for (String s : subs) retHeader.addSub(s);
                return retHeader;
            }
        }
    }

    public static class QuerySerializer implements JsonSerializer<Query>, JsonDeserializer<Query> {

        @Override
        public Query deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext
                jsonDeserializationContext) throws JsonParseException {
            JsonObject o = jsonElement.getAsJsonObject();

            Query.QueryBuilder qb = new Query.QueryBuilder(o.get("queryName").getAsString());

            JsonElement templateJSON = o.get("template");
            if (templateJSON != null) {
                String template = templateJSON.getAsString().trim();
                if (!(template.equals("")) && !(template.equals("false")))
                    qb.setTemplate(template);
            }

            JsonElement outputTimestampJSON = o.get("outputTimestamp");
            if (outputTimestampJSON != null) {
                boolean outputTimestamp = outputTimestampJSON.getAsBoolean();
                qb = qb.setOutputTimestamp(outputTimestamp);
            }

            JsonArray sheetsArray = o.get("sheets").getAsJsonArray();
            for (JsonElement sheet : sheetsArray) {
                JsonObject sheetO = sheet.getAsJsonObject();

                JsonElement sheetNameJSON = sheetO.get("sheetName");
                String sheetName = (sheetNameJSON != null) ? sheetNameJSON.getAsString() : "Results";
                Query.QuerySheet.QuerySheetBuilder qsb = new Query.QuerySheet.QuerySheetBuilder(sheetName);

                JsonElement ssPriorityJSON = sheetO.get("prioritySS");
                String ssPriority = ssPriorityJSON != null ? ssPriorityJSON.getAsString() : null;
                qsb = qsb.withSSPriority(ssPriority);

                JsonElement allowFallbackJSON = sheetO.get("fallback");
                boolean allowFallback = allowFallbackJSON != null && allowFallbackJSON.getAsBoolean();
                qsb = qsb.setAllowFallback(allowFallback);


                JsonElement isHiddenJSON = sheetO.get("hidden");
                boolean isHidden = (isHiddenJSON != null) && isHiddenJSON.getAsBoolean();
                qsb = qsb.setIsHidden(isHidden);

                JsonArray headersJSON = sheetO.getAsJsonArray("headers"),
                        headerGroupsJSON = sheetO.getAsJsonArray("headerGroups");

                for (int i = 0; i < headersJSON.size(); i++) {
                    String header = headersJSON.get(i).getAsString();
                    JsonArray headerGroupJSON = headerGroupsJSON.get(i).getAsJsonArray();

                    for (int x = 0; x < headerGroupJSON.size(); x++)
                        qsb = qsb.withColumn(header,
                                headerGroupJSON.get(x).getAsString());
                }

                JsonElement filterColumnJSON = sheetO.get("filterColumn"),
                        filterValueJSON = sheetO.get("filterValue");

                String filterColumn = filterColumnJSON == null ? null : filterColumnJSON.getAsString(),
                        filterValue = filterValueJSON == null ? null : filterValueJSON.getAsString();

                qsb = qsb.setFilter(filterColumn, filterValue);

                JsonElement groupByJSON = sheetO.get("groupBy");
                if (groupByJSON != null) qsb = qsb.setGroupBy(groupByJSON.getAsString());

                qsb = qsb.setSortBy(sheetO.get("sortBy").getAsString());

                qb = qb.addSheet(qsb.build());
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
                        logger.error("Unable to construct calculated column: {}", e.getMessage(), e);
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

        @Override
        public JsonElement serialize(Query query, Type type, JsonSerializationContext jsonSerializationContext) {
            StringBuilder sb = new StringBuilder("{");

            sb.append("\"queryName\":\"").append(query.name).append("\",");

            if (query.hasTemplate)
                sb.append("\"template\":\"")
                        .append(query.templateName).append("\",");

            sb.append("\"outputTimestamp\":\"")
                    .append(query.outputTimestamp).append("\",")
                    .append("\"sheets\":")
                    .append(serializeSheets(query.sheets))
                    .append(",\"calculatedColumns\":")
                    .append(serializeCalculatedColumns(query.calculatedColumns))
                    .append(",\"mappedColumns\":")
                    .append(serializeMappedColumns(query.mappedColumns))
                    .append("}");

            JsonParser parser = new JsonParser();
            return parser.parse(sb.toString());
        }

        private String serializeSheets(List<Query.QuerySheet> sheets) {
            StringBuilder sb = new StringBuilder("[");

            for (Query.QuerySheet sheet : sheets) {
                Query.QuerySheet.QuerySheetBuilder qsb = sheet.toQuerySheetBuilder();


                sb.append("{\"sheetName\":").append("\"").append(qsb.getSheetName()).append("\"");

                if (sheet.ssPriority != null)
                    sb.append(",\"prioritySS\":\"").append(sheet.ssPriority).append("\",\"fallback\":\"")
                            .append(sheet.allowFallback).append("\"");

                sb.append(",\"headers\":[");
                for (String h : qsb.getHeaders()) sb.append("\"").append(h).append("\",");
                sb.deleteCharAt(sb.lastIndexOf(","));
                sb.append("],\"headerGroups\":[");

                for (String h : qsb.getHeaders()) {
                    sb.append("[");
                    for (String c : qsb.getColumnsForHeader(h)) {
                        sb.append("\"").append(c).append("\",");
                    }
                    sb.deleteCharAt(sb.lastIndexOf(","));
                    sb.append("],");
                }
                sb.deleteCharAt(sb.lastIndexOf(","));

                sb.append("],\"sortBy\":")
                        .append("\"").append(qsb.getSortBy()).append("\"")
                        .append(",\"filterColumn\":")
                        .append("\"").append(qsb.getFilterColumn()).append("\"")
                        .append(",\"filterValue\":")
                        .append("\"").append(qsb.getFilterValue()).append("\"");

                if ((qsb.getGroupBy() != null) && (!(qsb.getGroupBy().equals("NO GROUP BY"))))
                    sb.append(",\"groupBy\":\"").append(qsb.getGroupBy()).append("\"");

                if (qsb.isHidden) sb.append(",\"hidden\":\"true\"");
                else sb.append(",\"hidden\":\"false\"");

                sb.append("},");
            }

            if (sb.lastIndexOf(",") != -1) sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append("]");
            return sb.toString();
        }

        private String serializeCalculatedColumns(Map<String, CalculatedColumn> calculatedColumns) {
            StringBuilder sb = new StringBuilder("[");

            for (Map.Entry<String, CalculatedColumn> entry : calculatedColumns.entrySet()) {
                CalculatedColumn cc = entry.getValue();

                sb.append("{\"reference\":")
                        .append("\"").append(entry.getKey()).append("\"")
                        .append(",\"header\":")
                        .append("\"").append(cc.getHeader()).append("\"")
                        .append(",\"condition\":{\"firstHalf\":")
                        .append("\"").append(cc.getFirstHalf()).append("\"")
                        .append(",\"operator\":")
                        .append("\"").append(cc.getOperator()).append("\"")
                        .append(",\"secondHalf\":")
                        .append("\"").append(cc.getSecondHalf()).append("\"}},");
            }

            if (sb.lastIndexOf(",") != -1) sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append("]");
            return sb.toString();
        }

        private String serializeMappedColumns(Map<String, MappedColumn> mappedColumns) {
            StringBuilder sb = new StringBuilder("[");

            for (Map.Entry<String, MappedColumn> entry : mappedColumns.entrySet()) {
                MappedColumn mc = entry.getValue();

                sb.append("{\"reference\":")
                        .append("\"").append(entry.getKey()).append("\"")
                        .append(",\"original\":")
                        .append("\"").append(mc.getOriginal()).append("\"")
                        .append(",\"header\":")
                        .append("\"").append(mc.getHeader()).append("\"},");
            }

            if (sb.lastIndexOf(",") != -1) sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append("]");
            return sb.toString();
        }
    }
}