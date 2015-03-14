package webservice.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import managers.QueryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import query.Query;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.InvalidKeyException;
import java.util.List;

@Controller
public class QueryController {
    private final Logger logger = LoggerFactory.getLogger(QueryController.class);

    private QueryManager qm;
    private final Gson gson;

    public QueryController() {

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Query.class, new Query.QuerySerializer());
        gson = builder.create();
    }

    @RequestMapping(value = "/getAllQueries", method = RequestMethod.GET)
    public void getAllQueries(HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info(request.getRemoteAddr() + " is requesting all queries");

        if (qm == null) qm = QueryManager.getQueryManager();

        try {
            List<Query> allQueries = qm.getAllQueries();
            Type listAllQueries = new TypeToken<List<Query>>(){}.getType();
            response.getWriter().write(gson.toJson(allQueries, listAllQueries));
            logger.info("All queries served to " + request.getRemoteAddr());
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    @RequestMapping(value = "/executeQuery", method = RequestMethod.POST)
    public void executeQuery(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String queryName = request.getParameter("queryName");
        logger.info(request.getRemoteAddr() + " is attempting to execute query " + queryName);

        if (qm == null) qm = QueryManager.getQueryManager();
        try {
            qm.executeQuery(queryName);

            response.getWriter().write("{\"result\":true, \"queryName\":\"" + queryName + "\"}");
            logger.info("Returned success message to " + request.getRemoteAddr());
        } catch (InvalidKeyException e) {
            logger.warn("Error hit when executing query " + queryName + ": " + e.getMessage(), e);
            response.getWriter().write("{\"result\":false, \"queryName\":\"" + queryName + "\"}");
            logger.info("Returned failure message to " + request.getRemoteAddr());
        }
    }

    @RequestMapping(value = "/saveQuery", method = RequestMethod.POST)
    public void saveQuery(HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info(request.getRemoteAddr() + " is attempting to save a query");

        if (qm == null) qm = QueryManager.getQueryManager();
        String queryJSON = request.getParameter("query");
        logger.info(queryJSON);
    }
}