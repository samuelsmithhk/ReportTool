package files;

import com.google.common.collect.Lists;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import query.Query;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;


public class ExportFileManager {

    private final Logger logger = LoggerFactory.getLogger(ExportFileManager.class);

    private final String exportDirectory, processedMacroDirectory;

    public ExportFileManager(String exportDirectory, String processedMacroDirectory) {
        this.exportDirectory = exportDirectory;
        this.processedMacroDirectory = processedMacroDirectory;
    }

    public synchronized void writeExport(String filename, Workbook result, boolean hasTemplate, boolean useTimestamp) {
        logger.info("Writing exported excel file: {}", filename);

        FileOutputStream out;
        try {
            String extension = hasTemplate ? ".xlsm" : ".xlsx";
            String timestamp = useTimestamp ?
                    " - " + DateTime.now().toString(DateTimeFormat.forPattern("yyyyMMdd")) : "";
            out = new FileOutputStream(new File(exportDirectory + filename + timestamp + extension));
            result.write(out);
            out.close();
        } catch (IOException e) {
            logger.error("Error saving query result file: {}", e.getLocalizedMessage());
        }

    }

    public synchronized List<File> getLatestExportForQuery(final Query query) {
        logger.info("Getting filename of latest export for query {}", query);

        File dir;
        if (query.hasTemplate) dir = new File(processedMacroDirectory);
        else dir = new File(exportDirectory);

        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(query.name);
            }
        });

        logger.info("Found {} exports for query {}", files.length, query);

        if (files.length == 0) return null;
        List<File> retList = Lists.newLinkedList();

        File latestFile = files[0];
        retList.add(latestFile);
        DateTime latestTimestamp = getFileTimestamp(latestFile);

        for (int i = 1; i < files.length; i++) {
            File currentFile = files[i];
            DateTime currentTimestamp = getFileTimestamp(currentFile);

            if (currentTimestamp.isAfter(latestTimestamp)) {
                latestFile = currentFile;
                latestTimestamp = currentTimestamp;
                retList.clear();
                retList.add(latestFile);
            } else if (currentTimestamp.isEqual(latestTimestamp)) {
                retList.add(latestFile);
            }
        }

        logger.info("Latest version found: {}", latestFile);
        return retList;
    }


    public DateTime getFileTimestamp(File file) {
        logger.info("Identifying timestamp for file: {}", file);
        return new DateTime(file.lastModified());
    }

}
