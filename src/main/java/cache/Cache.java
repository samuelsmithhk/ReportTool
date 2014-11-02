package cache;

import com.google.common.collect.Maps;
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

    private static String serializeCache(Map<String, Deal> file) {
        //TODO: parse map to json
        return null;
    }

    private static Map<String, Deal> deserializeCache(String json) {
        //TODO: Parse json into map
        return null;
    }
}
