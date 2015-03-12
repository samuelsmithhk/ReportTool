package files;

import deal.Deal;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class InputPair {

    public final DateTime timestamp;
    public final Map<String, Deal> dealMap;
    private final String filename;

    public InputPair(String filename, DateTime timestamp, Map<String, Deal> dealMap) {
        Logger logger = LoggerFactory.getLogger(InputPair.class);
        logger.info("Creating input pair for " + timestamp + " and " + dealMap);
        this.timestamp = timestamp;
        this.dealMap = dealMap;
        this.filename = filename;
    }

    @Override
    public String toString() {
        return filename + " " + timestamp;
    }

}
