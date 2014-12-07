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

    public final List<Header> headers;
    public final String name, groupBy, filterColumn, filterValue;

    private Query(QueryBuilder qb) {
        logger.info("Creating query");

        this.name = qb.name;
        this.headers = qb.headers;
        this.groupBy = qb.groupBy;
        this.filterColumn = qb.filterColumn;
        this.filterValue = qb.filterValue;
    }

    public static class QueryBuilder {
        List<Header> headers;
        String name, groupBy, filterColumn, filterValue;

        public QueryBuilder(String name) {
            this.name = name;
            headers = null;
            groupBy = null;
            filterColumn = null;
            filterValue = null;
        }

        public QueryBuilder withColumns(String header, String[] columns) {
            if (this.headers == null) {
                this.headers = Lists.newLinkedList();
            }

            Header _header = new Header(header, columns);

            this.headers.add(_header);

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

    public static class Header {
        public final String header;
        public final String[] subs;

        public Header(String header, String[] subs) {
            this.header = header;
            this.subs = subs;
        }
    }

}
