package mapping;

import com.google.common.collect.Maps;
import managers.MappingManager;
import org.joda.time.LocalDate;

import java.util.Map;

public class ICDateMapping {

    private final Map<String, String> dateMap;

    public boolean hasNewMapping;
    public ICDateMapping newMapping;

    private ICDateMapping(ICDateMappingBuilder icdmb){ this.dateMap = icdmb.dateMap; }

    public String getMapping(String dealCode, LocalDate current) throws Exception {
        //04/30/2014
        if (dateMap.containsKey(dealCode)) return dateMap.get(dealCode);
        else {
            //save datetime to mapping file
            String currentStr = current.toString("MM/dd/yyyy");
            newMapping = MappingManager.getMappingManager().addNewICDate(dealCode, currentStr);
            hasNewMapping = true;
            return currentStr;
        }
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
