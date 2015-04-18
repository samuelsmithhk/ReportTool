package scheduler.timerule;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

public class NoRepeat extends AbstractTimeRule {

    private DateTime executionDateTime;

    public NoRepeat(String date, String time) {
        super(new ArrayList<DateTime>());
        executionDateTime = mergeDateTime(parseDate(date), parseTime(time));
    }

    @Override
    public String getType() {
        return "NoRepeat";
    }

    @Override
    public Queue<DateTime> getDateTimes() {
        Queue<DateTime> retQueue = new PriorityQueue<DateTime>();
        retQueue.add(executionDateTime);
        return purgeExcluded(purgeOldInstances(retQueue));
    }

    public LocalDate getExecutionDate() {
        return executionDateTime.toLocalDate();
    }

    public LocalTime getExecutionTime() {
        return executionDateTime.toLocalTime();
    }
}
