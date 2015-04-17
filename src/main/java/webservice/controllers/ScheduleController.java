package webservice.controllers;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import managers.ScheduleManager;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import scheduler.JobInstance;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

@Controller
public class ScheduleController {

    private final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    private final Gson gson;

    public ScheduleController() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(JobInstance.class, new JobInstance.JobInstanceSerializer());
        builder.registerTypeAdapter(JobInstance.Job.class, new JobInstance.Job.JobSerializer());
        gson = builder.create();
    }

    @RequestMapping(value = "/getJobsForDate", method = RequestMethod.GET)
    public void getJobsForDate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String dateString = request.getParameter("date");
        logger.info(request.getRemoteAddr() + " is requesting jobs for date " + dateString);

        DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy");
        LocalDate date = dtf.parseLocalDate(dateString);

        List<JobInstance> jobsForDate = ScheduleManager.getScheduleManager().getJobsForDate(date);
        Type listAllJobs = new TypeToken<List<JobInstance>>(){}.getType();

        response.getWriter().write(gson.toJson(jobsForDate, listAllJobs));
        logger.info("Jobs for date " + dateString + " served to " + request.getRemoteAddr());
    }

    @RequestMapping(value = "/getJobByName", method = RequestMethod.GET)
    public void getJobByName(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String jobName = request.getParameter("jobName");
        logger.info(request.getRemoteAddr() + " is requesting job by name " + jobName);

        try {
            JobInstance.Job job = ScheduleManager.getScheduleManager().getJobByName(jobName);
            if (job != null) {
                List<DateTime> jobExecutionTimes = job.getExecutionTimes();
                Collections.sort(jobExecutionTimes);

                String jobJson = "{\"job\":" + gson.toJson(job) + "}";
                String dtJson = "{\"executionTimes\":" + gson.toJson(jobExecutionTimes) + "}";
                String json = "{" + jobJson + ", " + dtJson + "}";
                response.getWriter().write(json);
            }

        } catch (Exception e) {
            logger.error("Error getting job by name for schedule: " + e.getMessage(), e);
            response.getWriter().write("\"error\"");
        }
    }

}
