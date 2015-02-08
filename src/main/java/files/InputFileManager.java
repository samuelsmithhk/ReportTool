package files;

import cache.Cache;
import com.google.common.collect.Lists;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parse.DCFileNameParser;
import parse.DCPipelineParser;
import parse.EverestParser;
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
    private final String everestDirectory, dealCentralDirectory;


    public InputFileManager(Cache cache, String everestDirectory, String dealCentralDirectory) {
        logger.info("Creating InputFileManager");
        this.cache = cache;
        this.everestDirectory = everestDirectory;
        this.dealCentralDirectory = dealCentralDirectory;
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

        File[] everestFiles = getFilesForDirectory(everestDirectory);
        File[] dealCentralFiles = getFilesForDirectory(dealCentralDirectory);

        if ((everestFiles.length == 0) && (dealCentralFiles.length == 0)) return null;

        File latestEverest = everestFiles.length == 0 ? null : getNewestFileInFiles(everestFiles);
        File latestDealCentral = dealCentralFiles.length == 0 ? null : getNewestFileInFiles(dealCentralFiles);

        DateTime everestTimestamp = latestEverest == null ?
                null : new DateTime(Files.readAttributes(latestEverest.toPath(),
                                BasicFileAttributes.class).creationTime().toMillis());

        DateTime dealCentralTimestamp = latestDealCentral == null ?
                null : new DateTime(Files.readAttributes(latestDealCentral.toPath(),
                BasicFileAttributes.class).creationTime().toMillis());

        if (everestTimestamp == null) return dealCentralTimestamp;
        if (dealCentralTimestamp == null) return everestTimestamp;

        return everestTimestamp.isAfter(dealCentralTimestamp) ? everestTimestamp : dealCentralTimestamp;
    }

    private File[] getFilesForDirectory(String directory) {
        File dir = new File(directory);
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xlsx") || (name.endsWith(".xls"));
            }
        });

        return files;
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

    public List<InputPair> parseNewInputs(MappingFileManager mfm) throws Exception {
        logger.info("Parsing the new input files");
        List<InputPair> retList = Lists.newArrayList();

        File[] newEverestFiles = getAllNewFilesForDirectory(everestDirectory);
        File[] newDealCentralFiles = getAllNewFilesForDirectory(dealCentralDirectory);

        for (File f : newEverestFiles) {
            logger.info("Parsing everest file: " + f);
            Workbook wb = WorkbookFactory.create(f);

            String mapName = f.getName().contains("NBFC") ? "everestNBFC" : "everestSpecial";

            SheetParser parser = new EverestParser(wb, getTimestamp(f), mfm.loadColumnMap(mapName), mfm.loadCagMap());
            retList.add(new InputPair(getTimestamp(f), parser.parse()));
        }

        for (File f : newDealCentralFiles) {
            logger.info("Parsing deal central file: " + f);
            Workbook wb = WorkbookFactory.create(f);
            SheetParser parser = DCFileNameParser.getParser(f.getName(), wb, getTimestamp(f), mfm);
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

    private File[] getAllNewFilesForDirectory(String directory) {
        logger.info("Getting all the new input files");

        File dir = new File(directory);
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

                    if (cacheTimestamp == null) return (name.endsWith(".xlsx") || name.endsWith(".xls"));
                    logger.info("cacheTimestamp: " + cacheTimestamp);
                    logger.info("fileTimestamp: " + fileTimestamp);
                    if (fileTimestamp.isAfter(cacheTimestamp)) return (name.endsWith(".xlsx") || name.endsWith(".xls"));
                    return false;

                } catch (IOException e) {
                    logger.error("Error finding latest files: " + e.getLocalizedMessage());
                    return false;
                }
            }
        });

        logger.info("Found " + files.length + " new input files in directory " + directory);
        return files;
    }

}
