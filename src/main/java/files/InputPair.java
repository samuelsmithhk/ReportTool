package files;

import deal.Deal;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class InputPair {

    public final DateTime timestamp;
    public final Map<String, Deal> dealMap;

    public InputPair(DateTime timestamp, Map<String, Deal> dealMap) {
        Logger logger = LoggerFactory.getLogger(InputPair.class);
        logger.info("Creating input pair for " + timestamp + " and " + dealMap);
        this.timestamp = timestamp;
        this.dealMap = dealMap;
    }

}
