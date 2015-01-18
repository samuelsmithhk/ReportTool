package query;

import com.google.common.collect.Maps;
import deal.DealProperty;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by samuelsmith on 09/11/2014.
 */
public class QueryResultDeal {

    public final String dealName;
    public final Map<Header, String> dealProperties;

    public QueryResultDeal(String dealName, Map<String, DealProperty> dpToConvert, List<Query.QuerySheet.Header> selectedColumns) {
        this.dealName = dealName;
        this.dealProperties = convertDealProperties(dpToConvert, selectedColumns);
    }

    public Map<Header, String> convertDealProperties(Map<String, DealProperty> toConvert, List<Query.QuerySheet.Header> cols) {
        Map<Header, String> retMap = Maps.newHashMap();

        for (Query.QuerySheet.Header col : cols) {
            for (String sub : col.subs) {
                if (toConvert.containsKey(sub))
                    retMap.put(new Header(col.header, sub), QueryUtils.parseValue(toConvert.get(sub).getLatestValue()));
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

            if (sub.equals(subHeader)) {
                return entry.getValue();
            }
        }

        return null;
    }

    public class Header {
        public final String header, sub;

        public Header(String header, String sub) {
            this.header = header;
            this.sub = sub;
        }
    }
}
