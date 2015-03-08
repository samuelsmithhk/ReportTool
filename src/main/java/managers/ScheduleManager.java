package managers;

import files.ScheduleFileManager;
import scheduler.JobInstance;
import scheduler.Schedule;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class ScheduleManager {

    private final ScheduleFileManager sfm;
    private static ScheduleManager sm;

    public static void initScheduleManager(ScheduleFileManager sfm) {
        if (sm == null) sm = new ScheduleManager(sfm);
    }

    public static ScheduleManager getScheduleManager() throws Exception {
        if (sm == null)
            throw new Exception("ScheduleManager needs to be instantiated with instance of ScheduleFileManager");
        return sm;
    }

    private ScheduleManager(ScheduleFileManager sfm) {
        this.sfm = sfm;
    }

    private Schedule loadSchedule() {
        List<JobInstance.Job> jobs = sfm.loadJobs();

        Queue<JobInstance> masterQueue = new PriorityQueue<JobInstance>();

        for (JobInstance.Job j : jobs) {
            Queue<JobInstance> instances = j.createJobInstances();
            masterQueue.addAll(instances);
        }

        return new Schedule(masterQueue);
    }

    public void startSchedule() {
        Schedule schedule = loadSchedule();
        new Thread(schedule).start();
    }
}
