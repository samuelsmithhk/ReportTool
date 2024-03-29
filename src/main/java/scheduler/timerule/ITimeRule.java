package scheduler.timerule;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Queue;

public interface ITimeRule {

    String getType();

    Queue<DateTime> getDateTimes();

    void addExclusion(String instance);

    List<DateTime> getExclusions();
}
