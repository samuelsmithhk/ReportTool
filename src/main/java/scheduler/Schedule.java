package scheduler;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class Schedule implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Schedule.class);

    private volatile Queue<JobInstance> jobs;

    private boolean shouldBreak, hasBroken;

    public Schedule(Queue<JobInstance> jobs) {
        this.jobs = jobs;
        shouldBreak = false;
        hasBroken = false;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {

            if (shouldBreak) {
                logger.info("Should break set to true, terminating loop");
                hasBroken = true;
                break;
            }


            if (jobs.peek() == null) {
                logger.info("No jobs left in schedule, terminating loop");
                break;
            }

            try {
                DateTime timeNow = new DateTime();

                if (timeNow.isAfter(jobs.peek().executionTime)) {
                    JobInstance job = jobs.poll();
                    logger.info("Job {} is due, executing", job);
                    job.execute();
                }

                Thread.sleep(6000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error("Error executing query: {}", e.getMessage(), e);
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public List<JobInstance> getJobInstancesForDate(LocalDate date) {
        List<JobInstance> retList = Lists.newArrayList();

        for (JobInstance job : jobs) {
            LocalDate executionDate = job.executionTime.toLocalDate();
            if (date.isEqual(executionDate)) retList.add(job);
        }

        Collections.sort(retList);

        return retList;
    }

    public void shouldBreakOnNextLoop() {
        shouldBreak = true;
    }

    public boolean hasBroken() {
        return hasBroken;
    }
}
