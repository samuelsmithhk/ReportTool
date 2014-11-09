package query;

import cache.Cache;
import com.google.common.collect.Maps;
import deal.Deal;
import deal.DealProperty;

import java.util.Map;

/**
 * Created by samuelsmith on 09/11/2014.
 */
public class QueryExecutor {

    public static String executeQuery(Cache cache, Query query) {
        QueryExecutor qe = new QueryExecutor(cache);

        Map<String, Deal> filteredDeals = qe.filterDeals(query.filterColumn, query.filterValue);

        //TODO: implement execution of group by and select columns
        return null;
    }

    private final Cache cache;

    private QueryExecutor(Cache cache) {
        this.cache = cache;
    }

    public Map<String, Deal> filterDeals(String filterColumn, String filterValue) {
        Map<String, Deal> dealMap = cache.getDeals();
        Map<String, Deal> retMap = Maps.newHashMap();

        for (Map.Entry<String, Deal> entry : dealMap.entrySet()) {
            Deal deal = entry.getValue();

            if (deal.dealProperties.containsKey(filterColumn)) {
                DealProperty dp = deal.dealProperties.get(filterColumn);
                DealProperty.Value latestValue = dp.getLatestValue();
                if (parseValue(latestValue).equals(filterValue))
                    retMap.put(entry.getKey(), entry.getValue());
            }
        }

        return retMap;
    }

    public String parseValue(DealProperty.Value value) {
        switch (value.type) {
            case BLANK:
                return "";
        }

        return String.valueOf(value.innerValue);
    }

}
