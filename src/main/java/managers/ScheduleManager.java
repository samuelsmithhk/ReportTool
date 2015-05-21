package managers;

import files.ScheduleFileManager;
import org.joda.time.LocalDate;
import scheduler.JobInstance;
import scheduler.Schedule;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class ScheduleManager {

    private static ScheduleManager sm;
    private final ScheduleFileManager sfm;
    List<JobInstance.Job> jobs;
    private Schedule schedule;

    private ScheduleManager(ScheduleFileManager sfm) {
        this.sfm = sfm;
    }

    public static void initScheduleManager(ScheduleFileManager sfm) {
        if (sm == null) sm = new ScheduleManager(sfm);
    }

    public static ScheduleManager getScheduleManager() throws Exception {
        if (sm == null)
            throw new Exception("ScheduleManager needs to be instantiated with instance of ScheduleFileManager");
        return sm;
    }

    private Schedule loadSchedule() {
            jobs = sfm.loadJobs();

            Queue<JobInstance> masterQueue = new PriorityQueue<>();

            for (JobInstance.Job j : jobs) {
                Queue<JobInstance> instances = j.createJobInstances();
                masterQueue.addAll(instances);
            }

            return new Schedule(masterQueue);
    }

    public void startSchedule() throws InterruptedException {
        if (schedule != null) shutdownSchedule();

        schedule = loadSchedule();
        new Thread(schedule).start();
    }

    public List<JobInstance> getJobsForDate(LocalDate date) {
        if (sfm.hasUpdate()) loadSchedule();
        return schedule.getJobInstancesForDate(date);
    }

    public JobInstance.Job getJobByName(String jobName) {
        for (JobInstance.Job job : jobs) if (job.getName().equals(jobName)) return job;
        return null;
    }

    public void removeJobInstance(String jobName, String instance) throws FileNotFoundException, InterruptedException {
        JobInstance.Job j = getJob(jobName);
        j.addExclusion(instance);
        sfm.saveJob(j);
        startSchedule();
    }

    public JobInstance.Job getJob(String jobName) {
        for (JobInstance.Job j : jobs) if (j.getName().equals(jobName)) return j;
        return null;
    }

    public synchronized void removeJob(String jobName) throws InterruptedException {
        sfm.removeJob(jobName);
        startSchedule();
    }

    public synchronized void saveJob(JobInstance.Job job) throws FileNotFoundException, InterruptedException {
        sfm.saveJob(job);
        startSchedule();
    }

    public synchronized void shutdownSchedule() throws InterruptedException {
        schedule.shouldBreakOnNextLoop();

    while (!(schedule.hasBroken())) Thread.sleep(1500);
    }
}
