package webservice.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by samuelsmith on 05/03/15.
 */

@Controller
public class HomeController {

    @RequestMapping("/index")
    public String home(){
        return "html/index.html";
    }
}
