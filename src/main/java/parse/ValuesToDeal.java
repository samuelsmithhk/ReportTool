package parse;

import com.google.common.collect.Maps;
import deal.Deal;
import deal.DealProperty;
import org.joda.time.DateTime;
import sheetparser.Value;

import java.util.List;
import java.util.Map;

public class ValuesToDeal {

    private String companyName;

    public Map<String, Deal> convert(DateTime timestamp, String sourceSystem, List<Map<String, Value>> in) throws Exception {
        Map<String, Deal> retMap = Maps.newHashMap();

        for (Map<String, Value> valueMap : in) {
            //create deal from deal properties
            //use company name as key in return map

            Map<String, DealProperty> dealProperties = convertToDealProperties(timestamp, valueMap);
            DealProperty.DealPropertyBuilder dpb = new DealProperty.DealPropertyBuilder();
            dpb.withValue(timestamp, new DealProperty.Value(sourceSystem, DealProperty.Value.ValueType.STRING));
            dealProperties.put("Source System", dpb.build());

            if (companyName == null) throw new Exception("Company Name missing from dealset");


            Deal d = new Deal(dealProperties);
            retMap.put(companyName, d);
        }

        return retMap;
    }

    private Map<String, DealProperty> convertToDealProperties(DateTime timestamp, Map<String, Value> valueMap) {
        Map<String, DealProperty> retMap = Maps.newHashMap();

        for (Map.Entry<String, Value> entry : valueMap.entrySet()) {

            DealProperty.DealPropertyBuilder dpb = new DealProperty.DealPropertyBuilder();

            Value v = entry.getValue();

            switch (v.type) {
                case BOOLEAN :
                    dpb.withValue(timestamp, new DealProperty.Value(v.t, DealProperty.Value.ValueType.BOOLEAN));
                    break;
                case BLANK:
                    dpb.withValue(timestamp, new DealProperty.Value(v.t, DealProperty.Value.ValueType.BLANK));
                    break;
                case NUMERIC:
                    dpb.withValue(timestamp, new DealProperty.Value(v.t, DealProperty.Value.ValueType.NUMERIC));
                    break;
                case STRING:
                    dpb.withValue(timestamp, new DealProperty.Value(v.t, DealProperty.Value.ValueType.STRING));
                    break;
            }

            if (entry.getKey().equals("Company Name")) companyName = (String) v.t;
            retMap.put(entry.getKey(), dpb.build());
        }
        return retMap;
    }

}
