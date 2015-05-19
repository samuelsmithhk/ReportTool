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

    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

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
        logger.info("{} is requesting jobs for date {}", request.getRemoteAddr(), dateString);

        DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy");
        LocalDate date = dtf.parseLocalDate(dateString);

        List<JobInstance> jobsForDate = ScheduleManager.getScheduleManager().getJobsForDate(date);
        Type listAllJobs = new TypeToken<List<JobInstance>>() {
        }.getType();

        response.getWriter().write(gson.toJson(jobsForDate, listAllJobs));
        logger.info("Jobs for date {} served to {}", dateString, request.getRemoteAddr());
    }

    @RequestMapping(value = "/getJobByName", method = RequestMethod.GET)
    public void getJobByName(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String jobName = request.getParameter("jobName");
        logger.info("{} is requesting job by name {}", request.getRemoteAddr(), jobName);

        try {
            JobInstance.Job job = ScheduleManager.getScheduleManager().getJobByName(jobName);
            if (job != null) {
                List<DateTime> jobExecutionTimes = job.getExecutionTimes();
                Collections.sort(jobExecutionTimes);

                StringBuilder sb = new StringBuilder("[");
                for (DateTime dt : jobExecutionTimes)
                    sb.append("\"").append(dt.toString("yyyy-MM-dd hh:mm")).append("\",");
                int commaIndex = sb.lastIndexOf(",");

                if (commaIndex != -1) sb.deleteCharAt(commaIndex);
                sb.append("]");

                String jobJson = "\"job\":" + gson.toJson(job);
                String dtJson = "\"executionTimes\":" + sb.toString();
                String json = "{" + jobJson + ", " + dtJson + "}";
                response.getWriter().write(json);
            }

        } catch (Exception e) {
            logger.error("Error getting job by name for schedule: {}", e.getMessage(), e);
            response.getWriter().write("\"error\"");
        }
    }

    @RequestMapping(value = "removeJobInstance", method = RequestMethod.POST)
    public void removeJobInstance(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String jobName = request.getParameter("jobName");
        String instance = request.getParameter("instance");
        logger.info("{} is trying to remove instance {} of job {}", request.getRemoteAddr(), instance, jobName);

        try {
            ScheduleManager.getScheduleManager().removeJobInstance(jobName, instance);
            getJobByName(request, response);
            logger.info("Successfully completed removing job instance for {}", request.getRemoteAddr());
        } catch (Exception e) {
            logger.error("Error removing job instance: {}", e.getMessage(), e);
            response.getWriter().write("error");
        }
    }

    @RequestMapping(value = "removeJob", method = RequestMethod.POST)
    public void removeJob(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String jobName = request.getParameter("jobName");
        logger.info("{} is trying to remove job {}", request.getRemoteAddr(), jobName);

        try {
            ScheduleManager.getScheduleManager().removeJob(jobName);
            response.getWriter().write("success");
            logger.info("Successfully removed job  {}", jobName);
        } catch (Exception e) {
            logger.error("Error removing job: {}", e.getMessage(), e);
            response.getWriter().write("error");
        }
    }

    @RequestMapping(value = "saveJob", method = RequestMethod.POST)
    public void saveJob(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("{} is trying to save a job", request.getRemoteUser());

        try {
            JobInstance.Job job = gson.fromJson(request.getParameter("toBeSaved"), JobInstance.Job.class);
            ScheduleManager.getScheduleManager().saveJob(job);
            response.getWriter().write("saved");
            logger.info("Successfully saved job {}", job.getName());
        } catch (Exception e) {
            logger.error("Exception saving job: {}", e.getMessage(), e);
            response.getWriter().write("error");
        }
    }
}
