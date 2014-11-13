package query;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by samuelsmith on 09/11/2014.
 */
public class Group {

    private transient Logger logger = LoggerFactory.getLogger(Group.class);

    public final String groupKey;
    public final List<QueryResultDeal> groupValues;

    public Group(String groupKey) {
        logger.info("Creating group: " + groupKey);
        this.groupKey = groupKey;
        this.groupValues = Lists.newArrayList();
    }

    public void addDeal(QueryResultDeal deal) {
        logger.info("Adding deal " + deal + " to group " + groupKey);
        groupValues.add(deal);
    }
}
