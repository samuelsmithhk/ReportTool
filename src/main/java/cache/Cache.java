package cache;

import com.google.common.collect.Maps;
import com.google.gson.*;
import deal.Deal;
import deal.DealProperty;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Map;

/**
 * Created by samuelsmith on 02/11/2014.
 */
public class Cache {

    public static Cache createEmptyCache() {
        return new Cache();
    }

    public static Cache createLoadedCache(String cacheFile) {
        return new Cache(deserializeCache(cacheFile));
    }


    private final Map<String, Deal> deals;

    private Cache(){
        deals = Maps.newHashMap();
    }

    private Cache(Map<String, Deal> deals) {
        this.deals = deals;
    }

    public void processDealUpdate(DateTime timestamp, Map<String, Deal> newDeals) {

        for (Map.Entry<String, Deal> entry : newDeals.entrySet()) {
            if (deals.containsKey(entry.getKey())) {
                //update deal
                deals.get(entry.getKey()).updateDeal(timestamp, entry.getValue());
            } else {
                //new deal
                deals.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public static String serializeCache(Map<String, Deal> file) {
        Gson gson = new Gson();
        return gson.toJson(file);

    }

    public static Map<String, Deal> deserializeCache(String json) {
        //{"Project PE - AA2":{"dealProperties":{"Deal Code Name":
        // {"values":{"2014-10-10T10:10:00.000+08:00":
        // {"innerValue":"Deal Code - Project PE - AA2","type":"STRING"}}}}},"Project PE - AA1":
        // {"dealProperties":{"Deal Code Name":{"values":{"2014-10-10T10:10:00.000+08:00":
        // {"innerValue":"Deal Code - Project PE - AA1","type":"STRING"}}}}}}

        JsonParser parser = new JsonParser();
        JsonObject o = (JsonObject) parser.parse(json);

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

                    for (Map.Entry<String, JsonElement> val : ((JsonObject) value).entrySet()) {
                        timestampStr = val.getKey();
                        JsonObject v = val.getValue().getAsJsonObject();

                        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'hh:mm:ss.SSSZZ");
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

    private static Object parseInnerValue(DealProperty.Value.ValueType type, JsonElement innerValue) {
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

    private static DealProperty.Value.ValueType parseType(String typeStr) {
        if (typeStr.equals("BLANK")) return DealProperty.Value.ValueType.BLANK;
        if (typeStr.equals("BOOLEAN")) return DealProperty.Value.ValueType.BOOLEAN;
        if (typeStr.equals("NUMERIC")) return DealProperty.Value.ValueType.NUMERIC;
        return DealProperty.Value.ValueType.STRING;
    }
}
