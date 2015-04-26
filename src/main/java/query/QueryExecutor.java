package query;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import deal.Deal;
import deal.DealProperty;
import managers.CacheManager;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


public class QueryExecutor {

    private static transient Logger logger = LoggerFactory.getLogger(QueryExecutor.class);

    private QueryExecutor() {
    }

    public static QueryResult executeQuery(Query query) throws Exception {
        QueryExecutor qe = new QueryExecutor();

        QueryResult.QueryResultBuilder qrb = new QueryResult.QueryResultBuilder(query);
        CacheManager cm = CacheManager.getCacheManager();
        LocalDate snapshotDate = cm.getSnapshotDate();

        for (Query.QuerySheet sheet : query.sheets) {
            Map<String, Deal> filteredDeals;
            try {
                filteredDeals = qe.filterDeals(sheet.filterColumn, sheet.filterValue, snapshotDate);
                List<QueryResultDeal> selectedColumns = qe.selectColumns(query, sheet.headers, filteredDeals,
                        snapshotDate);
                List<Group> groupedValues;
                try {
                    groupedValues = qe.groupValues(query, sheet.groupBy, selectedColumns, sheet.sortBy);
                } catch (SpecialColumn.SpecialColumnException e) {
                    logger.warn("Unable to execute group by for query, outputting with no groupings, reason: {}"
                            , e.getMessage(), e);
                    groupedValues = qe.safeGroupValues(selectedColumns, sheet.sortBy);
                }
                List<Group> sortedValues = qe.sortValues(groupedValues);

                qe.overwriteHeaders(query, sheet.headers);

                qrb.addSheet(new QueryResult.QueryResultSheet(sheet.sheetName, sortedValues, sheet.headers,
                        sheet.isHidden));
            } catch (Exception e) {
                logger.error("Error executing query, skipping sheet: {}", e.getMessage(), e);
            }
        }

        logger.info("Query execution finished");

        return qrb.build();
    }

    public Map<String, Deal> filterDeals(String filterColumn, String filterValue, LocalDate snapshotDate)
            throws Exception {
        logger.info("Filtering deals");

        CacheManager cm = CacheManager.getCacheManager();

        Map<String, Deal> dealMap = cm.getDeals();
        Map<String, Deal> retMap = Maps.newHashMap();

        if ((filterColumn == null) || (filterColumn.trim().equals("")) || (filterColumn.trim().equals("null")) ||
                (filterColumn.trim().equals("N/A")))
            return dealMap;

        for (Map.Entry<String, Deal> entry : dealMap.entrySet()) {
            Deal deal = entry.getValue();

            if (deal.dealProperties.containsKey(filterColumn)) {
                DealProperty dp = deal.dealProperties.get(filterColumn);
                DealProperty.Value latestValue = dp.getSnapshotValue(snapshotDate);
                if ((latestValue != null) && (QueryUtils.parseValue(latestValue).equals(filterValue)))
                    retMap.put(entry.getKey(), entry.getValue());
            }
        }

        return retMap;
    }

    public List<QueryResultDeal> selectColumns(Query query, List<Query.QuerySheet.Header> headers,
                                               Map<String, Deal> filteredDeals,
                                               LocalDate snapshotDate) throws Exception {

        logger.info("Selecting columns");
        List<QueryResultDeal> retList = Lists.newArrayList();

        for (Map.Entry<String, Deal> toBeConverted : filteredDeals.entrySet()) {
            retList.add(new QueryResultDeal(query, toBeConverted.getKey(),
                    toBeConverted.getValue().dealProperties, headers, snapshotDate));
        }


        return retList;
    }

    public List<Group> groupValues(Query query, String groupBy, List<QueryResultDeal> selected, String sortBy)
            throws SpecialColumn.SpecialColumnException {
        logger.info("Grouping values");
        Map<String, Group> retMap = Maps.newTreeMap();

        if ((groupBy == null) || (groupBy.trim().equals("")) || (groupBy.trim().equals("null")) ||
                (groupBy.trim().equals("N/A"))) {
            return safeGroupValues(selected, sortBy);
        }

        if (groupBy.startsWith("$")) {
            MappedColumn mc = (MappedColumn) query.getSpecialColumn(groupBy);
            groupBy = mc.getHeader();
        } else if (groupBy.startsWith("=")) {
            throw new SpecialColumn.SpecialColumnException("Unable to group by calculated columns");
        }

        for (QueryResultDeal deal : selected) {
            if (deal.hasDealProperty(groupBy)) {
                String val = deal.getDPValue(groupBy);

                if (retMap.containsKey(val)) {
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

    public List<Group> safeGroupValues(List<QueryResultDeal> selected, String sortBy) {
        Map<String, Group> retMap = Maps.newTreeMap();

        Group g = new Group("no-group", sortBy);

        for (QueryResultDeal deal : selected) {
            g.addDeal(deal);
        }
        retMap.put("Deals", g);

        return Lists.newArrayList(retMap.values());
    }

    public List<Group> sortValues(List<Group> toSort) {
        for (Group g : toSort) g.sortGroup();
        return toSort;
    }

    private void overwriteHeaders(Query query, List<Query.QuerySheet.Header> headers) {
        for (Query.QuerySheet.Header header : headers) {
            for (String sub : header.subs) {
                if ((sub.startsWith("=")) || (sub.startsWith("$"))) {
                    try {
                        SpecialColumn sc = query.getSpecialColumn(sub);
                        header.overwriteSub(sub, sc.getHeader());
                    } catch (SpecialColumn.SpecialColumnException e) {
                        logger.warn("Special column {} does not exist in query, skipping header overwrite", sub);
                    }
                }
            }
        }
    }

}
