package mapping;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by samuelsmith on 08/02/2015.
 */
public class CRGMapping {

    private final Map<String, CagHolder> cagMap;

    private CRGMapping(CagMappingBuilder cmb) {
        this.cagMap = cmb.cagMap;
    }

    public Map<String, String> getMapping(String coi) {
        if (cagMap.containsKey(coi)) {
            CagHolder ch = cagMap.get(coi);
            Map retMap = Maps.newHashMap();
            retMap.put("Geography", ch.geography);
            retMap.put("Region", ch.region);
            retMap.put("Groupings", ch.grouping);

            return retMap;
        }
        return null;
    }

    public static class CagMappingBuilder {
        private Map<String, CagHolder> cagMap;

        public CagMappingBuilder() {
            cagMap = Maps.newHashMap();
        }

        public CagMappingBuilder withCag(String coi, String geo, String gru, String reg) {
            CagHolder holder = new CagHolder(geo, gru, reg);
            this.cagMap.put(coi, holder);
            return this;
        }

        public CRGMapping build() {
            return new CRGMapping(this);
        }
    }


    private static class CagHolder {
        final String geography, grouping, region;

        public CagHolder(String geography, String grouping, String region) {
            this.geography = geography;
            this.region = region;
            this.grouping = grouping;
        }
    }

}
