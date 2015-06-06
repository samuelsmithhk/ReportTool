package files;

import com.google.common.collect.Lists;
import deal.Deal;
import managers.CacheManager;
import managers.ParserManager;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parse.ValuesToDeal;
import sheetparser.SheetParser;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;

public class InputFileManager {

    private final Logger logger = LoggerFactory.getLogger(InputFileManager.class);
    private final List<String> directories;
    private final CacheManager cm;


    public InputFileManager() throws Exception {
        logger.info("Creating InputFileManager");
        directories = ParserManager.getParserConfigManager().getDirectories();
        cm = CacheManager.getCacheManager();
    }

    public boolean newInputs() throws Exception {
        logger.info("Checking to see if there's any new input files");

        for (String d : directories) {
            if (cm.getDirectoriesLastUpdated(d) == null) {
                logger.info("New input files");
                return true;
            }
            if (cm.getDirectoriesLastUpdated(d).isBefore(newestTimestamp(d))) {
                logger.info("New input files");
                return true;
            }
        }

        return false;
    }

    private DateTime newestTimestamp(String directory) throws IOException {
        logger.info("Getting the newest timestamp out of the input files");

        File[] files = getFilesForDirectory(directory);

        if (files.length == 0) return null;

        File latest = getNewestFileInFiles(files);

        return new DateTime(Files.readAttributes(latest.toPath(),
                BasicFileAttributes.class).creationTime().toMillis());
    }

    public File[] getFilesForDirectory(String directory) {
        File dir = new File(directory);

        return dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xlsx") || (name.endsWith(".xls"));
            }
        });
    }

    private File getNewestFileInFiles(File[] files) throws IOException {
        Path firstFile = files[0].toPath();
        BasicFileAttributes firstAttr = Files.readAttributes(firstFile, BasicFileAttributes.class);
        DateTime latestTimestamp = new DateTime(firstAttr.creationTime().toMillis());
        File latestFile = files[0];

        for (int i = 1; i < files.length; i++) {
            Path currentFile = files[i].toPath();
            BasicFileAttributes currentAttr = Files.readAttributes(currentFile, BasicFileAttributes.class);
            DateTime currentTimestamp = new DateTime(currentAttr.creationTime().toMillis());

            if (currentTimestamp.isAfter(latestTimestamp)) {
                latestTimestamp = currentTimestamp;
                latestFile = files[i];
            }
        }

        return latestFile;
    }

    public List<InputPair> parseNewInputs() throws Exception {
        logger.info("Parsing the new input files");
        List<InputPair> retList = Lists.newArrayList();

        for (String d : directories) {
            File[] newFiles = getAllNewFilesForDirectory(d);

            for (File f : newFiles) {
                String fName = f.getName();
                logger.info("Parsing file: {}", fName);
                Workbook wb = WorkbookFactory.create(f);

                SheetParser sheetParser = ParserManager.getParserConfigManager().getSheetParser(d, fName);
                DateTime timestamp = getTimestamp(f);
                ValuesToDeal vtd = new ValuesToDeal();
                String sourceSystem = ParserManager.getParserConfigManager().getSourceSystem(d, fName);

                Map<String, Deal> dealMap = vtd.convert(timestamp, sourceSystem, sheetParser.parse(wb));

                retList.add(new InputPair(sourceSystem, d, f.getName(), timestamp, dealMap));
            }
        }

        logger.info("Files parsed");
        return retList;
    }

    public DateTime getTimestamp(File file) throws IOException {
        logger.info("Getting the timestamp for input file: {}", file);
        BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        return new DateTime(attr.creationTime().toMillis());
    }

    private File[] getAllNewFilesForDirectory(final String directory) throws Exception {
        logger.info("Getting all the new input files");

        File dir = new File(directory);

        File[] files = dir.listFiles(new FilenameFilter() {

            DateTime cacheTimestamp = cm.getDirectoriesLastUpdated(directory),
                    fourWeeksAgo = new DateTime().minusWeeks(4);

            @Override
            public boolean accept(File dir, String name) {

                try {
                    logger.info("name: {}", name);

                    BasicFileAttributes attr = Files.readAttributes(Paths.get(dir.getAbsolutePath() + "/" + name),
                            BasicFileAttributes.class);

                    DateTime fileTimestamp = new DateTime(attr.creationTime().toMillis());

                    logger.info("Checking if {} is a new input file that is younger than 4 weeks", name);

                    if (fileTimestamp.isBefore(fourWeeksAgo)) return false;
                    if (cacheTimestamp == null) return (name.endsWith(".xlsx") || name.endsWith(".xls"));
                    logger.info("cacheTimestamp: {}", cacheTimestamp);
                    logger.info("fileTimestamp: {}", fileTimestamp);
                    return fileTimestamp.isAfter(cacheTimestamp) && (name.endsWith(".xlsx") || name.endsWith(".xls"));

                } catch (IOException e) {
                    logger.error("Error finding latest files: {}", e.getLocalizedMessage());
                    return false;
                }
            }
        });

        logger.info("Found {} new input files in directory {}", files.length, directory);
        return files;
    }

}
