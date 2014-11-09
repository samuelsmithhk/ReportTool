package query;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by samuelsmith on 09/11/2014.
 */
public class Query {

    public final List<String> columns;
    public final String groupBy, filterColumn, filterValue;

    private Query(QueryBuilder qb) {
        this.columns = qb.columns;
        this.groupBy = qb.groupBy;
        this.filterColumn = qb.filterColumn;
        this.filterValue = qb.filterValue;
    }

    public class QueryBuilder {
         List<String> columns;
         String groupBy, filterColumn, filterValue;

        public QueryBuilder() {
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
    }

}
