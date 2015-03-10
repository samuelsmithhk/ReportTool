package parse;

import managers.MappingManager;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;

public class DCFileNameParser {

    public static SheetParser getParser(String filename, Workbook wb, DateTime timestamp)
            throws Exception {

        MappingManager mm = MappingManager.getMappingManager();

        filename = filename.toLowerCase();
        if (filename.contains("pipeline")) return new DCPipelineParser(wb, timestamp, mm.loadColumnMap("dcPipeline"));
        else if (filename.contains("ic_summary"))
            return new DCICSummaryParser(wb, timestamp, mm.loadColumnMap("icSummary"), mm.loadICDateMap());

        throw new Exception("Unable to find parser for file: " + filename);
    }

}
