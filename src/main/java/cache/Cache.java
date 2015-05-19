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

    private static final Logger logger = LoggerFactory.getLogger(Cache.class);
    private final Set<String> columnIndex;
    private final Map<String, Deal> deals;
    private Map<String, DateTime> directoriesLastUpdated;
    private Map<String, LocalDate> sourceSystemsLastUpdated;
    private Cache() {
        logger.info("Creating empty cache");
        this.deals = Maps.newHashMap();
        this.columnIndex = Sets.newTreeSet();
        this.directoriesLastUpdated = Maps.newHashMap();
        this.sourceSystemsLastUpdated = Maps.newHashMap();
    }
    private Cache(Map<String, Deal> deals, Set<String> columnIndex, Map<String, DateTime> directoriesLastUpdated,
                  Map<String, LocalDate> sourceSystemsLastUpdated) {
        logger.info("Creating loaded cache with deals");
        this.deals = deals;
        this.columnIndex = columnIndex;
        this.directoriesLastUpdated = directoriesLastUpdated;
        this.sourceSystemsLastUpdated = sourceSystemsLastUpdated;
    }

    public static Cache createEmptyCache() {
        return new Cache();
    }

    public static Cache createLoadedCache(String cacheContents) {
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse(cacheContents).getAsJsonObject();

        JsonArray directoriesLastUpdatedJSON = o.getAsJsonArray("dLU");
        JsonArray sourceSystemsLastUpdatedJSON = o.getAsJsonArray("ssLU");
        JsonArray cacheColumnsJSON = o.getAsJsonArray("columnIndex");
        JsonObject cacheContentsJSON = o.getAsJsonObject("deals");

        Map<String, DateTime> directoriesLastUpdated = deserializeDirectoriesTimestamps(directoriesLastUpdatedJSON);
        Map<String, LocalDate> sourceSystemLastUpdated = deserializeSourceSystemTimestamps(sourceSystemsLastUpdatedJSON);
        Set<String> cacheColumns = deserializeCacheColumns(cacheColumnsJSON);
        Map<String, Deal> cacheDeals = deserializeCacheContents(cacheContentsJSON);

        return new Cache(cacheDeals, cacheColumns, directoriesLastUpdated, sourceSystemLastUpdated);
    }

    public static String serializeCache(Map<String, Deal> deals, Set<String> columnIndex,
                                        Map<String, DateTime> directoriesLastUpdated,
                                        Map<String, LocalDate> sourceSystemsLastUpdated) {

        Gson gson = new Gson();
        String dealsJSON = "{\"deals\":" + gson.toJson(deals) + ",";
        String colsJSON = "\"columnIndex\":" + gson.toJson(columnIndex) + ",";

        StringBuilder sb = new StringBuilder("\"dLU\":[");
        for (Map.Entry<String, DateTime> entry : directoriesLastUpdated.entrySet())
            sb.append("{\"directory\":\"").append(entry.getKey()).append("\",\"ts\":\"")
                    .append(entry.getValue()).append("\"},");

        sb.deleteCharAt(sb.lastIndexOf(",")).append("],\"ssLU\":[");

        for (Map.Entry<String, LocalDate> entry : sourceSystemsLastUpdated.entrySet())
            sb.append("{\"ss\":\"").append(entry.getKey()).append("\",\"ts\":\"")
                    .append(entry.getValue().toString("yyyyMMdd")).append("\"},");

        sb.deleteCharAt(sb.lastIndexOf(",")).append("]}");

        return dealsJSON + colsJSON + sb.toString();

    }

    public static Map<String, Deal> deserializeCacheContents(JsonObject o) {
        //{"Project PE - AA2":{"dealProperties":{"Deal Code Name":
        // {"values":{"2014-10-10T10:10:00.000+08:00":
        // {"innerValue":"Deal Code - Project PE - AA2","type":"STRING"}}}}},"Project PE - AA1":
        // {"dealProperties":{"Deal Code Name":{"values":{"2014-10-10T10:10:00.000+08:00":
        // {"innerValue":"Deal Code - Project PE - AA1","type":"STRING"}}}}}}

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

                    String timestampStr, sourceSystem;
                    Object innerValue;
                    DealProperty.Value.ValueType type;

                    for (Map.Entry<String, JsonElement> val : value.entrySet()) {
                        timestampStr = val.getKey();
                        JsonObject v = val.getValue().getAsJsonObject();

                        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
                        DateTime timestamp = formatter.parseDateTime(timestampStr);

                        type = parseType(v.get("type").getAsString());
                        innerValue = parseInnerValue(type, v.get("innerValue"));

                        sourceSystem = v.get("ss").getAsString();


                        DealProperty.Value parsedValue = new DealProperty.Value(innerValue, type, sourceSystem);
                        propertyMap.put(timestamp, parsedValue);
                    }

                    DealProperty.DealPropertyBuilder dpb = new DealProperty.DealPropertyBuilder();
                    DealProperty parsedDP = dpb.withValues(propertyMap).build();

                    dealMap.put(dpName, parsedDP);
                }

                parsedDeal = new Deal(dealMap);
            }

            if (parsedDeal != null) retMap.put(opportunity, parsedDeal);

        }

        return retMap;
    }

    public static Set<String> deserializeCacheColumns(JsonArray columns) {
        Set<String> retSet = Sets.newTreeSet();
        for (JsonElement col : columns) retSet.add(col.getAsString());
        return retSet;
    }

    public static Map<String, DateTime> deserializeDirectoriesTimestamps(JsonArray directories) {
        Map<String, DateTime> retMap = Maps.newHashMap();

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");

        for (JsonElement lu : directories) {
            JsonObject luO = lu.getAsJsonObject();
            String d = luO.get("directory").getAsString();
            String timestampStr = luO.get("ts").getAsString();

            DateTime timestamp = formatter.parseDateTime(timestampStr);

            retMap.put(d, timestamp);
        }

        return retMap;
    }

    private static Map<String, LocalDate> deserializeSourceSystemTimestamps(JsonArray sourceSystems) {
        Map<String, LocalDate> retMap = Maps.newHashMap();

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");

        for (JsonElement ss : sourceSystems) {
            JsonObject ssO = ss.getAsJsonObject();
            String ssString = ssO.get("ss").getAsString();
            String timestampStr = ssO.get("ts").getAsString();
            LocalDate timestamp = formatter.parseLocalDate(timestampStr);

            retMap.put(ssString, timestamp);
        }

        return retMap;
    }

    public static Object parseInnerValue(DealProperty.Value.ValueType type, JsonElement innerValue) {
        switch (type) {
            case BL:
                return "";
            case BO:
                return innerValue.getAsBoolean();
            case NU:
                return innerValue.getAsDouble();
            default:
                return innerValue.getAsString();
        }
    }

    public static DealProperty.Value.ValueType parseType(String typeStr) {
        if (typeStr.equals("BL")) return DealProperty.Value.ValueType.BL;
        if (typeStr.equals("BO")) return DealProperty.Value.ValueType.BO;
        if (typeStr.equals("NU")) return DealProperty.Value.ValueType.NU;
        return DealProperty.Value.ValueType.ST;
    }

    public void processDealUpdate(String sourceSystem, String directory, DateTime timestamp,
                                  Map<String, Deal> newDeals) {
        logger.info("Processing deal update with newDeals");

        for (Map.Entry<String, Deal> entry : newDeals.entrySet()) {
            Deal deal = entry.getValue();

            //update column index
            columnIndex.addAll(deal.dealProperties.keySet());

            if (deals.containsKey(entry.getKey())) {
                //update deal
                logger.info("Updating deal {}", entry.getKey());
                deals.get(entry.getKey()).updateDeal(timestamp, deal);
            } else {
                //new deal
                deals.put(entry.getKey(), deal);
            }
        }

        if (directoriesLastUpdated.get(directory) == null) directoriesLastUpdated.put(directory, timestamp);
        else if (directoriesLastUpdated.get(directory).isBefore(timestamp))
            directoriesLastUpdated.put(directory, timestamp);

        LocalDate tsLD = timestamp.toLocalDate();

        if (sourceSystemsLastUpdated.get(sourceSystem) == null)
            sourceSystemsLastUpdated.put(sourceSystem, tsLD);
        else if (sourceSystemsLastUpdated.get(sourceSystem).isBefore(timestamp.toLocalDate()))
            sourceSystemsLastUpdated.put(sourceSystem, tsLD);
    }

    public DateTime getDirectoriesLastUpdated(String directory) {
        return directoriesLastUpdated.get(directory);
    }

    public Map<String, DateTime> getDirectoriesLastUpdated() {
        return directoriesLastUpdated;
    }

    public LocalDate getSnapshotDate() {
        DateTime latest = null;
        for (DateTime dt : directoriesLastUpdated.values()) if (latest == null || dt.isAfter(latest)) latest = dt;
        return latest == null ? null : latest.toLocalDate();
    }

    public Map<String, Deal> getDeals() {
        return deals;
    }

    public Set<String> getCols() {
        return columnIndex;
    }

    public Set<String> getSourceSystems() {
        return sourceSystemsLastUpdated.keySet();
    }

    public Deal getDeal(String dealName) throws CacheException {
        if (deals.containsKey(dealName)) return deals.get(dealName);
        else throw new CacheException("Deal does not exist in cache: " + dealName);
    }

    public Map<String, LocalDate> getSourceSystemsLastUpdated() {
        return sourceSystemsLastUpdated;
    }

    public LocalDate getSourceSystemLastUpdated(String sourceSystem) {
        return sourceSystemsLastUpdated.get(sourceSystem);
    }

    public void purgeOldData() {
        for (Map.Entry<String, Deal> entry : deals.entrySet())
            if (!entry.getValue().purgeOldData()) deals.remove(entry.getKey());

        deals.size();
    }

    public class CacheException extends Exception {
        public CacheException(String e) {
            super(e);
        }
    }
}