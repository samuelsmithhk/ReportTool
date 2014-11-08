package parse;

import files.InputFileManager;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;

/**
 * Created by samuelsmith on 08/11/2014.
 */
public class FileNameParser {

    public static SheetParser getParser(File file, InputFileManager ifm) throws IOException, InvalidFormatException {
        SheetParser parser;

        Workbook wb = WorkbookFactory.create(file);

        if (file.getName().contains("Deal_Pipeline_Report")) {
            return new DCParser(wb, ifm.getTimestamp(file));
        }

        throw new IOException("Not a valid input spreadsheet: " + file.getAbsolutePath());
    }
}
