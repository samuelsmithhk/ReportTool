package query;

import cache.Cache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import deal.Deal;
import deal.DealProperty;

import java.util.List;
import java.util.Map;

/**
 * Created by samuelsmith on 09/11/2014.
 */
public class QueryExecutor {

    public static String executeQuery(Cache cache, Query query) {
        QueryExecutor qe = new QueryExecutor(cache);

        Map<String, Deal> filteredDeals = qe.filterDeals(query.filterColumn, query.filterValue);
        List<QueryResultDeal> selectedColumns = qe.selectColumns(query.columns, filteredDeals);
        List<Group> groupedValues = qe.groupValues(query.groupBy, selectedColumns);
        QueryResult result = new QueryResult(groupedValues);

        Gson gson = new Gson();
        return gson.toJson(result);
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
                if (QueryUtils.parseValue(latestValue).equals(filterValue))
                    retMap.put(entry.getKey(), entry.getValue());
            }
        }

        return retMap;
    }

    public List<QueryResultDeal> selectColumns(List<String> columns, Map<String, Deal> filteredDeals) {
        List<QueryResultDeal> retList = Lists.newArrayList();

        for (Map.Entry<String, Deal> toBeConverted : filteredDeals.entrySet()) {
            retList.add(new QueryResultDeal(toBeConverted.getKey(),
                    toBeConverted.getValue().dealProperties, columns));
        }


        return retList;
    }

    public List<Group> groupValues(String groupBy, List<QueryResultDeal> selected) {
        Map<String, Group> retList = Maps.newHashMap();

        for (QueryResultDeal deal : selected) {
            if (deal.dealProperties.containsKey(groupBy)) {
                String val = deal.dealProperties.get(groupBy);

                if (retList.containsKey(val)){
                    Group g = retList.get(val);
                    g.addDeal(deal);
                } else {
                    Group g = new Group(val);
                    g.addDeal(deal);
                    retList.put(val, g);
                }
            }
        }

        return Lists.newArrayList(retList.values());
    }

}
