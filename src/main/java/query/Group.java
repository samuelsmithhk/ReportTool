package query;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by samuelsmith on 09/11/2014.
 */
public class Group {
    public final String groupKey;
    public final List<QueryResultDeal> groupValues;

    public Group(String groupKey) {
        this.groupKey = groupKey;
        this.groupValues = Lists.newArrayList();
    }

    public void addDeal(QueryResultDeal deal) {
        groupValues.add(deal);
    }
}
