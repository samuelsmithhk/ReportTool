package scheduler.timerule;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Months;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class RepeatsMonthly extends AbstractTimeRule {

    private int every, dayOfMonth;
    private LocalDate until;
    private LocalTime runAt;

    public RepeatsMonthly(int every, int dayOfMonth, String until, String runAt) {
        this.every = every;
        this.dayOfMonth = dayOfMonth;
        this.until = parseDate(until);
        this.runAt = parseTime(runAt);
    }

    public RepeatsMonthly(int every, int dayOfMonth, String until, String runAt, List<DateTime> exclude) {
        super(exclude);
        this.every = every;
        this.dayOfMonth = dayOfMonth;
        this.until = parseDate(until);
        this.runAt = parseTime(runAt);
    }

    @Override
    public String getType() {
        return "RepeatsMonthly";
    }

    @Override
    public Queue<DateTime> getDateTimes() {
        Queue<DateTime> retQueue = new PriorityQueue<DateTime>();

        LocalDate today = new LocalDate();
        int numberOfMonths = Months.monthsBetween(today, until).getMonths();

        for (int i = 0; i < numberOfMonths; i += every) {
            LocalDate date = today.plusMonths(i).withDayOfMonth(dayOfMonth);
            retQueue.add(mergeDateTime(date, runAt));
        }
        
        return purgeExcluded(purgeOldInstances(retQueue));
    }

    public int getEvery() {
        return every;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public LocalDate getUntil() {
        return until;
    }

    public LocalTime getExecutionTime() {
        return runAt;
    }
}
