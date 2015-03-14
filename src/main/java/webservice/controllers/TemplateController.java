package webservice.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import managers.TemplateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class TemplateController {

    private final Logger logger = LoggerFactory.getLogger(TemplateController.class);

    private TemplateManager tm;
    private final Gson gson;

    public TemplateController() {
        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();
    }

    @RequestMapping(value = "/getAllTemplates", method= RequestMethod.GET)
    public void getAllTemplates(HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info(request.getRemoteAddr() + " is requesting all templates");

        if (tm == null) tm = TemplateManager.getTemplateManager();

        List<String> templateList = tm.getTemplateList();
        response.getWriter().write(gson.toJson(templateList));
        logger.info("All templates served to " + request.getRemoteAddr());

    }
}
