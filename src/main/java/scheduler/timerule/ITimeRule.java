package scheduler.timerule;

import org.joda.time.DateTime;

import java.util.Queue;

public interface ITimeRule {

    public String getType();
    public Queue<DateTime> getDateTimes();

}
