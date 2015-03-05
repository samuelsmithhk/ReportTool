package query;

import cache.Cache;
import com.google.common.collect.Maps;
import deal.DealProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class QueryResultDeal {

    Logger logger = LoggerFactory.getLogger(QueryResultDeal.class);

    public final String dealName;
    public final Map<Header, String> dealProperties;

    private final Query query;
    private final Cache cache;

    public QueryResultDeal(Cache cache, Query query, String dealName, Map<String, DealProperty> dpToConvert,
                           List<Query.QuerySheet.Header> selectedColumns) {
        this.dealName = dealName;
        this.query = query;
        this.cache = cache;

        this.dealProperties = convertDealProperties(dpToConvert, selectedColumns);
    }

    public Map<Header, String> convertDealProperties(Map<String, DealProperty> toConvert,
                                                     List<Query.QuerySheet.Header> cols) {

        Map<Header, String> retMap = Maps.newLinkedHashMap();

        for (Query.QuerySheet.Header col : cols) {
            for (String sub : col.subs) {
                if (toConvert.containsKey(sub))
                    retMap.put(new Header(col.header, sub), QueryUtils.parseValue(toConvert.get(sub).getLatestValue()));
                else if ((sub.startsWith("=")) || (sub.startsWith("$"))) {
                    try {
                        SpecialColumn sc = query.getSpecialColumn(sub);

                        retMap.put(new Header(col.header, sc.getHeader()),
                                QueryUtils.parseValue(sc.evaluate(query, cache, dealName)));
                    } catch (SpecialColumn.SpecialColumnException e) {
                        retMap.put(new Header(col.header, sub), "");
                    } catch (Cache.CacheException e) {
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
