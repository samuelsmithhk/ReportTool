package cache;

import com.google.common.collect.Maps;
import deal.Deal;
import org.joda.time.DateTime;

import java.util.Map;

/**
 * Created by samuelsmith on 02/11/2014.
 */
public class Cache {

    private final Map<String, Deal> deals;

    public Cache(){
        deals = Maps.newHashMap();
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
}
