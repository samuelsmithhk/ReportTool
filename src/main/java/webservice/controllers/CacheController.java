package webservice.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import managers.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
public class CacheController {

    private final Logger logger = LoggerFactory.getLogger(CacheController.class);

    private final Gson gson;

    private CacheManager cm;

    public CacheController() {
        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();
    }


    @RequestMapping(value = "/getAllColumns", method = RequestMethod.GET)
    public void getAllColumns(HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info("{} is requesting all columns", request.getRemoteAddr());

        if (cm == null) cm = CacheManager.getCacheManager();

        try {
            List<String> columns = cm.getAllColumns();
            response.getWriter().write(gson.toJson(columns));
            logger.info("Sent list of columns to {}", request.getRemoteAddr());
        } catch (IOException e) {
            logger.error("Error sending columns to {}: {}", request.getRemoteAddr(), e.getMessage(), e);
        }
    }

}
