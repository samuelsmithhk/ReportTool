package cache;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.*;
import deal.Deal;
import deal.DealProperty;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Cache {

    private static final Logger logger = LoggerFactory.getLogger(Cache.class);
    private final Set<String> columnIndex;
    private final Map<String, Deal> deals;
    private Map<String, DateTime> directoriesLastUpdated;
    private Map<String, LocalDate> sourceSystemsLastUpdated;

    public Cache() {
        logger.info("Creating new cache");
        this.deals = Maps.newHashMap();
        this.columnIndex = Sets.newTreeSet();
        this.directoriesLastUpdated = Maps.newHashMap();
        this.sourceSystemsLastUpdated = Maps.newHashMap();
    }


    public static Object parseInnerValue(DealProperty.Value.ValueType type, JsonElement innerValue) {
        switch (type) {
            case BL:
                return "";
            case BO:
                return innerValue.getAsBoolean();
            case NU:
                return innerValue.getAsDouble();
            default:
                return innerValue.getAsString();
        }
    }

    public static DealProperty.Value.ValueType parseType(String typeStr) {
        if (typeStr.equals("BL")) return DealProperty.Value.ValueType.BL;
        if (typeStr.equals("BO")) return DealProperty.Value.ValueType.BO;
        if (typeStr.equals("NU")) return DealProperty.Value.ValueType.NU;
        return DealProperty.Value.ValueType.ST;
    }

    public void processDealUpdate(String sourceSystem, String directory, DateTime timestamp,
                                  Map<String, Deal> newDeals) {
        logger.info("Processing deal update with newDeals");

        for (Map.Entry<String, Deal> entry : newDeals.entrySet()) {
            Deal deal = entry.getValue();

            //update column index
            columnIndex.addAll(deal.dealProperties.keySet());

            if (deals.containsKey(entry.getKey())) {
                //update deal
                logger.info("Updating deal {}", entry.getKey());
                deals.get(entry.getKey()).updateDeal(timestamp, deal);
            } else {
                //new deal
                deals.put(entry.getKey(), deal);
            }
        }

        if (directoriesLastUpdated.get(directory) == null) directoriesLastUpdated.put(directory, timestamp);
        else if (directoriesLastUpdated.get(directory).isBefore(timestamp))
            directoriesLastUpdated.put(directory, timestamp);

        LocalDate tsLD = timestamp.toLocalDate();

        if (sourceSystemsLastUpdated.get(sourceSystem) == null)
            sourceSystemsLastUpdated.put(sourceSystem, tsLD);
        else if (sourceSystemsLastUpdated.get(sourceSystem).isBefore(timestamp.toLocalDate()))
            sourceSystemsLastUpdated.put(sourceSystem, tsLD);
    }

    public DateTime getDirectoriesLastUpdated(String directory) {
        return directoriesLastUpdated.get(directory);
    }

    public Map<String, DateTime> getDirectoriesLastUpdated() {
        return directoriesLastUpdated;
    }

    public LocalDate getSnapshotDate() {
        DateTime latest = null;
        for (DateTime dt : directoriesLastUpdated.values()) if (latest == null || dt.isAfter(latest)) latest = dt;
        return latest == null ? null : latest.toLocalDate();
    }

    public Map<String, Deal> getDeals() {
        return deals;
    }

    public Set<String> getCols() {
        return columnIndex;
    }

    public Set<String> getSourceSystems() {
        return sourceSystemsLastUpdated.keySet();
    }

    public Deal getDeal(String dealName) throws CacheException {
        if (deals.containsKey(dealName)) return deals.get(dealName);
        else throw new CacheException("Deal does not exist in cache: " + dealName);
    }

    public Map<String, LocalDate> getSourceSystemsLastUpdated() {
        return sourceSystemsLastUpdated;
    }

    public LocalDate getSourceSystemLastUpdated(String sourceSystem) {
        return sourceSystemsLastUpdated.get(sourceSystem);
    }

    public void purgeOldData() {
        logger.info("Purging old data from cache");

        for (Iterator<Map.Entry<String, Deal>> d = deals.entrySet().iterator(); d.hasNext();) {
            Map.Entry<String, Deal> element = d.next();
            if (!element.getValue().purgeOldData())
                d.remove();
        }

    }

    public class CacheException extends Exception {
        public CacheException(String e) {
            super(e);
        }
    }
}