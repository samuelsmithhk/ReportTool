package query;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by samuelsmith on 09/11/2014.
 */
public class Group {

    private transient Logger logger = LoggerFactory.getLogger(Group.class);

    public final String groupKey;
    public final List<QueryResultDeal> groupValues;

    private final ColumnCompare compare;

    public Group(String groupKey, String sortBy) {
        logger.info("Creating group: " + groupKey);
        this.groupKey = groupKey;
        this.groupValues = Lists.newArrayList();
        compare = new ColumnCompare(sortBy);
    }

    public void addDeal(QueryResultDeal deal) {
        logger.info("Adding deal " + deal + " to group " + groupKey);
        groupValues.add(deal);
        Collections.sort(groupValues, compare);
    }

    private class ColumnCompare implements Comparator<QueryResultDeal> {

        private final String sortBy;

        public ColumnCompare(String sortBy) {
            this.sortBy = sortBy;
        }

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
