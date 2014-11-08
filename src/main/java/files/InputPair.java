package files;

import deal.Deal;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by samuelsmith on 08/11/2014.
 */
public class InputPair {

    private final Logger logger = LoggerFactory.getLogger(InputPair.class);

    public final DateTime timestamp;
    public final Map<String, Deal> dealMap;

    public InputPair(DateTime timestamp, Map<String, Deal> dealMap) {
        logger.info("Creating input pair for " + timestamp + " and " + dealMap);
        this.timestamp = timestamp;
        this.dealMap = dealMap;
    }

}
