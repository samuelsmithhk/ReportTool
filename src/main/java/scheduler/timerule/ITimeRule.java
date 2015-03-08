package scheduler.timerule;

import org.joda.time.DateTime;

import java.util.Queue;

public interface ITimeRule {

    public Queue<DateTime> getDateTimes();

}
