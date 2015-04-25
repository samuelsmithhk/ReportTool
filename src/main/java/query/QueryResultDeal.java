package query;

import cache.Cache;
import com.google.common.collect.Maps;
import deal.DealProperty;
import org.joda.time.LocalDate;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class QueryResultDeal {

    public final String dealName;
    public final Map<Header, String> dealProperties;

    private final Query query;

    public QueryResultDeal(Query query, String dealName, Map<String, DealProperty> dpToConvert,
                           List<Query.QuerySheet.Header> selectedColumns, LocalDate snapshotDate) throws Exception {
        this.dealName = dealName;
        this.query = query;
        this.dealProperties = convertDealProperties(dpToConvert, selectedColumns, snapshotDate);
    }

    public Map<Header, String> convertDealProperties(Map<String, DealProperty> toConvert,
                                                     List<Query.QuerySheet.Header> cols,
                                                     LocalDate snapshotDate) throws Exception {

        Map<Header, String> retMap = Maps.newLinkedHashMap();

        for (Query.QuerySheet.Header col : cols) {
            for (String sub : col.subs) {
                if (toConvert.containsKey(sub))
                    retMap.put(new Header(col.header, sub), QueryUtils.parseValue(toConvert.get(sub)
                            .getSnapshotValue(snapshotDate)));
                else if ((sub.startsWith("=")) || (sub.startsWith("$"))) {
                    try {
                        SpecialColumn sc = query.getSpecialColumn(sub);

                        retMap.put(new Header(col.header, sc.getHeader()),
                                QueryUtils.parseValue(sc.evaluate(query, dealName)));
                    } catch (SpecialColumn.SpecialColumnException | Cache.CacheException e) {
                        retMap.put(new Header(col.header, sub), "");
                    }
                }
                else
                    retMap.put(new Header(col.header, sub), "");
            }
        }

        return retMap;
    }

    public boolean hasDealProperty(String subHeader) {
        Set<Header> keySet = dealProperties.keySet();

        for (QueryResultDeal.Header key : keySet) {
            String sub = key.sub;

            if (sub.equals(subHeader)) {
                return true;
            }
        }

        return false;
    }

    public String getDPValue(String subHeader) {
        Set<Map.Entry<QueryResultDeal.Header, String>> entrySet = dealProperties.entrySet();

        for (Map.Entry<QueryResultDeal.Header, String> entry : entrySet) {
            String sub = entry.getKey().sub;

            if (sub.equals(subHeader)) return entry.getValue();
        }

        return null;
    }

    @Override
    public String toString() {
        return dealName;
    }

    public class Header {
        public final String header, sub;

        public Header(String header, String sub) {
            this.header = header;
            this.sub = sub;
        }
    }
}
