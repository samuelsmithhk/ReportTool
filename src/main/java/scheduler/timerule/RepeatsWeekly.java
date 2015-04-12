package scheduler.timerule;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Weeks;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class RepeatsWeekly extends AbstractTimeRule {

    private int every;
    private List<DAY> days;
    private LocalTime runAt;
    private LocalDate startingFrom, until;

    public RepeatsWeekly(int every, List<String> days, String runAt, String startingFrom, String until) {
        this.every = every;
        this.days = parseDays(days);
        this.runAt = parseTime(runAt);
        this.startingFrom = parseDate(startingFrom);
        this.until = parseDate(until);
    }

    @Override
    public String getType() {
        return "RepeatsWeekly";
    }

    @Override
    public Queue<DateTime> getDateTimes() {
        Queue<DateTime> retQueue = new PriorityQueue<DateTime>();

        int numberOfWeeks = Weeks.weeksBetween(startingFrom, until).getWeeks();

        for (int i = 0; i < numberOfWeeks; i += every) {
            LocalDate week = startingFrom.plusWeeks(i);
            for (DAY d : days) {
                LocalDate date = week.withDayOfWeek(d.ordinal() + 1);
                retQueue.add(mergeDateTime(date, runAt));
            }
        }

        return purgeOldInstances(retQueue);
    }

    public int getEvery() {
        return every;
    }

    public List<DAY> getDays() {
        return days;
    }

    public LocalTime getExecutionTime() {
        return runAt;
    }

    public LocalDate getStartingFrom() {
        return startingFrom;
    }

    public LocalDate getUntil() {
        return until;
    }
}
