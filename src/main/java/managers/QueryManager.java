package managers;

import files.QueryFileManager;
import query.Query;

import java.util.List;

public class QueryManager {

    private final QueryFileManager qfm;
    private static QueryManager qm;

    public static void initQueryManager(QueryFileManager qfm) {
        if (qm == null) qm = new QueryManager(qfm);
    }

    public static QueryManager getQueryManager() throws Exception {
        if (qm == null) throw new Exception("QueryManager needs to be instantiated with instance of QueryFileManager");
        return qm;
    }

    private List<Query> currentQueries;

    private QueryManager(QueryFileManager qfm) {
        this.qfm = qfm;
    }

    public synchronized List<Query> getAllQueries() {
        if (qfm.hasUpdate()) currentQueries = qfm.loadQueries();
        return currentQueries;
    }
}