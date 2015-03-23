package scheduler;

import com.google.common.collect.Lists;
import com.google.gson.*;
import export.Email;
import managers.QueryManager;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import query.Query;
import scheduler.timerule.*;

import java.lang.reflect.Type;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class JobInstance implements Comparable<JobInstance> {

    private static final Logger logger = LoggerFactory.getLogger(JobInstance.class);

    @Override
    public int compareTo(JobInstance o) {
        return executionTime.compareTo(o.executionTime);
    }

    public final DateTime executionTime;
    private final Job jobToExecute;

    private JobInstance(DateTime executionTime, Job jobToExecute) {
        this.executionTime = executionTime;
        this.jobToExecute = jobToExecute;
    }

    public void execute() throws Exception {
        logger.info("Executing " + this);
        QueryManager qm = QueryManager.getQueryManager();
        for (Query q : jobToExecute.queries) qm.executeQuery(q);
        Email.sendEmail(jobToExecute.queries, jobToExecute.emailTo, jobToExecute.subject, jobToExecute.message);
        logger.info("Emails sent, job complete");
    }

    @Override
    public String toString() {
        return executionTime + " - " + jobToExecute;
    }

    public static class Job {

        private final String jobName, subject, message;
        private final List<String> emailTo;
        private final List<Query> queries;
        private final ITimeRule timeRule;


        private Job(JobBuilder jb) {
            this.jobName = jb.jobName;
            this.subject = jb.subject;
            this.message = jb.message;
            this.emailTo = jb.emailTo;
            this.queries = jb.queries;
            this.timeRule = jb.timeRule;
        }

        public Queue<JobInstance> createJobInstances() {
            Queue<JobInstance> retQueue = new PriorityQueue<JobInstance>();
            Queue<DateTime> executionTimes = timeRule.getDateTimes();
            for (DateTime dt : executionTimes) retQueue.add(new JobInstance(dt, this));
            return retQueue;
        }

        @Override
        public String toString() {
            return jobName + " (send queries " + queries + " to addresses " + emailTo + ")";
        }

        public static class JobBuilder {
            private String jobName, subject, message;
            private List<String> emailTo;
            private List<Query> queries;
            private ITimeRule timeRule;

            public JobBuilder(String jobName) {
                this.jobName = jobName;

            }

            public JobBuilder withSubject(String subject) {
                this.subject = subject;
                return this;
            }

            public JobBuilder withMessage(String message) {
                this.message = message;
                return this;
            }

            public JobBuilder withEmailTo(List<String> emailTo) {
                this.emailTo = emailTo;
                return this;
            }

            public JobBuilder withQueries(List<Query> queries) {
                this.queries = queries;
                return this;
            }

            public JobBuilder withITimeRule(ITimeRule timeRule) {
                this.timeRule = timeRule;
                return this;
            }

            public Job build() {
                return new Job(this);
            }

        }

        public static class JobSerializer implements JsonDeserializer<Job> {

            @Override
            public Job deserialize(JsonElement jsonElement, Type type,
                                   JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

                JsonObject o = jsonElement.getAsJsonObject();

                String jobName = o.get("jobName").getAsString();
                JobBuilder jb = new JobBuilder(jobName);

                String subject = o.get("subject").getAsString();
                jb.withSubject(subject);

                String message = o.get("message").getAsString();
                jb.withMessage(message);

                JsonArray emailToArray = o.get("emailTo").getAsJsonArray();
                List<String> emailTo = Lists.newArrayList();
                for (JsonElement address : emailToArray) emailTo.add(address.getAsString());
                jb.withEmailTo(emailTo);

                try {
                    QueryManager qm = QueryManager.getQueryManager();

                    JsonArray queryArray = o.get("queries").getAsJsonArray();
                    List<Query> queries = Lists.newArrayList();
                    for (JsonElement query : queryArray) queries.add(qm.getQueryByName(query.getAsString()));
                    jb.withQueries(queries);

                } catch (Exception e) {
                    logger.error("Unable to create queries for job " + jobName + ": " + e.getMessage(), e);
                }

                JsonObject timeObject = o.get("scheduler").getAsJsonObject();
                jb.withITimeRule(parseTimeObject(timeObject));

                return jb.build();
            }

            private ITimeRule parseTimeObject(JsonObject o) {
                ITimeRule retRule;

                String runType = o.get("runType").getAsString();

                if (runType.equals("once")) {
                    String date = o.get("date").getAsString();
                    String time = o.get("time").getAsString();
                    retRule = new NoRepeat(date, time);
                } else {
                    String resolution = o.get("resolution").getAsString();

                    if (resolution.equals("daily")) {
                        String startingFrom = o.get("startingFrom").getAsString();
                        int every = o.get("every").getAsInt();
                        String until = o.get("until").getAsString();
                        String runAt = o.get("time").getAsString();

                        retRule = new RepeatsDaily(startingFrom, every, until, runAt);
                    } else if (resolution.equals("weekly")) {
                        int every = o.get("every").getAsInt();

                        JsonArray daysArray = o.getAsJsonArray("days");
                        List<String> days = Lists.newArrayList();
                        for (JsonElement day : daysArray) days.add(day.getAsString());

                        String runAt = o.get("time").getAsString();
                        String startingFrom = o.get("startingFrom").getAsString();
                        String until = o.get("until").getAsString();


                        retRule = new RepeatsWeekly(every, days, runAt, startingFrom, until);
                    } else { //resolution.equals("monthly")
                        int every = o.get("every").getAsInt();
                        int dayOfMonth = o.get("date").getAsInt();
                        String until = o.get("until").getAsString();
                        String runAt = o.get("time").getAsString();

                        retRule = new RepeatsMonthly(every, dayOfMonth, until, runAt);
                    }
                }
                return retRule;
            }
        }

    }
}