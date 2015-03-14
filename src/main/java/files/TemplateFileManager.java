package files;
import com.google.common.collect.Lists;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

    public List<String> getAllTemplates() {
        logger.info("Retrieving all templates from the filesystem");

        File dir = new File(templateDirectory);
        File[] templates = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xlsm") || name.endsWith(".xlsx") || name.endsWith(".xls");
            }
        });

        if (templates.length == 0) return new ArrayList<String>();

        List<String> retList = Lists.newArrayList();

        for (File f : templates) {
            retList.add(f.getName());
        }

        return retList;
    }
}
