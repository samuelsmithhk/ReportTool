package query;

import java.util.List;

/**
 * Created by samuelsmith on 09/11/2014.
 */
public class QueryResult {

    public final Query query;
    public final List<Group> valuesGrouped;
    public final String queryName;
    public final List<Query.Header> headers;

    public QueryResult(Query query, String queryName, List<Group> valuesGrouped, List<Query.Header> headers) {
        this.query = query;
        this.valuesGrouped = valuesGrouped;
        this.queryName = queryName;
        this.headers = headers;
    }


}
