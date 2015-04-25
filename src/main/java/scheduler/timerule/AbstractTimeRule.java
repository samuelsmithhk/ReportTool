package scheduler.timerule;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public abstract class AbstractTimeRule implements ITimeRule {

    private final Logger logger = LoggerFactory.getLogger(AbstractTimeRule.class);
    private List<DateTime> exclude;

    public AbstractTimeRule() {
        exclude = new ArrayList<>();
    }

    public LocalDate parseDate(String date) {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd");
        return dtf.parseLocalDate(date);
    }

    public LocalTime parseTime(String time) {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("HHmm");
        return dtf.parseLocalTime(time);
    }

    public DateTime mergeDateTime(LocalDate date, LocalTime time) {
        return date.toDateTime(time);
    }

    public List<DAY> parseDays(List<String> days) {
        List<DAY> retList = Lists.newLinkedList();

        for (String d : days) {
            try {
                retList.add(parseDay(d));
            } catch (Exception e) {
                logger.error("Exception parsing day: {}", e.getMessage(), e);
            }
        }

        return retList;
    }

    public DAY parseDay(String day) {
        day = day.trim().toLowerCase();
        if (day.equals("mon")) return DAY.MON;
        if (day.equals("tue")) return DAY.TUE;
        if (day.equals("wed")) return DAY.WED;
        if (day.equals("thu")) return DAY.THU;
        if (day.equals("fri")) return DAY.FRI;
        if (day.equals("sat")) return DAY.SAT;
        if (day.equals("sun")) return DAY.SUN;
        return DAY.MON;
    }

    @Override
    public void addExclusion(String instance) {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
        DateTime exclusion = dtf.parseDateTime(instance);
        exclude.add(exclusion);
    }

    @Override
    public List<DateTime> getExclusions() {
        return exclude;
    }

    public Queue<DateTime> purgeOldInstances(Queue<DateTime> queue) {
        DateTime now = new DateTime();

        for (int count = 0; count < queue.size(); count++) {
            if (queue.element().isBefore(now)) queue.remove();
            else break;
        }

        return queue;
    }

    public Queue<DateTime> purgeExcluded(Queue<DateTime> queue) {
        queue.removeAll(exclude);
        return queue;
    }

    public enum DAY {
        MON, TUE, WED, THU, FRI, SAT, SUN
    }
}
