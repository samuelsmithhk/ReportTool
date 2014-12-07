package files;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by samuelsmith on 27/11/2014.
 */
public class ExportFileManager {

    private final Logger logger = LoggerFactory.getLogger(ExportFileManager.class);

    private final String exportDirectory;

    public ExportFileManager(String exportDirectory) {
        this.exportDirectory = exportDirectory;
    }

    public void writeExport(String filename, Workbook result) {
        logger.info("Writing exported excel file: " + filename);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(exportDirectory + filename + ".xlsx"));
            result.write(out);
            out.close();
        } catch (IOException e) {
            logger.error("Error saving query result file: " + e.getLocalizedMessage());
        }

    }

}
