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
        Queue<DateTime> retQueue = new PriorityQueue<>();

        LocalDate today = new LocalDate();
        int numberOfMonths = Months.monthsBetween(today, until).getMonths();

        for (int i = 1; i <= numberOfMonths; i += every) {
            switch (param1) {
                case "first":
                    retQueue.add(mergeDateTime(getNthOfMonth(1, i, param2), runAt));
                    break;
                case "second":
                    retQueue.add(mergeDateTime(getNthOfMonth(2, i, param2), runAt));
                    break;
                case "third":
                    retQueue.add(mergeDateTime(getNthOfMonth(3, i, param2), runAt));
                    break;
                case "fourth":
                    retQueue.add(mergeDateTime(getNthOfMonth(4, i, param2), runAt));
                    break;
                case "last":
                    retQueue.add(mergeDateTime(getNthOfMonth(5, i, param2), runAt));
                    break;
            }
        }

        return purgeExcluded(purgeOldInstances(retQueue));
    }

    private LocalDate getNthOfMonth(int weekNo, int monthsToAdd, DAY dayOfWeek) {
        int yearsToAdd = 0;
        int monthOfYear = monthsToAdd;

        if (monthsToAdd > 12) {
            yearsToAdd = monthsToAdd / 12;
            monthOfYear = (monthOfYear - (12 * yearsToAdd)) + 1;
        }

        LocalDate ret = new LocalDate().plusYears(yearsToAdd).withMonthOfYear(monthOfYear).withDayOfMonth(1)
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
