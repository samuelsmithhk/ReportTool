package scheduler.timerule;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.PriorityQueue;
import java.util.Queue;

public class RepeatsDaily extends AbstractTimeRule {

    private int every;
    private LocalDate startingFrom, until;
    private LocalTime runAt;


    public RepeatsDaily(String startingFrom, int every, String until, String runAt) {
        this.every = every;
        this.startingFrom = parseDate(startingFrom);
        this.until = parseDate(until);
        this.runAt = parseTime(runAt);
    }

    @Override
    public String getType() {
        return "RepeatsDaily";
    }

    @Override
    public Queue<DateTime> getDateTimes() {
        Queue<DateTime> retQueue = new PriorityQueue<DateTime>();

        int numberOfDays = Days.daysBetween(startingFrom, until).getDays();

        for (int i = 0; i < numberOfDays; i += every) {
            LocalDate date = startingFrom.plusDays(i);


            retQueue.add(mergeDateTime(date, runAt));
        }

        return purgeExcluded(purgeOldInstances(retQueue));
    }

    public int getEvery() {
        return every;
    }

    public LocalDate getStartingFrom() {
        return startingFrom;
    }

    public LocalDate getUntil() {
        return until;
    }

    public LocalTime getExecutionTime() {
        return runAt;
    }
}
