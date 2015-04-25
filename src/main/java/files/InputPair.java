package files;

import deal.Deal;
import org.joda.time.DateTime;

import java.util.Map;

public class InputPair {

    public final DateTime timestamp;
    public final Map<String, Deal> dealMap;
    public final String filename, directory;
    public final String sourceSystem;

    public InputPair(String sourceSystem, String directory, String filename, DateTime timestamp,
                     Map<String, Deal> dealMap) {
        this.sourceSystem = sourceSystem;
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
