package query;

import deal.DealProperty;

/**
 * Created by samuelsmith on 09/11/2014.
 */
public class QueryUtils {
    public static String parseValue(DealProperty.Value value) {
        if (value.innerValue == null) return "";
        return String.valueOf(value.innerValue);
    }
}
