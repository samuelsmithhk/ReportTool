package scheduler.timerule;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Months;

import java.util.PriorityQueue;
import java.util.Queue;

public class RepeatsMonthlyDay extends AbstractTimeRule {

    private int every;
    private DAY param2;
    private String param1;
    private LocalDate until;
    private LocalTime runAt;

    public RepeatsMonthlyDay(int every, String param1, String param2, String until, String runAt) {
        this.every = every;
        this.param1 = param1;
        this.param2 = parseDay(param2);
        this.until = parseDate(until);
        this.runAt = parseTime(runAt);
    }
    

    @Override
    public String getType() {
        return "RepeatsMonthlyDay";
    }

    @Override
    public Queue<DateTime> getDateTimes() {
        Queue<DateTime> retQueue = new PriorityQueue<DateTime>();

        LocalDate today = new LocalDate();
        int numberOfMonths = Months.monthsBetween(today, until).getMonths();

        for (int i = 0; i < numberOfMonths; i += every) {
            if (param1.equals("first"))
                retQueue.add(mergeDateTime(getNthOfMonth(1, today.plusMonths(i).getMonthOfYear(), param2), runAt));
            else if (param1.equals("second"))
                retQueue.add(mergeDateTime(getNthOfMonth(2, today.plusMonths(i).getMonthOfYear(), param2), runAt));
            else if (param1.equals("third"))
                retQueue.add(mergeDateTime(getNthOfMonth(3, today.plusMonths(i).getMonthOfYear(), param2), runAt));
            else if (param1.equals("fourth"))
                retQueue.add(mergeDateTime(getNthOfMonth(4, today.plusMonths(i).getMonthOfYear(), param2), runAt));
            else if (param1.equals("last"))
                retQueue.add(mergeDateTime(getNthOfMonth(5, today.plusMonths(i).getMonthOfYear(), param2), runAt));
        }

        return purgeExcluded(purgeOldInstances(retQueue));
    }

    private LocalDate getNthOfMonth(int weekNo, int monthOfYear, DAY dayOfWeek) {
        LocalDate ret = new LocalDate().withMonthOfYear(monthOfYear).withDayOfMonth(1)
                .withDayOfWeek(dayOfWeek.ordinal() + 1).plusWeeks(weekNo);

        if (ret.getMonthOfYear() < monthOfYear) ret = ret.plusDays(7);
        if (ret.getMonthOfYear() > monthOfYear) ret = ret.minusDays(7);

        return ret;

    }

    public int getEvery() {
        return every;
    }

    public LocalDate getUntil() {
        return until;
    }

    public LocalTime getExecutionTime() {
        return runAt;
    }

    public String getParam1() {
        return param1;
    }

    public DAY getParam2() {
        return param2;
    }
} 
