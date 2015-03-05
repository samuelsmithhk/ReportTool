package files;

import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;


public class ExportFileManager {

    private final Logger logger = LoggerFactory.getLogger(ExportFileManager.class);

    private final String exportDirectory;

    public ExportFileManager(String exportDirectory) {
        this.exportDirectory = exportDirectory;
    }

    public void writeExport(String filename, Workbook result, boolean hasTemplate, boolean useTimestamp) {
        logger.info("Writing exported excel file: " + filename);

        FileOutputStream out;
        try {
            String extension = hasTemplate ? ".xlsm" : ".xlsx";
            String timestamp = useTimestamp ? "-" + DateTime.now().toString(DateTimeFormat.forPattern("yyyyMMdd-HHmmss")) : "";
            out = new FileOutputStream(new File(exportDirectory + filename + timestamp + extension));
            result.write(out);
            out.close();
        } catch (IOException e) {
            logger.error("Error saving query result file: " + e.getLocalizedMessage());
        }

    }

}
