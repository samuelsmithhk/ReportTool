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

    private Schedule loadSchedule(Schedule currentSchedule) {
        if (sfm.hasUpdate()) {
            jobs = sfm.loadJobs();

            Queue<JobInstance> masterQueue = new PriorityQueue<>();

            for (JobInstance.Job j : jobs) {
                Queue<JobInstance> instances = j.createJobInstances();
                masterQueue.addAll(instances);
            }

            return new Schedule(masterQueue);
        }

        return currentSchedule;
    }

    public void startSchedule() {
        schedule = loadSchedule(null);
        new Thread(schedule).start();
    }

    public List<JobInstance> getJobsForDate(LocalDate date) {
        if (sfm.hasUpdate()) loadSchedule(schedule);
        return schedule.getJobInstancesForDate(date);
    }

    public JobInstance.Job getJobByName(String jobName) {
        for (JobInstance.Job job : jobs) if (job.getName().equals(jobName)) return job;
        return null;
    }

    public void removeJobInstance(String jobName, String instance) throws FileNotFoundException {
        JobInstance.Job j = getJob(jobName);
        j.addExclusion(instance);
        sfm.saveJob(j);
        schedule = loadSchedule(schedule);
    }

    public JobInstance.Job getJob(String jobName) {
        for (JobInstance.Job j : jobs) if (j.getName().equals(jobName)) return j;
        return null;
    }

    public void removeJob(String jobName) {
        sfm.removeJob(jobName);
        schedule = loadSchedule(schedule);
    }

    public void saveJob(JobInstance.Job job) throws FileNotFoundException {
        sfm.saveJob(job);
        schedule = loadSchedule(schedule);
    }
}
