package files;

import cache.Cache;
import com.google.common.collect.Lists;
import deal.Deal;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parse.FileNameParser;
import parse.SheetParser;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * Created by samuelsmith on 08/11/2014.
 */
public class InputFileManager {

    private final Logger logger = LoggerFactory.getLogger(InputFileManager.class);

    private final Cache cache;
    private final String inputsDirectory;


    public InputFileManager(Cache cache, String inputsDirectory) {
        logger.info("Creating InputFileManager");
        this.cache = cache;
        this.inputsDirectory = inputsDirectory;
    }

    public boolean newInputs() throws IOException {
        logger.info("Checking to see if there's any new input files");
        if (newestTimestamp() == null) {
            logger.info("No new input files");
            return false;
        }
        if (cache.getLastUpdated() == null) {
            logger.info("New input files");
            return true;
        }
        if (cache.getLastUpdated().isBefore(newestTimestamp())) {
            logger.info("New input files");
            return true;
        }

        logger.info("No new input files newestTimestamp: " + newestTimestamp() +
                " cacheLastUpdated: " + cache.getLastUpdated());
        return false;
    }

    private DateTime newestTimestamp() throws IOException {
        logger.info("Getting the newest timestamp out of the input files");
        File dir = new File(inputsDirectory);
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xlsx");
            }
        });

        if (files.length == 0) return null;

        Path firstFile = files[0].toPath();
        BasicFileAttributes firstAttr = Files.readAttributes(firstFile, BasicFileAttributes.class);
        DateTime latestTimestamp = new DateTime(firstAttr.creationTime().toMillis());

        for (int i = 1; i < files.length; i++) {
            Path currentFile = files[i].toPath();
            BasicFileAttributes currentAttr = Files.readAttributes(currentFile, BasicFileAttributes.class);
            DateTime currentTimestamp = new DateTime(currentAttr.creationTime().toMillis());

            if (currentTimestamp.isAfter(latestTimestamp)) latestTimestamp = currentTimestamp;
        }

        logger.info("Newest timestamp found: " + latestTimestamp);
        return latestTimestamp;
    }

    public List<InputPair> parseNewInputs() throws IOException, InvalidFormatException {
        logger.info("Parsing the new input files");
        List<InputPair> retList = Lists.newArrayList();

        File[] allNewFiles = getAllNewFiles();

        for (File f : allNewFiles) {
            logger.info("Parsing file: " + f);
            SheetParser parser = FileNameParser.getParser(f, this);
            retList.add(new InputPair(getTimestamp(f), parser.parse()));
        }

        logger.info("Files parsed");
        return retList;
    }

    public DateTime getTimestamp(File file) throws IOException {
        logger.info("Getting the timestamp for input file: " + file);
        BasicFileAttributes attr = Files.readAttributes(file.toPath(),  BasicFileAttributes.class);
        return new DateTime(attr.creationTime().toMillis());
    }

    private File[] getAllNewFiles() {
        logger.info("Getting all the new input files");

        File dir = new File(inputsDirectory);
        File[] files = dir.listFiles(new FilenameFilter() {

            DateTime cacheTimestamp = cache.getLastUpdated();

            @Override
            public boolean accept(File dir, String name) {

                try {
                    logger.info("name: " + name);

                    BasicFileAttributes attr  = Files.readAttributes(Paths.get(dir.getAbsolutePath() + "/" + name),
                            BasicFileAttributes.class);

                    DateTime fileTimestamp = new DateTime(attr.creationTime().toMillis());

                    logger.info("Checking if " + name + " is a new input file");

                    if (cacheTimestamp == null) return name.endsWith(".xlsx");
                    logger.info("cacheTimestamp: " + cacheTimestamp);
                    logger.info("fileTimestamp: " + fileTimestamp);
                    if (fileTimestamp.isAfter(cacheTimestamp)) return name.endsWith(".xlsx");
                    return false;

                } catch (IOException e) {
                    logger.error("Error finding latest files: " + e.getLocalizedMessage());
                    return false;
                }
            }
        });

        logger.info("Found " + files.length + " new input files");
        return files;
    }

}
