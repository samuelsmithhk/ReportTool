package query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by samuelsmith on 31/01/2015.
 */
public class MappedColumn {
    Logger logger = LoggerFactory.getLogger(MappedColumn.class);

    public final String original, header;

    public MappedColumn(String original, String header) {
        logger.info("Constructing mapped column");

        this.original = original;
        this.header = header;
    }
}
