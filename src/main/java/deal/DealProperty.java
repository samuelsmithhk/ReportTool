package deal;

import com.google.common.collect.Maps;
import org.joda.time.DateTime;

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

    public static class DealPropertyBuilder<T> {

        private TreeMap<DateTime, Value> values = Maps.newTreeMap();

        public DealPropertyBuilder withValue(DateTime timestamp, Value value) {
            this.values.put(timestamp, value);
            return this;
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

    }

}
