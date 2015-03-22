package scheduler;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

public class Schedule implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(Schedule.class);

    private Queue<JobInstance> jobs;

    public Schedule(Queue<JobInstance> jobs) {
        this.jobs = jobs;
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {

            if (jobs.peek() == null) {
                logger.info("No jobs left in schedule, terminating loop");
                break;
            }

            try {
                DateTime timeNow = new DateTime();

                if (timeNow.isAfter(jobs.peek().executionTime)) {
                    JobInstance job = jobs.poll();
                    logger.info("Job " + job + " is due, executing");
                    job.execute();
                }

                Thread.sleep(60000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error("Error executing query: " + e.getMessage(), e);
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
