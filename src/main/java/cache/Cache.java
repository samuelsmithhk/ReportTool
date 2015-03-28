package cache;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.*;
import deal.Deal;
import deal.DealProperty;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class Cache {

    private final Logger logger = LoggerFactory.getLogger(Cache.class);

    public static Cache createEmptyCache() {
        return new Cache();
    }

    public static Cache createLoadedCache(String cacheContents) {
        Map<String, DateTime> lastUpdated = deserializeCacheTimestamps(cacheContents);
        return new Cache(deserializeCacheContents(cacheContents), deserializeCacheColumns(cacheContents), lastUpdated);
    }

    private final Set<String> columnIndex;
    private final Map<String, Deal> deals;
    private Map<String, DateTime> lastUpdated;

    private Cache(){
        logger.info("Creating empty cache");
        this.deals = Maps.newHashMap();
        this.columnIndex = Sets.newTreeSet();
        this.lastUpdated = Maps.newHashMap();
    }

    private Cache(Map<String, Deal> deals, Set<String> columnIndex, Map<String, DateTime> lastUpdated) {
        logger.info("Creating loaded cache with deals");
        this.deals = deals;
        this.columnIndex = columnIndex;
        this.lastUpdated = lastUpdated;
    }

    public void processDealUpdate(String directory, DateTime timestamp, Map<String, Deal> newDeals) {
        logger.info("Processing deal update with newDeals");

        for (Map.Entry<String, Deal> entry : newDeals.entrySet()) {
            Deal deal = entry.getValue();

            //update column index
            columnIndex.addAll(deal.dealProperties.keySet());

            if (deals.containsKey(entry.getKey())) {
                //update deal
                logger.info("Updating deal " + entry.getKey());
                deals.get(entry.getKey()).updateDeal(timestamp, deal);
            } else {
                //new deal
                deals.put(entry.getKey(), deal);
            }
        }

        if (lastUpdated.get(directory) == null) lastUpdated.put(directory, timestamp);
        else if (lastUpdated.get(directory).isBefore(timestamp)) lastUpdated.put(directory, timestamp);
    }

    public DateTime getLastUpdated(String directory) {
        return lastUpdated.get(directory);
    }

    public Map<String,DateTime> getLastUpdated() {
        return lastUpdated;
    }

    public LocalDate getSnapshotDate() {
        DateTime latest = null;
        for (DateTime dt : lastUpdated.values()) if (latest == null || dt.isAfter(latest)) latest = dt;
        return latest == null ? null : latest.toLocalDate();
    }


    public Map<String, Deal> getDeals() {
        return deals;
    }

    public Set<String> getCols() {
        return columnIndex;
    }

    public Deal getDeal(String dealName) throws CacheException {
        if (deals.containsKey(dealName)) return deals.get(dealName);
        else throw new CacheException("Deal does not exist in cache: " + dealName);
    }

    public static String serializeCache(Map<String, Deal> deals, Set<String> columnIndex,
                                        Map<String, DateTime> lastUpdated) {

        Gson gson = new Gson();
        String dealsJSON = "{\"deals\":" + gson.toJson(deals) + ",";
        String colsJSON = "\"columnIndex\":" + gson.toJson(columnIndex) + ",";

        StringBuilder sb = new StringBuilder("\"lastUpdated\":[");
        for (Map.Entry<String, DateTime> entry : lastUpdated.entrySet())
            sb.append("{\"directory\":\"").append(entry.getKey()).append("\",\"timestamp\":\"")
                    .append(entry.getValue()).append("\"},");

        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("]}");

        return dealsJSON + colsJSON + sb.toString();

    }

    public static Map<String, Deal> deserializeCacheContents(String json) {
        //{"Project PE - AA2":{"dealProperties":{"Deal Code Name":
        // {"values":{"2014-10-10T10:10:00.000+08:00":
        // {"innerValue":"Deal Code - Project PE - AA2","type":"STRING"}}}}},"Project PE - AA1":
        // {"dealProperties":{"Deal Code Name":{"values":{"2014-10-10T10:10:00.000+08:00":
        // {"innerValue":"Deal Code - Project PE - AA1","type":"STRING"}}}}}}

        JsonParser parser = new JsonParser();
        JsonObject jo = (JsonObject) parser.parse(json);
        JsonObject o = jo.get("deals").getAsJsonObject();

        Map<String, Deal> retMap = Maps.newHashMap();

        for (Map.Entry<String, JsonElement> entry : o.entrySet()) {
            String opportunity = entry.getKey();
            JsonObject dealPropertiesJSON = entry.getValue().getAsJsonObject().get("dealProperties").getAsJsonObject();

            Deal parsedDeal = null;
            Map<String, DealProperty> dealMap = Maps.newHashMap();

            for (Map.Entry<String, JsonElement> packEntry : dealPropertiesJSON.entrySet()) {
                String dpName = packEntry.getKey();

                JsonObject dpValues = (JsonObject) packEntry.getValue();

                Map<DateTime, DealProperty.Value> propertyMap = Maps.newHashMap();

                for (Map.Entry<String, JsonElement> dealProperty : dpValues.entrySet()) {

                    JsonObject value = dealProperty.getValue().getAsJsonObject();

                    String timestampStr;
                    Object innerValue;
                    DealProperty.Value.ValueType type;

                    for (Map.Entry<String, JsonElement> val : value.entrySet()) {
                        timestampStr = val.getKey();
                        JsonObject v = val.getValue().getAsJsonObject();

                        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
                        DateTime timestamp = formatter.parseDateTime(timestampStr);

                        type = parseType(v.get("type").getAsString());
                        innerValue = parseInnerValue(type, v.get("innerValue"));

                        DealProperty.Value parsedValue = new DealProperty.Value(innerValue, type);
                        propertyMap.put(timestamp, parsedValue);
                    }

                    DealProperty.DealPropertyBuilder dpb = new DealProperty.DealPropertyBuilder();
                    DealProperty parsedDP = dpb.withValues(propertyMap).build();

                    dealMap.put(dpName, parsedDP);
                }

                parsedDeal = new Deal(dealMap);
            }

            if (parsedDeal  != null) retMap.put(opportunity, parsedDeal);

        }

        return retMap;
    }

    public static Set<String> deserializeCacheColumns(String json) {
        Set<String> retSet = Sets.newTreeSet();
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse(json).getAsJsonObject();

        JsonArray colIndexJsonArray = o.get("columnIndex").getAsJsonArray();

        for (JsonElement col : colIndexJsonArray) {
            retSet.add(col.getAsString());
        }

        return retSet;
    }

    public static Map<String, DateTime> deserializeCacheTimestamps(String json) {
        Map<String, DateTime> retMap = Maps.newHashMap();
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse(json).getAsJsonObject();

        JsonArray luJsonArray = o.get("lastUpdated").getAsJsonArray();

        for (JsonElement lu : luJsonArray) {
            JsonObject luO = lu.getAsJsonObject();
            String d = luO.get("directory").getAsString();
            String timestampStr = luO.get("timestamp").getAsString();

            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
            DateTime timestamp = formatter.parseDateTime(timestampStr);

            retMap.put(d, timestamp);
        }

        return retMap;
    }

    public static Object parseInnerValue(DealProperty.Value.ValueType type, JsonElement innerValue) {
        switch (type) {
            case BLANK:
                return "";
            case BOOLEAN:
                return innerValue.getAsBoolean();
            case NUMERIC:
                return innerValue.getAsDouble();
            default :
                return innerValue.getAsString();
        }
    }

    public static DealProperty.Value.ValueType parseType(String typeStr) {
        if (typeStr.equals("BLANK")) return DealProperty.Value.ValueType.BLANK;
        if (typeStr.equals("BOOLEAN")) return DealProperty.Value.ValueType.BOOLEAN;
        if (typeStr.equals("NUMERIC")) return DealProperty.Value.ValueType.NUMERIC;
        return DealProperty.Value.ValueType.STRING;
    }

    public class CacheException extends Exception {
        public CacheException(String e) { super(e);}
    }
}
