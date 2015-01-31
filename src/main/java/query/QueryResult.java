package query;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by samuelsmith on 09/11/2014.
 */
public class QueryResult {

    public final Query query;
    public final String queryName;
    public final boolean hasTemplate, outputTimestamp;
    public final List<QueryResultSheet> sheets;

    private QueryResult(QueryResultBuilder qb) {
        this.query = qb.query;
        this.queryName = qb.queryName;
        this.hasTemplate = qb.hasTemplate;
        this.outputTimestamp = qb.outputTimestamp;
        this.sheets = qb.sheets;
    }

    public static class QueryResultBuilder {
        Query query;
        String queryName;
        boolean hasTemplate, outputTimestamp;
        List<QueryResultSheet> sheets;

        public QueryResultBuilder(Query query) {
            this.query = query;
            this.queryName = query.name;
            this.hasTemplate = query.hasTemplate;
            this.outputTimestamp = query.outputTimestamp;
            this.sheets = Lists.newLinkedList();
        }

        public QueryResultBuilder addSheet(QueryResultSheet sheet) {
            this.sheets.add(sheet);
            return this;
        }

        public QueryResult build() {
            return new QueryResult(this);
        }
    }

    public static class QueryResultSheet {
        public final String sheetName;
        public final List<Group> valuesGrouped;
        public final List<Query.QuerySheet.Header> headers;

        public QueryResultSheet(String sheetName, List<Group> valuesGrouped, List<Query.QuerySheet.Header> headers) {
            this.sheetName = sheetName;
            this.valuesGrouped = valuesGrouped;
            this.headers = headers;
        }
    }


}
