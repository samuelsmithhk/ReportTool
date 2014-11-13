package query;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by samuelsmith on 09/11/2014.
 */
public class Query {

    private transient Logger logger = LoggerFactory.getLogger(Query.class);

    public final List<String> columns;
    public final String name, groupBy, filterColumn, filterValue;

    private Query(QueryBuilder qb) {
        logger.info("Creating query");

        this.name = qb.name;
        this.columns = qb.columns;
        this.groupBy = qb.groupBy;
        this.filterColumn = qb.filterColumn;
        this.filterValue = qb.filterValue;
    }

    public static class QueryBuilder {
         List<String> columns;
         String name, groupBy, filterColumn, filterValue;

        public QueryBuilder(String name) {
            this.name = name;
            columns = null;
            groupBy = null;
            filterColumn = null;
            filterValue = null;
        }

        public QueryBuilder withColumn(String column) {
            if (this.columns == null) {
                this.columns = Lists.newArrayList();
            }

            this.columns.add(column);

            return this;
        }

        public QueryBuilder withColumns(List<String> columns) {
            QueryBuilder qb = this;

            for (String c : columns) {
                qb = qb.withColumn(c);
            }

            return this;
        }

        public QueryBuilder setGroupBy(String groupBy) {
            this.groupBy = groupBy;
            return this;
        }

        public QueryBuilder setFilter(String filterColumn, String filterValue) {
            this.filterColumn = filterColumn;
            this.filterValue = filterValue;
            return this;
        }

        public Query build() {
            return new Query(this);
        }
    }

}
