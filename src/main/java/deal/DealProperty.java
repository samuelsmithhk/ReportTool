package deal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DealProperty {

    private transient Logger logger = LoggerFactory.getLogger(DealProperty.class);

    private final TreeMap<DateTime, Value> values; //contains all versions of the value

    private DealProperty(DealPropertyBuilder dpb) {
        values = dpb.values;
    }

    public Value getLatestValue() {
        return values.lastEntry().getValue();
    }

    public Set<Value> getValuesForDayRange(int r1, int r2) {
        //special case, both values 0, return all
        if ((r1 == 0) && (r2 == 0))
            return Sets.newLinkedHashSet(values.values());

        int rangeStart = Math.max(r1, r2), rangeEnd = Math.min(r1, r2);

        DateTime firstDate = values.lastKey().minusDays(rangeStart);
        DateTime lastDate = values.lastKey().minusDays(rangeEnd);

        Set<Value> retValues = Sets.newLinkedHashSet();

        boolean withinRange = false;
        for (Map.Entry<DateTime, Value> entry : values.entrySet()) {
            if (withinRange) {
                if (entry.getKey().isBefore(lastDate))
                    retValues.add(entry.getValue());
                else if (entry.getKey().isEqual(lastDate) || entry.getKey().isAfter(lastDate)) break;
            }

            if (!withinRange)
                if (entry.getKey().isEqual(firstDate) || entry.getKey().isAfter(firstDate)) {
                    withinRange = true;
                    retValues.add(entry.getValue());
                }
        }

        return retValues;
    }

    public Value getValueAtTimestamp(DateTime timestamp) {
        logger.info("Getting value at timestamp: " + timestamp);

        if (values.containsKey(timestamp)) return values.get(timestamp);

        throw new IllegalArgumentException("No value for given timestamp");
    }

    public Value getSnapshotValue(LocalDate snapshot) {
        int snapshotYear = snapshot.getYear();
        int snapshotMonth = snapshot.getMonthOfYear();
        int snapshotDay = snapshot.getDayOfMonth();

        for (DateTime key : values.keySet()) {
            int keyYear = key.getYear();
            int keyMonth = key.getMonthOfYear();
            int keyDay = key.getDayOfMonth();

            if (snapshotYear == keyYear)
                if (snapshotMonth == keyMonth)
                    if (snapshotDay == keyDay)
                        return values.get(key);
        }

        return null;
    }

    public Value getValueMinusXDays(int days) {
       /* DateTime timestamp = values.lastKey().minusDays(days);

        if (values.firstKey().isAfter(timestamp)) return values.get(values.firstKey());

        List<DateTime> dates = Lists.newLinkedList(values.keySet());

        for (int i = dates.size() -1; i >= 0; i--) {
            DateTime compare = dates.get(i);
            if (!(compare.isAfter(timestamp))) return values.get(compare);
        }

        logger.warn("No value found at minus x days, returning latest value");
        return values.lastEntry().getValue();*/

        LocalDate timestamp = LocalDate.now().minusDays(days);
        return getSnapshotValue(timestamp);

    }

    public void addValue(DateTime timestamp, Value value) {
        if (!values.containsKey(timestamp)) values.put(timestamp, value);
    }

    @Override
    public String toString() {
        return String.valueOf(values);
    }


    public static class DealPropertyBuilder {

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

    public static class Value {

        public enum ValueType {
            STRING, NUMERIC, BOOLEAN, BLANK
        }

        public final Object innerValue;
        public final ValueType type;

        public Value(Object innerValue, ValueType type){
            this.innerValue = innerValue;
            this.type = type;
        }

        @Override
        public String toString() {
            return (String.valueOf(innerValue) + " (type: " + type + ")");
        }

    }

}
