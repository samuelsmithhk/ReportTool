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
            }
        }
    }
}
