package webservice.controllers;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import export.Email;
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
import java.util.List;

@Controller
public class QueryController {
    private static final Logger logger = LoggerFactory.getLogger(QueryController.class);
    private final Gson gson;
    private QueryManager qm;

    public QueryController() {

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Query.class, new Query.QuerySerializer());
        gson = builder.create();
    }

    @RequestMapping(value = "/getAllQueries", method = RequestMethod.GET)
    public void getAllQueries(HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info("{} is requesting all queries", request.getRemoteAddr());

        if (qm == null) qm = QueryManager.getQueryManager();

        try {
            List<Query> allQueries = qm.getAllQueries();
            Type listAllQueries = new TypeToken<List<Query>>() {
            }.getType();
            response.getWriter().write(gson.toJson(allQueries, listAllQueries));
            logger.info("All queries served to {}", request.getRemoteAddr());
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    @RequestMapping(value = "/getAllQueryNames", method = RequestMethod.GET)
    public void getQueryNames(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("{} is requesting all query names", request.getRemoteAddr());

        try {
            List<String> allQueryNames = QueryManager.getQueryManager().getQueryNames();
            response.getWriter().write(gson.toJson(allQueryNames));
            logger.info("All query names served to {}", request.getRemoteAddr());
        } catch (Exception e) {
            logger.error("Error retrieving query names: {}", e.getMessage(), e);
            response.getWriter().write("error");
        }
    }

    @RequestMapping(value = "/getQuery", method = RequestMethod.GET)
    public void getQuery(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String queryName = request.getParameter("queryName");
        logger.info("{} is requesting query {}", request.getRemoteAddr(), queryName);

        if (qm == null) qm = QueryManager.getQueryManager();

        try {
            Query toReturn = qm.getQueryByName(queryName);
            String returnJSON = gson.toJson(toReturn);
            response.getWriter().write(returnJSON);
            logger.info("Sent query {} to {}", toReturn, request.getRemoteAddr());
        } catch (Exception e) {
            logger.error("ERROR when trying to get query: {}", e.getMessage());
            response.getWriter().write("{\"result\":false");
            logger.info("Returned failure message to {}", request.getRemoteAddr());
        }
    }

    @RequestMapping(value = "/executeQuery", method = RequestMethod.POST)
    public void executeQuery(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String queryName = request.getParameter("queryName");
        logger.info("{} is attempting to execute query {}", request.getRemoteAddr(), queryName);

        try {
            if (qm == null) qm = QueryManager.getQueryManager();
            qm.executeQuery(queryName);

            String emailAddress = request.getParameter("emailAddress");

            if (!(emailAddress.equals("N/A"))) {
                Query q = qm.getQueryByName(queryName);
                List<Query> queries = Lists.newArrayList();
                queries.add(q);

                List<String> addresses = Lists.newArrayList();
                addresses.add(emailAddress);

                Email.getEmail().sendEmail(queries, addresses, "Report for " + q.name,
                        "This is an automated message, do not respond");

                logger.info("Email sent");
            }

            response.getWriter().write("{\"result\":true, \"queryName\":\"" + queryName + "\"}");
            logger.info("Returned success message to {}", request.getRemoteAddr());
        } catch (Exception e) {
            logger.warn("Error hit when executing query {}: {}", queryName, e.getMessage(), e);
            response.getWriter().write("{\"result\":\"false\", \"queryName\":\"" + queryName + "\"}");
            logger.info("Returned failure message to {}", request.getRemoteAddr());
        }
    }

    @RequestMapping(value = "/saveQuery", method = RequestMethod.POST)
    public void saveQuery(HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info("{} is attempting to save a query", request.getRemoteAddr());

        if (qm == null) qm = QueryManager.getQueryManager();
        String queryJSON = request.getParameter("query");
        Query newQuery = gson.fromJson(queryJSON, Query.class);
        qm.saveQuery(newQuery);
        response.getWriter().write("saved");
        logger.info("Query successfully saved, and {} has been informed", request.getRemoteAddr());
    }

    @RequestMapping(value = "/removeQuery", method = RequestMethod.POST)
    public void removeQuery(HttpServletRequest request, HttpServletResponse response) {
        String queryName = request.getParameter("queryName");
        logger.info("{} is attempting to remove query {}", request.getRemoteAddr(), queryName);

        try {
            if (qm == null) qm = QueryManager.getQueryManager();
            qm.removeQuery(queryName);
            response.getWriter().write("removed");
            logger.info("Completed removal of query {}", queryName);
        } catch (Exception e) {
            logger.info("Unable to remove query: {}", e.getMessage(), e);
        }
    }
}