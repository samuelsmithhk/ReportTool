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
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class JobInstance implements Comparable<JobInstance> {

    private static final Logger logger = LoggerFactory.getLogger(JobInstance.class);
    public final DateTime executionTime;
    private final Job jobToExecute;
    private JobInstance(DateTime executionTime, Job jobToExecute) {
        this.executionTime = executionTime;
        this.jobToExecute = jobToExecute;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(JobInstance o) {
        return executionTime.compareTo(o.executionTime);
    }

    public void execute() throws Exception {
        logger.info("Executing {}", this);
        QueryManager qm = QueryManager.getQueryManager();
        for (Query q : jobToExecute.queries) qm.executeQuery(q);
        Email.getEmail().sendEmail(jobToExecute.queries, jobToExecute.emailTo, jobToExecute.subject,
                jobToExecute.message);
        logger.info("Emails sent, job complete");
    }

    @Override
    public String toString() {
        return executionTime + " - " + jobToExecute;
    }

    public static class JobInstanceSerializer implements JsonSerializer<JobInstance> {

        @Override
        public JsonElement serialize(JobInstance jobInstance, Type type, JsonSerializationContext jsonSerializationContext) {
            StringBuilder sb = new StringBuilder("{");
            Job job = jobInstance.jobToExecute;

            sb.append("\"executionTime\":\"").append(jobInstance.executionTime.toString())
                    .append("\",\"job\":{\"jobName\":\"").append(job.jobName).append("\",\"subject\":\"")
                    .append(job.subject).append("\",\"message\":\"").append(job.message).append("\",\"emailTo\":[");

            for (String address : job.emailTo) sb.append("\"").append(address).append("\",");
            sb.deleteCharAt(sb.lastIndexOf(","));

            sb.append("],\"queries\":[");

            for (Query query : job.queries) sb.append("\"").append(query.name).append("\",");
            sb.deleteCharAt(sb.lastIndexOf(","));

            sb.append("],\"timeRule\":{");

            ITimeRule timeRule = job.timeRule;
            String timeRuleType = timeRule.getType();
            sb.append("\"type\":\"").append(timeRuleType).append("\",");

            switch (timeRuleType) {
                case "NoRepeat":
                    NoRepeat noRepeat = (NoRepeat) timeRule;

                    sb.append("\"executionDate\":\"").append(noRepeat.getExecutionDate()).append("\",\"executionTime\":\"")
                            .append(noRepeat.getExecutionTime()).append("\"");
                    break;
                case "RepeatsDaily":
                    RepeatsDaily repeatsDaily = (RepeatsDaily) timeRule;

                    sb.append("\"every\":\"").append(repeatsDaily.getEvery()).append("\",\"startingFrom\":")
                            .append(repeatsDaily.getStartingFrom()).append("\",\"until\":\"")
                            .append(repeatsDaily.getUntil()).append("\",\"executionTime\":\"")
                            .append(repeatsDaily.getExecutionTime()).append("\"");

                    break;
                case "RepeatsMonthlyDate":
                    RepeatsMonthlyDate repeatsMonthlyDate = (RepeatsMonthlyDate) timeRule;

                    sb.append("\"option\":\"date\",").append("\"every\":\"").append(repeatsMonthlyDate.getEvery())
                            .append("\",\"dayOfMonth\":\"").append(repeatsMonthlyDate.getDayOfMonth())
                            .append("\",\"until\":\"").append(repeatsMonthlyDate.getUntil())
                            .append("\",\"executionTime\":\"").append(repeatsMonthlyDate.getExecutionTime()).append("\"");

                    break;
                case "RepeatsMonthlyDay":
                    RepeatsMonthlyDay repeatsMonthlyDay = (RepeatsMonthlyDay) timeRule;

                    sb.append("\"option\":\"day\",").append("\"every\":\"").append(repeatsMonthlyDay.getEvery())
                            .append("\",\"one\":\"").append(repeatsMonthlyDay.getParam1()).append("\",\"two\":\"")
                            .append(repeatsMonthlyDay.getParam2()).append("\",\"until\":\"")
                            .append(repeatsMonthlyDay.getUntil()).append("\",\"executionTime\":\"")
                            .append(repeatsMonthlyDay.getExecutionTime()).append("\"");

                    break;
                case "RepeatsWeekly":
                    RepeatsWeekly repeatsWeekly = (RepeatsWeekly) timeRule;

                    sb.append("\"every\":\"").append(repeatsWeekly.getEvery()).append("\",\"days\":[");

                    for (AbstractTimeRule.DAY day : repeatsWeekly.getDays()) sb.append("\"").append(day).append("\",");
                    sb.deleteCharAt(sb.lastIndexOf(","));
                    sb.append("],\"executionTime\":\"").append(repeatsWeekly.getExecutionTime())
                            .append("\",\"startingFrom\":\"").append(repeatsWeekly.getStartingFrom())
                            .append("\",\"until\":\"").append(repeatsWeekly.getUntil()).append("\"");
                    break;
            }

            sb.append("}");
            sb.append("}");
            sb.append("}");

            JsonParser parser = new JsonParser();
            return parser.parse(sb.toString());
        }
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
            Queue<JobInstance> retQueue = new PriorityQueue<>();
            Queue<DateTime> executionTimes = timeRule.getDateTimes();
            for (DateTime dt : executionTimes) retQueue.add(new JobInstance(dt, this));
            return retQueue;
        }

        @Override
        public String toString() {
            return jobName + " (send queries " + queries + " to addresses " + emailTo + ")";
        }

        public List<DateTime> getExecutionTimes() {
            return new ArrayList<>(timeRule.getDateTimes());
        }

        public String getName() {
            return jobName;
        }

        public void addExclusion(String instance) {
            timeRule.addExclusion(instance);
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

        public static class JobSerializer implements JsonDeserializer<Job>, JsonSerializer<Job> {

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
                    logger.error("Unable to create queries for job {}: {}", jobName, e.getMessage(), e);
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

                    switch (resolution) {
                        case "daily": {
                            String startingFrom = o.get("startingFrom").getAsString();
                            int every = o.get("every").getAsInt();
                            String until = o.get("until").getAsString();
                            String runAt = o.get("time").getAsString();

                            retRule = new RepeatsDaily(startingFrom, every, until, runAt);
                            break;
                        }
                        case "weekly": {
                            int every = o.get("every").getAsInt();

                            JsonArray daysArray = o.getAsJsonArray("days");
                            List<String> days = Lists.newArrayList();
                            for (JsonElement day : daysArray) days.add(day.getAsString());

                            String runAt = o.get("time").getAsString();
                            String startingFrom = o.get("startingFrom").getAsString();
                            String until = o.get("until").getAsString();


                            retRule = new RepeatsWeekly(every, days, runAt, startingFrom, until);
                            break;
                        }
                        default:  //resolution.equals("monthly")
                            String option = o.get("option").getAsString();

                            if (option.equals("date")) {
                                int every = o.get("every").getAsInt();
                                int dayOfMonth = o.get("date").getAsInt();
                                String until = o.get("until").getAsString();
                                String runAt = o.get("time").getAsString();

                                retRule = new RepeatsMonthlyDate(every, dayOfMonth, until, runAt);
                            } else { //monthly day
                                int every = o.get("every").getAsInt();
                                String one = o.get("one").getAsString();
                                String two = o.get("two").getAsString();
                                String until = o.get("until").getAsString();
                                String runAt = o.get("time").getAsString();

                                retRule = new RepeatsMonthlyDay(every, one, two, until, runAt);
                            }
                            break;
                    }
                }

                JsonArray excludeJson = o.getAsJsonArray("exclude");
                if (excludeJson != null)
                    for (JsonElement e : excludeJson) retRule.addExclusion(e.getAsString());


                return retRule;
            }

            @Override
            public JsonElement serialize(Job job, Type type, JsonSerializationContext jsonSerializationContext) {
                StringBuilder sb = new StringBuilder("{\"jobName\":\"");

                sb.append(job.jobName).append("\",\"emailTo\":[");

                for (String s : job.emailTo) sb.append("\"").append(s).append("\",");
                sb.deleteCharAt(sb.lastIndexOf(",")).append("],\"queries\":[");

                for (Query q : job.queries) sb.append("\"").append(q.name).append("\",");
                sb.deleteCharAt(sb.lastIndexOf(",")).append("],\"subject\":\"").append(job.subject)
                        .append("\",\"message\":\"").append(job.message).append("\",\"scheduler\":{");

                if (job.timeRule instanceof NoRepeat) {
                    NoRepeat noRepeat = (NoRepeat) job.timeRule;
                    String date = noRepeat.getExecutionDate().toString("yyyyMMdd");
                    String time = noRepeat.getExecutionTime().toString("HHmm");

                    sb.append("\"runType\":\"once\",\"date\":\"").append(date).append("\",\"time\":\"").append(time)
                            .append("\"");
                } else if (job.timeRule instanceof RepeatsDaily) {
                    RepeatsDaily repeatsDaily = (RepeatsDaily) job.timeRule;

                    String startsFrom = repeatsDaily.getStartingFrom().toString("yyyyMMdd");
                    String every = String.valueOf(repeatsDaily.getEvery());
                    String until = repeatsDaily.getUntil().toString("yyyyMMdd");
                    String runAt = repeatsDaily.getExecutionTime().toString("HHmm");

                    sb.append("\"runType\":\"repeats\",\"resolution\":\"daily\",\"startingFrom\":\"")
                            .append(startsFrom).append("\",\"every\":").append(every).append(",\"until\":\"")
                            .append(until).append("\",\"time\":\"").append(runAt).append("\"");
                } else if (job.timeRule instanceof RepeatsWeekly) {
                    RepeatsWeekly repeatsWeekly = (RepeatsWeekly) job.timeRule;

                    String every = String.valueOf(repeatsWeekly.getEvery());
                    String time = repeatsWeekly.getExecutionTime().toString("HHmm");
                    String startingFrom = repeatsWeekly.getStartingFrom().toString("yyyyMMdd");
                    String until = repeatsWeekly.getUntil().toString("yyyyMMdd");

                    sb.append("\"runType\":\"repeats\",\"resolution\":\"weekly\",\"every\":").append(every)
                            .append(",\"days\":").append(repeatsWeekly.getDays()).append(",\"time\":\"").append(time)
                            .append("\",\"startingFrom\":\"").append(startingFrom).append("\",\"until\":\"")
                            .append(until).append("\"");
                } else if (job.timeRule instanceof RepeatsMonthlyDate) {
                    RepeatsMonthlyDate repeatsMonthlyDate = (RepeatsMonthlyDate) job.timeRule;

                    String every = String.valueOf(repeatsMonthlyDate.getEvery());
                    String date = String.valueOf(repeatsMonthlyDate.getDayOfMonth());
                    String time = repeatsMonthlyDate.getExecutionTime().toString("HHmm");
                    String until = repeatsMonthlyDate.getUntil().toString("yyyyMMdd");

                    sb.append("\"runType\":\"repeats\",\"resolution\":\"monthly\",\"option\":\"date\",\"every\":")
                            .append(every).append(",\"date\":").append(date).append(",\"time\":\"").append(time)
                            .append("\",\"until\":\"").append(until).append("\"");
                } else if (job.timeRule instanceof RepeatsMonthlyDay) {
                    RepeatsMonthlyDay repeatsMonthlyDay = (RepeatsMonthlyDay) job.timeRule;

                    String every = String.valueOf(repeatsMonthlyDay.getEvery());
                    String one = repeatsMonthlyDay.getParam1();
                    String two = repeatsMonthlyDay.getParam2().name();
                    String time = repeatsMonthlyDay.getExecutionTime().toString("HHmm");
                    String until = repeatsMonthlyDay.getUntil().toString("yyyyMMdd");

                    sb.append("\"runType\":\"repeats\",\"resolution\":\"monthly\",\"option\":\"day\",\"every\":")
                            .append(every).append(",\"one\":\"").append(one).append("\",\"two\":\"").append(two)
                            .append("\",\"time\":\"").append(time).append("\",\"until\":\"").append(until).append("\"");
                }

                if (job.timeRule.getExclusions().size() != 0) {
                    sb.append(",\"exclude\":[");

                    for (DateTime dt : job.timeRule.getExclusions())
                        sb.append("\"").append(dt.toString("yyyy-MM-dd HH:mm")).append("\",");

                    sb.deleteCharAt(sb.lastIndexOf(",")).append("]");
                }

                sb.append("}}");

                JsonParser parser = new JsonParser();
                return parser.parse(sb.toString());
            }
        }

    }
}