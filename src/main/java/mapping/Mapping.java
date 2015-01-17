package mapping;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by samuelsmith on 17/01/2015.
 */
public class Mapping {

    private final Map<String, String> headerMap;

    private Mapping(MappingBuilder mb) {
        this.headerMap = mb.headerMap;
    }

    public String getMapping(String toBeMapped) {
        if (headerMap.containsKey(toBeMapped)) return headerMap.get(toBeMapped);
        return toBeMapped;
    }

    public static class MappingBuilder {
        Map<String, String> headerMap;

        public MappingBuilder() {
            headerMap = Maps.newHashMap();
        }

        public MappingBuilder addColumnMap(String toMapTo, String toBeMapped) {
            this.headerMap.put(toMapTo, toBeMapped);
            return this;
        }

        public Mapping build() {
            return new Mapping(this);
        }
    }

}
