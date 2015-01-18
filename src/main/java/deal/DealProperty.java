package deal;

import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by samuelsmith on 01/11/2014.
 */
public class DealProperty {

    private transient Logger logger = LoggerFactory.getLogger(DealProperty.class);

    private final TreeMap<DateTime, Value> values; //contains all versions of the value

    private DealProperty(DealPropertyBuilder dpb) {
        values = dpb.values;
    }

    public Value getLatestValue() {
        return values.lastEntry().getValue();
    }

    public Value getValueAtTimestamp(DateTime timestamp) {
        logger.info("Getting value at timestamp: " + timestamp);

        if (values.containsKey(timestamp)) return values.get(timestamp);
        throw new IllegalArgumentException("No value for given timestamp");
    }

    public Value getValueMinusXDays(int days) {
        DateTime timestamp = values.lastKey().minusDays(days);

        if (values.firstKey().isAfter(timestamp)) return values.get(values.firstKey());

        for (DateTime compare : values.keySet()) {
            if (!(compare.isBefore(timestamp))) return values.get(compare);
        }

        logger.warn("No value found at minus x days, returning latest value");
        return values.lastEntry().getValue();

    }

    public void addValue(DateTime timestamp, Value value) {
        logger.info("Adding value: " + value + " to DealProperty " + this);
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
