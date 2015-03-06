package webservice.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import managers.QueryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import query.Query;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

@Controller
public class QueryController {

    private QueryManager qm;
    private final Gson gson;

    public QueryController() {

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Query.class, new Query.QuerySerializer());
        gson = builder.create();
    }

    Logger logger = LoggerFactory.getLogger(QueryController.class);

    @RequestMapping(value = "/getAllQueries")
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

    @RequestMapping(value = "/executeQuery")
    public void executeQuery(HttpServletRequest request, HttpServletResponse response) {
        logger.info(request.getRemoteAddr() + " is attempting to execute a query");

    }
}