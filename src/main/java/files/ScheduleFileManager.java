package files;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

import static scheduler.JobInstance.*;

public class ScheduleFileManager {

    private final Logger logger = LoggerFactory.getLogger(ScheduleFileManager.class);

    private final String scheduleDirectory;
    private final Gson gson;

    private boolean hasUpdate;

    public ScheduleFileManager(String scheduleDirectory) {
        hasUpdate = true;
        this.scheduleDirectory = scheduleDirectory;
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Job.class, new Job.JobSerializer());
        gson = builder.create();

    }

    public List<Job> loadJobs() {
        logger.info("Loading jobs for scheduler");

        List<Job> retList = Lists.newArrayList();

        File dir = new File(scheduleDirectory);
        File[] jobFiles = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                logger.info(name);
                return name.endsWith(".job");
            }
        });

        if (jobFiles.length == 0) return Lists.newArrayList();

        for (File f : jobFiles) {
            logger.info("Parsing job: " + f.getName());

            try {
                byte[] encodedJSON = Files.readAllBytes(f.toPath());
                String json = new String(encodedJSON, Charset.defaultCharset());
                Job j = gson.fromJson(json, Job.class);
                retList.add(j);
            } catch (IOException e) {
                logger.error("ERROR parsing job file " + f.getName() + ": " + e.getMessage(), e);
            }
        }

        hasUpdate = false;
        return retList;
    }

    public synchronized void saveJob(Job job) throws FileNotFoundException {
        logger.info("Saving job");
        String json = gson.toJson(job);

        PrintWriter out;
        try {
            out = new PrintWriter(scheduleDirectory + job.getName() + ".job");
            out.print(json);
            out.close();
            hasUpdate = true;
        } catch (FileNotFoundException e) {
            logger.error("Exception saving job: " + e.getMessage(), e);
            throw e;
        }
    }

    public boolean hasUpdate() {
        return hasUpdate;
    }
}
