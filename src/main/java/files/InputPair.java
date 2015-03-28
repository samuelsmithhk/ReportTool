package files;

import deal.Deal;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class InputPair {

    public final DateTime timestamp;
    public final Map<String, Deal> dealMap;
    public final String filename, directory;

    public InputPair(String directory, String filename, DateTime timestamp, Map<String, Deal> dealMap) {
        this.timestamp = timestamp;
        this.dealMap = dealMap;
        this.filename = filename;
        this.directory = directory;
    }

    @Override
    public String toString() {
        return filename + " " + timestamp;
    }

}
