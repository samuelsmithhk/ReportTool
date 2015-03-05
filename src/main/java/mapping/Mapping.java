package mapping;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;


public class Mapping {

    private final Map<String, String> headerMap;
    private final Map<String, List<Map.Entry<String, String>>> valueMap;

    private Mapping(MappingBuilder mb) {
        this.headerMap = mb.headerMap;
        this.valueMap = mb.valueMap;
    }

    public String getHeaderMapping(String header) {
        if (headerMap.containsKey(header)) return headerMap.get(header);
        return header;
    }

    public Map.Entry<String, String> getMapping(String header, String value) {
        String retHeader = header, retValue = value;

        if (headerMap.containsKey(header)) {
            retHeader = headerMap.get(header);

            if (valueMap.containsKey(retHeader)) {
                List<Map.Entry<String, String>> values = valueMap.get(retHeader);

                for (Map.Entry<String, String> entry : values)
                    if (entry.getKey().equals(value)) {
                        retValue = entry.getValue();
                        break;
                    }
            }
        }

        return new AbstractMap.SimpleEntry<String, String>(retHeader, retValue);
    }

    public static class MappingBuilder {
        Map<String, String> headerMap;
        Map<String, List<Map.Entry<String, String>>> valueMap;

        public MappingBuilder() {
            this.headerMap = Maps.newHashMap();
            this.valueMap = Maps.newHashMap();
        }

        public MappingBuilder addColumnMap(String toMapTo, String toBeMapped) {
            this.headerMap.put(toMapTo, toBeMapped);
            return this;
        }

        public MappingBuilder addValueMap(String column, String toMapTo, String toBeMapped) {
            AbstractMap.SimpleEntry<String, String> toAdd = new AbstractMap.SimpleEntry<String, String>(toMapTo, toBeMapped);

            if (this.valueMap.containsKey(column)) {
                List<Map.Entry<String, String>> values = valueMap.get(column);
                values.add(toAdd);
                this.valueMap.put(column, values);
            } else {
                List<Map.Entry<String, String>> values = Lists.newArrayList();
                values.add(toAdd);
                this.valueMap.put(column, values);
            }

            return this;
        }

        public Mapping build() {
            return new Mapping(this);
        }
    }

}
