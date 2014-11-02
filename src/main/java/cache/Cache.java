package cache;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import deal.Deal;
import org.joda.time.DateTime;

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

        //TODO: Parse json into map
        return null;
    }
}
