package mapping;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by samuelsmith on 08/02/2015.
 */
public class ICDateMapping {

    private final Map<String, String> dateMap;

    private ICDateMapping(ICDateMappingBuilder icdmb){ this.dateMap = icdmb.dateMap; }

    public String getMapping(String dealCode) {
        if (dateMap.containsKey(dealCode)) return dateMap.get(dealCode);
        else return "";
    }

    public static class ICDateMappingBuilder {
        private Map<String, String> dateMap;

        public ICDateMappingBuilder() {
            this.dateMap = Maps.newHashMap();
        }

        public ICDateMappingBuilder withDate(String dealCode, String date) {
            this.dateMap.put(dealCode, date);
            return this;
        }

        public ICDateMapping build() {
            return new ICDateMapping(this);
        }
    }
}
