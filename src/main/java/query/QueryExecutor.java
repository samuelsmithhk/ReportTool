package query;

import cache.Cache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import deal.Deal;
import deal.DealProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by samuelsmith on 09/11/2014.
 */
public class QueryExecutor {

    private transient Logger logger = LoggerFactory.getLogger(QueryExecutor.class);

    public static QueryResult executeQuery(Cache cache, Query query) {
        QueryExecutor qe = new QueryExecutor(cache);

        QueryResult.QueryResultBuilder qrb = new QueryResult.QueryResultBuilder(query);

        for (Query.QuerySheet sheet : query.sheets) {
            Map<String, Deal> filteredDeals = qe.filterDeals(sheet.filterColumn, sheet.filterValue);
            List<QueryResultDeal> selectedColumns = qe.selectColumns(query, sheet.headers, filteredDeals);
            List<Group> groupedValues = qe.groupValues(sheet.groupBy, selectedColumns, sheet.sortBy);

            qe.overwriteHeaders(query, sheet.headers);

            qrb.addSheet(new QueryResult.QueryResultSheet(sheet.sheetName, groupedValues, sheet.headers));
        }
        


        QueryResult result = qrb.build();

        return result;
    }

    private final Cache cache;

    private QueryExecutor(Cache cache) {
        this.cache = cache;
    }

    public Map<String, Deal> filterDeals(String filterColumn, String filterValue) {
        logger.info("Filtering deals");
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

    public List<QueryResultDeal> selectColumns(Query query, List<Query.QuerySheet.Header> headers, Map<String, Deal> filteredDeals) {
        logger.info("Selecting columns");
        List<QueryResultDeal> retList = Lists.newArrayList();

        for (Map.Entry<String, Deal> toBeConverted : filteredDeals.entrySet()) {
            retList.add(new QueryResultDeal(cache, query, toBeConverted.getKey(),
                    toBeConverted.getValue().dealProperties, headers));
        }


        return retList;
    }

    public List<Group> groupValues(String groupBy, List<QueryResultDeal> selected, String sortBy) {
        logger.info("Grouping values");
        Map<String, Group> retMap = Maps.newTreeMap();

        if (groupBy == null) {
            Group g = new Group("Deals", sortBy);

            for (QueryResultDeal deal : selected) { g.addDeal(deal); }
            retMap.put("Deals", g);

            return Lists.newArrayList(retMap.values());
        }

        for (QueryResultDeal deal : selected) {
            if (deal.hasDealProperty(groupBy)) {
                String val = deal.getDPValue(groupBy);

                if (retMap.containsKey(val)){
                    Group g = retMap.get(val);
                    g.addDeal(deal);
                } else {
                    Group g = new Group(val, sortBy);
                    g.addDeal(deal);
                    retMap.put(val, g);
                }
            }
        }

        return Lists.newLinkedList(retMap.values());
    }

    private void  overwriteHeaders(Query query, List<Query.QuerySheet.Header> headers) {
        for (Query.QuerySheet.Header header : headers) {
            for (String sub : header.subs) {
                if (sub.startsWith("=")) {
                    String reference = sub.substring(1);
                    if (query.calculatedColumns.containsKey(reference)) {
                        header.overwriteSub(sub, query.calculatedColumns.get(reference).header);
                    }
                } else if (sub.startsWith("$")) {
                    String reference = sub.substring(1);
                    if (query.mappedColumns.containsKey(reference)) {
                        header.overwriteSub(sub, query.mappedColumns.get(reference).header);
                    }
                }
            }
        }
    }

}
