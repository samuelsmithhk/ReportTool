package parse;

import files.MappingFileManager;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;

/**
 * Created by samuelsmith on 08/02/2015.
 */
public class DCFileNameParser {

    public static SheetParser getParser(String filename, Workbook wb, DateTime timestamp, MappingFileManager mfm)
            throws Exception {
        filename = filename.toLowerCase();
        if (filename.contains("pipeline")) return new DCPipelineParser(wb, timestamp, mfm.loadColumnMap("dcPipeline"));
        else if (filename.contains("ic_summary"))
            return new DCICSummaryParser(wb, timestamp, mfm.loadColumnMap("icSummary"), mfm.loadICDateMap());

        throw new Exception("Unable to find parser for file: " + filename);
    }

}
