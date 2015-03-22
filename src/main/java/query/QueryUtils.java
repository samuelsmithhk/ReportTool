package query;

import deal.DealProperty;

public class QueryUtils {
    public static String parseValue(DealProperty.Value value) {
        if (value == null || value.innerValue == null) return "";
        if (value.type == DealProperty.Value.ValueType.NUMERIC) {
            Double val = (Double) value.innerValue;
            if (val % 1 == 0 ) return String.valueOf(val.intValue());
        }
        return String.valueOf(value.innerValue);
    }
}
