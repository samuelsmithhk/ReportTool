package files;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by samuelsmith on 29/12/2014.
 */
public class TemplateFileManager {

    private final Logger logger = LoggerFactory.getLogger(TemplateFileManager.class);

    private final String templateDirectory;

    public TemplateFileManager(String templateDirectory) {
        this.templateDirectory = templateDirectory;
    }

    public Workbook getTemplate(String template) {
        logger.info("Loading template: " + template);

        try {
            FileInputStream file = new FileInputStream(new File(templateDirectory + template));
            return WorkbookFactory.create(file);
        } catch (FileNotFoundException e) {
            logger.error("ERROR: Cannot find specified template: " + e.getLocalizedMessage());
            return null;
        } catch (InvalidFormatException e) {
            logger.error("ERROR: Invalid format for specified template: " + e.getLocalizedMessage());
            return null;
        } catch (IOException e) {
            logger.error("ERROR: IOException loading specified template: " + e.getLocalizedMessage());
            return null;
        }
    }

}
