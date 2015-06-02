function validateAndSaveJob() {
    var jobName = $("#jobNameTextBox").val();
    var selectedQueries = $("#querySelectBox").val();

    var emailTo = $("#emailToTextBox").val();
    var cc = $("#ccTextBox").val();
    var bcc = $("#bccTextBox").val();
    var subject = $("#subjectTextBox").val();
    var message = $("#messageTextArea").val();

    if (jobName.trim() === "") {
        alert("Enter a job name");
        return;
    }

    if (selectedQueries === null || selectedQueries.length == 0) {
        alert("Select at least one query");
        return;
    }

    if (emailTo.trim() === "") {
        alert("Email to box is empty");
        return;
    }

    if (subject.trim() === "") {
        alert("Subject is empty");
        return;
    }

    if (message.trim() === "") {
        alert("Message is empty");
        return;
    }

    var scheduleType = $("input[name=scheduleRuleRadio]:checked").attr("id");

    var scheduler = {};
    if (scheduleType === "radioOnce") {
        scheduler.runType = "once";

        var date = $("#onceDateTextBox").val();
        var time = $("#onceTimeTextBox").val();

        if (date.trim() === "") {
            alert("Enter an execution date");
            return;
        }

        if (time.trim() === "") {
            alert("Enter an execution time");
            return;
        }

        scheduler.date = date;
        scheduler.time = time;
    } else if (scheduleType === "radioDaily") {
        scheduler.runType = "repeats";
        scheduler.resolution = "daily";

        var every = $("#dailyEveryTextBox").val();
        var startingFrom = $("#dailyStartTextBox").val();
        var until = $("#dailyEndTextBox").val();
        var time = $("#dailyTimeTextBox").val();

        if (every.trim() === "") {
            alert("Every textbox is empty");
            return;
        }

        if (startingFrom.trim() === "") {
            alert("Starting from textbox is empty");
            return;
        }

        if (until.trim() === "") {
            alert("Until textbox is empty");
            return;
        }

        if (time.trim() === "") {
            alert("Time text box is empty");
            return;
        }

        scheduler.every = every;
        scheduler.startingFrom = startingFrom;
        scheduler.time = time;
        scheduler.until = until;
    } else if (scheduleType === "radioWeekly") {
        scheduler.runType = "repeats";
        scheduler.resolution = "weekly";

        var every = $("#weeklyEveryTextBox").val();
        var days = $("#weeklyDays").val();
        var startingFrom = $("#weeklyStartTextBox").val();
        var until = $("#weeklyEndTextBox").val();
        var time = $("#weeklyTimeTextBox").val();

        if (every.trim() === "") {
            alert("Every text box is empty");
            return;
        }

        if (days == null || days.length == 0) {
            alert("Select at least one day to run this job on");
            return;
        }

        if (startingFrom.trim() === "") {
            alert("Starting from is empty");
            return;
        }

        if (until.trim() === "") {
            alert("Until is empty");
            return;
        }

        if (time.trim() === "") {
            alert("Time text box is empty");
            return;
        }

        scheduler.every = every;
        scheduler.days = days;
        scheduler.startingFrom = startingFrom;
        scheduler.until = until;
        scheduler.time = time;
    } else if (scheduleType === "radioMonthly") {
        scheduler.runType = "repeats";
        scheduler.resolution = "monthly";

        var every = $("#monthlyEveryTextBox").val();

        if (every.trim() === "") {
            alert("Every is empty");
            return;
        }

        scheduler.every = every;

        var monthlyType = $("input[name=radioMonthlyOption]:checked").attr("id");

        if (monthlyType === "radioMonthlyDate") {
            var date = $("#monthlyDateTextBox").val();

            if (date.trim() === "") {
                alert("date text box empty");
                return;
            }

            scheduler.option = "date";
            scheduler.date = date;
        } else if (monthlyType === "radioMonthlyDay") {
            scheduler.option = "day";

            scheduler.one = $("#monthlyDaySelectOne").val();
            scheduler.two = $("#monthlyDaySelectTwo").val();
        }

        var until = $("#monthlyEndTextBox").val();
        var time = $("#monthlyTimeTextBox").val();

        if (until.trim() === "") {
            alert("Until box empty");
            return;
        }

        if (time.trim() === "") {
            alert("time box empty");
            return;
        }

        scheduler.until = until;
        scheduler.time = time;
    }

    var job = {};
    job.jobName = jobName;
    job.queries = selectedQueries;
    job.selectedQueries = selectedQueries;
    job.emailTo = emailTo.split(",");
    job.cc = cc.split(",");
    job.bcc = bcc.split(",");
    job.subject = subject;
    job.message = message;
    job.scheduler = scheduler;

    var r = confirm("Are you sure you want to save " + job.jobName + "?");

    if (r) {
        $.ajax({
            type : "POST",
            url : "/saveJob",
            data : {
                "toBeSaved" : JSON.stringify(job)
            }
        }).done(function(response){
            if (response === "saved") {
                alert("Job save completed successfully");
                hideDialog();
            }

            if (response === "error") {
                alert("Error occurred saving job");
            }
        });
    }
}