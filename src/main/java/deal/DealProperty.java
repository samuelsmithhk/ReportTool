package deal;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import managers.CacheManager;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class DealProperty {

    private final TreeMap<DateTime, Value> values; //contains all versions of the value
    private transient Logger logger = LoggerFactory.getLogger(DealProperty.class);

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
        logger.info("Getting value at timestamp: {}", timestamp);

        if (values.containsKey(timestamp)) return values.get(timestamp);

        throw new IllegalArgumentException("No value for given timestamp");
    }

    public Value getSnapshotValue(LocalDate snapshot) throws Exception {
        return getSnapshotValue(snapshot, null, false);
    }

    public Value getSnapshotValue(LocalDate snapshot, String ssPriority, boolean fallback) throws Exception {

        Value fallbackVal = null;

        for (int i = 0; i <= 2; i++) {
            LocalDate snapshotDerived = snapshot.minusDays(i);
            int snapshotYear = snapshotDerived.getYear();
            int snapshotMonth = snapshotDerived.getMonthOfYear();
            int snapshotDay = snapshotDerived.getDayOfMonth();

            for (DateTime key : values.keySet()) {
                int keyYear = key.getYear();
                int keyMonth = key.getMonthOfYear();
                int keyDay = key.getDayOfMonth();

                if (snapshotYear == keyYear)
                    if (snapshotMonth == keyMonth)
                        if (snapshotDay == keyDay) {
                            Value value = values.get(key);

                            if (ssPriority != null) {
                                if (ssPriority.equals(value.ss)) {
                                    if (i > 0) {
                                        LocalDate sourceSystemLastUpdated =
                                                CacheManager.getCacheManager().getSourceSystemLastUpdated(value.ss);

                                        if (snapshotDerived.isEqual(sourceSystemLastUpdated)) return value;
                                    } else return value;
                                } else if (fallback && (fallbackVal == null)) {
                                    if (i > 0) {
                                        LocalDate sourceSystemLastUpdated =
                                                CacheManager.getCacheManager().getSourceSystemLastUpdated(value.ss);

                                        if (snapshotDerived.isEqual(sourceSystemLastUpdated)) fallbackVal = value;
                                    } else fallbackVal = value;
                                }
                            } else {
                                if (i > 0) {
                                    LocalDate sourceSystemLastUpdated =
                                            CacheManager.getCacheManager().getSourceSystemLastUpdated(value.ss);

                                    if (snapshotDerived.isEqual(sourceSystemLastUpdated)) return value;
                                } else return value;
                            }
                        }
            }
        }

        return fallbackVal;
    }

    public Value getValueMinusXDays(int days) throws Exception {
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

    /**
     *
     * @return Whether dealproperty still contains values
     */
    public boolean purgeOldData() {
        DateTime fiveWeeksAgo = new DateTime().minusWeeks(5);

        for (Iterator<DateTime> dt = values.keySet().iterator(); dt.hasNext();) {
            DateTime element = dt.next();
            if (element.isBefore(fiveWeeksAgo))
                dt.remove();
        }

        return values.size() != 0;
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

        public final Object innerValue;
        public final ValueType type;
        public final String ss; //sourceSystem

        public Value(Object innerValue, ValueType type, String ss) {
            this.innerValue = innerValue;
            this.type = type;
            this.ss = ss;
        }

        @Override
        public String toString() {
            return (String.valueOf(innerValue) + " (type: " + type + ")");
        }

        public enum ValueType {
            ST, //string
            NU, //numeric
            BO, //boolean
            BL //blank
        }

    }

}
