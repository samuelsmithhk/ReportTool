package query;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Group {

    public final String groupKey, sortBy;
    public final List<QueryResultDeal> groupValues;

    private final ColumnCompare compare;

    public Group(String groupKey, String sortBy) {
        this.sortBy = sortBy;
        this.groupKey = groupKey;
        this.groupValues = Lists.newArrayList();
        compare = new ColumnCompare();
    }

    public void addDeal(QueryResultDeal deal) {
        groupValues.add(deal);
    }

    public void sortGroup() {
        if (!(sortBy == null || sortBy.trim().equals("") || sortBy.trim().equals("null") || sortBy.trim().equals("N/A")))
            Collections.sort(groupValues, compare);
    }

    private class ColumnCompare implements Comparator<QueryResultDeal> {
        @Override
        public int compare(QueryResultDeal o1, QueryResultDeal o2) {
            String v1 = o1.getDPValue(sortBy);
            String v2 = o2.getDPValue(sortBy);

            if (v1 == null) v1 = "";
            if (v2 == null) v2 = "";

            return v1.compareTo(v2);
        }
    }
}
