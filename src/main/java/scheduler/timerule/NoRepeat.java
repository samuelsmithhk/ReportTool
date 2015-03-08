package scheduler.timerule;

import org.joda.time.DateTime;

import java.util.PriorityQueue;
import java.util.Queue;

public class NoRepeat extends AbstractTimeRule {

    private DateTime executionDateTime;

    public NoRepeat(String date, String time) {
        executionDateTime = mergeDateTime(parseDate(date), parseTime(time));
    }

    @Override
    public Queue<DateTime> getDateTimes() {
        Queue<DateTime> retQueue = new PriorityQueue<DateTime>();
        retQueue.add(executionDateTime);
        return purgeOldInstances(retQueue);
    }
}
