package deal;

import com.google.common.collect.Maps;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by samuelsmith on 01/11/2014.
 */
public class DealProperty {

    private final TreeMap<DateTime, Value> values; //contains all versions of the value

    private DealProperty(DealPropertyBuilder dpb) {
        values = dpb.values;
    }

    public Value getLatestValue() {
        return values.lastEntry().getValue();
    }

    public Value getValueAtTimestamp(DateTime timestamp) {
        if (values.containsKey(timestamp)) return values.get(timestamp);
        throw new IllegalArgumentException("No value for given timestamp");
    }

    public void addValue(DateTime timestamp, Value value) {
        if (!values.containsKey(timestamp)) values.put(timestamp, value);
        else throw new IllegalArgumentException("Value already exists for timestamp");
    }

    @Override
    public String toString() {
        return String.valueOf(values);
    }


    public static class DealPropertyBuilder<T> {

        private TreeMap<DateTime, Value> values = Maps.newTreeMap();


        public DealPropertyBuilder withValue(DateTime timestamp, Value value) {
            if (values.containsKey(timestamp))
                throw new IllegalArgumentException("Value already exists for timestamp");
            this.values.put(timestamp, value);
            return this;
        }

        public DealPropertyBuilder withValues(Map<DateTime, Value> values) {
            DealPropertyBuilder retDPB = new DealPropertyBuilder();

            for (Map.Entry<DateTime, Value> value : values.entrySet()) {
                retDPB = retDPB.withValue(value.getKey(), value.getValue());
            }

            return retDPB;
        }

        public DealProperty build() {
            return new DealProperty(this);
        }
    }

    public static class Value<T> {

        public enum ValueType {
            STRING, NUMERIC, BOOLEAN, BLANK
        }

        public final T innerValue;
        public final ValueType type;

        public Value(T innerValue, ValueType type){
            this.innerValue = innerValue;
            this.type = type;
        }

        @Override
        public String toString() {
            return (String.valueOf(innerValue) + " (type: " + type + ")");
        }

    }

}
