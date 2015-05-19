package managers;

import com.google.common.collect.Lists;
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
    private final QueryFileManager qfm;
    private volatile Map<String, Query> currentQueries;

    private QueryManager(QueryFileManager qfm) {
        this.qfm = qfm;
    }

    public static void initQueryManager(QueryFileManager qfm) {
        if (qm == null) qm = new QueryManager(qfm);
    }

    public static QueryManager getQueryManager() throws Exception {
        if (qm == null) throw new Exception("QueryManager needs to be instantiated with instance of QueryFileManager");
        return qm;
    }

    public synchronized List<Query> getAllQueries() {
        if (qfm.hasUpdate()) currentQueries = qfm.loadQueries();
        return new ArrayList<>(currentQueries.values());
    }

    public synchronized void forceQueryReload() {
        currentQueries = qfm.loadQueries();
    }

    public synchronized List<String> getQueryNames() {
        if (qfm.hasUpdate()) currentQueries = qfm.loadQueries();

        List<String> retList = Lists.newArrayList();
        for (Query q : currentQueries.values()) retList.add(q.name);
        return retList;
    }

    public synchronized void executeQuery(String queryName) throws Exception {
        if (!currentQueries.containsKey(queryName)) throw new InvalidKeyException("Query"
                + queryName + " does not exist");

        Query toExecute = currentQueries.get(queryName);
        executeQuery(toExecute);
    }

    public synchronized void executeQuery(Query q) throws Exception {
        InputManager im = InputManager.getInputManager();
        im.loadNewInputsIfAny();

        CacheManager cm = CacheManager.getCacheManager();
        cm.purgeOldData();

        QueryResult qr = QueryExecutor.executeQuery(q);
        ExportManager em = ExportManager.getExportManager();
        em.exportQuery(qr);

        if (q.hasTemplate) em.runMacroOnQuery(q);
    }

    public Query getQueryByName(String queryName) throws Exception {
        if (qfm.hasUpdate()) currentQueries = qfm.loadQueries();
        if (!(currentQueries.containsKey(queryName))) throw new Exception("Query " + queryName + " does not exist");
        return currentQueries.get(queryName);
    }

    public synchronized void saveQuery(Query newQuery) {
        qfm.saveQuery(newQuery);
    }

    public synchronized void removeQuery(String queryName) {
        qfm.removeQuery(queryName);
    }
}