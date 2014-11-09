package query;

import com.google.common.collect.Maps;
import deal.DealProperty;

import java.util.List;
import java.util.Map;

/**
 * Created by samuelsmith on 09/11/2014.
 */
public class QueryResultDeal {

    public final String dealName;
    public final Map<String, String> dealProperties;

    public QueryResultDeal(String dealName, Map<String, DealProperty> dpToConvert, List<String> selectedColumns) {
        this.dealName = dealName;
        this.dealProperties = convertDealProperties(dpToConvert, selectedColumns);
    }

    public Map<String, String> convertDealProperties(Map<String, DealProperty> toConvert, List<String> cols) {
        Map<String, String> retMap = Maps.newHashMap();

        for (String col : cols) {
            if (toConvert.containsKey(col))
                retMap.put(col, QueryUtils.parseValue(toConvert.get(col).getLatestValue()));
            else
                retMap.put(col, "");
        }

        return retMap;
    }


}
