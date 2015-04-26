package webservice.controllers;

import managers.CacheManager;
import managers.QueryManager;
import managers.ScheduleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    @RequestMapping(value = "/reloadCache", method = RequestMethod.GET)
    public void reloadCache(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("{} is attempting to create a new cache from current inputs", request.getRemoteAddr());

        try {
            CacheManager.getCacheManager().createNewCache();
            logger.info("New Cache successfully created");
            response.getWriter().write("\"success\"");
        } catch (Exception e) {
            logger.error("Exception occurred trying to recreate cache: {}", e.getMessage(), e);
            logger.info("Informing user {} that an error has occurred", request.getRemoteAddr());
            response.getWriter().write("\"error\"");
        }
    }

    @RequestMapping(value = "/reloadQueries", method = RequestMethod.GET)
    public void reloadQueries(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("{} is attempting to reload the queries from the filesystem", request.getRemoteAddr());

        try {
            QueryManager.getQueryManager().forceQueryReload();
            logger.info("Queries successfully reloaded");
            response.getWriter().write("\"success\"");
        } catch (Exception e) {
            logger.error("Exception occurred trying to reload queries: {}", e.getMessage(), e);
            logger.info("Informing user {} that an error has occurred", request.getRemoteAddr());
            response.getWriter().write("\"error\"");
        }
    }

    @RequestMapping(value = "/reloadSchedule", method = RequestMethod.GET)
    public void reloadSchedule(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("{} is attempting to reload the schedule files from the filesystem", request.getRemoteAddr());

        try {
            ScheduleManager sm = ScheduleManager.getScheduleManager();
            logger.info("Shutting down scheduler");
            sm.shutdownSchedule();
            logger.info("Loading and starting new schedule");
            sm.startSchedule();
            logger.info("Successfully reloaded schedule");
            response.getWriter().write("\"success\"");
        } catch (Exception e) {
            logger.error("Exception trying to reload the schedule: {}", e.getMessage(), e);
            logger.info("Informing user {} that an error has occurred", request.getRemoteAddr());
            response.getWriter().write("\"error\"");
        }
    }

    @RequestMapping(value = "/terminateService", method = RequestMethod.GET)
    public void terminateService(HttpServletRequest request) {
        logger.info("User {} is attempting to terminate me. Goodbye world, I'll miss you :'(.",
                request.getRemoteAddr());

        System.exit(0);
    }

}
