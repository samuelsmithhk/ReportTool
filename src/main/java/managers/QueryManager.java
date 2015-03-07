package managers;

import cache.Cache;
import files.QueryFileManager;
import query.Query;
import query.QueryExecutor;
import query.QueryResult;

import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryManager {

    private static QueryManager qm;

    public static void initQueryManager(QueryFileManager qfm, Cache cache) {
        if (qm == null) qm = new QueryManager(qfm, cache);
    }

    public static QueryManager getQueryManager() throws Exception {
        if (qm == null) throw new Exception("QueryManager needs to be instantiated with instance of QueryFileManager");
        return qm;
    }

    private final QueryFileManager qfm;
    private volatile Map<String, Query> currentQueries;
    private final Cache cache;

    private QueryManager(QueryFileManager qfm, Cache cache) {
        this.qfm = qfm;
        this.cache = cache;
    }

    public synchronized List<Query> getAllQueries() {
        if (qfm.hasUpdate()) currentQueries = qfm.loadQueries();
        return new ArrayList<Query>(currentQueries.values());
    }

    public synchronized void executeQuery(String queryName) throws Exception {
        if (!currentQueries.containsKey(queryName)) throw new InvalidKeyException("Query"
                + queryName + " does not exist");

        Query toExecute = currentQueries.get(queryName);
        QueryResult qr = QueryExecutor.executeQuery(cache, toExecute);
        ExportManager.getExportManager().exportQuery(qr);
    }
}