function createJobView(dateText, response) {
    if (response === "error") {
        alert("An error occurred");
    }

    $("#dateText").html(dateText);

    var job = response.job;
    var times = response.executionTimes;

    $("#jobNameLabel").html(job.jobName);

    var timesHtml = "<table><tr><th>Execution Time</th><th>Remove</th></tr>";

    var odd = true;
    $.each(times, function(index, time){
        if (odd) {
            timesHtml += '<tr class="odd">'
            odd = false;
        } else {
            timesHtml += "<tr>"
            odd = true;
        }

        timesHtml += "<td>" + time + '</td><td><button id="removeInstance-' + time
            + '-button" class="removeInstanceButton">Remove</button></td></tr>';
    });

    timesHtml += "</table>";

    $("#jobExecutionTimes").html(timesHtml);

    $(".removeInstanceButton").button().click(function(){
        var instance = $(this).attr("id");
        instance = instance.substring(instance.indexOf("-"));
        instance = instance.substring(1, instance.indexOf("-button"));
        var r = confirm("Are you sure you want to remove this instance - " + instance);

        if (r) {
            $.ajax({
                type : "POST",
                url : "/removeJobInstance",
                data : {
                    "jobName" : job.jobName,
                    "instance" :  instance
                }
            }).done(function(newResponse){
                createJobView(dateText, JSON.parse(newResponse));
            });
        }
    });

    displayViewJobWindow();
}

function clearJobEditorWindow() {
    $("#jobNameTextBox").val("");
    $("#subjectTextBox").val("");
    $("#messageTextArea").val("");
    $("#emailToTextBox").val("");
    $("#ccTextBox").val("");
    $("#bccTextBox").val("");
    $("#onceDateTextBox").val("");
    $("#onceTimeTextBox").val("");
    $("#weeklyEveryTextBox").val("");
    $("#weeklyTimeTextBox").val("");
    $("#weeklyStartTextBox").val("");
    $("#weeklyEndTextBox").val("");
    $("#dailyStartTextBox").val("");
    $("#dailyEndTextBox").val("");
    $("#dailyTimeTextBox").val("");
    $("#dailyEveryTextBox").val("");
    $("#monthlyEveryTextBox").val("");
    $("#monthlyEndTextBox").val("");
    $("#monthlyTimeTextBox").val("");
    $("#monthlyDateTextBox").val("");

    $("#radioOnce").prop("checked", true);
    $("#radioMonthlyDate").prop("checked", true);

    $("#dailyOptions").addClass("hidden");
    $("#weeklyOptions").addClass("hidden");
    $("#monthlyOptions").addClass("hidden");
    $("#onceOptions").removeClass("hidden");

    $("#monthlyDate").removeClass("hidden");
    $("#monthlyDay").addClass("hidden");

    $("#weeklyDays").val([]);
    $("#querySelectBox").val([]);
}

function createJobEditorWindow(jobAndTimes) {
    var job = jobAndTimes.job;

    hideDialog();
    clearJobEditorWindow();
    displayNewJobWindow();

    requestQueryNames(job.queries);

    $("#jobNameTextBox").val(job.jobName);
    $("#subjectTextBox").val(job.subject);
    $("#messageTextArea").val(job.message);
    $("#emailToTextBox").val(job.emailTo);
    $("#ccTextBox").val(job.cc);
    $("#bccTextBox").val(job.bcc);

    var schedule = job.scheduler;

    if (schedule.runType === "once") {

        $("#radioOnce").prop("checked", true);

        $("#dailyOptions").addClass("hidden");
        $("#weeklyOptions").addClass("hidden");
        $("#monthlyOptions").addClass("hidden");
        $("#onceOptions").removeClass("hidden");

        $("#onceDateTextBox").val(schedule.date);
        $("#onceTimeTextBox").val(schedule.time);
    } else if (schedule.runType === "repeats") {
        var resolution = schedule.resolution;

        if (resolution === "weekly") {

            $("#radioWeekly").prop("checked", true);

            $("#dailyOptions").addClass("hidden");
            $("#weeklyOptions").removeClass("hidden");
            $("#monthlyOptions").addClass("hidden");
            $("#onceOptions").addClass("hidden");

            $("#weeklyEveryTextBox").val(schedule.every);
            $("#weeklyDays").val(schedule.days);

            $("#weeklyTimeTextBox").val(schedule.time);
            $("#weeklyStartTextBox").val(schedule.startingFrom);
            $("#weeklyEndTextBox").val(schedule.until);

        } else if (resolution === "daily") {

            $("#radioDaily").prop("checked", true);

            $("#dailyOptions").removeClass("hidden");
            $("#weeklyOptions").addClass("hidden");
            $("#monthlyOptions").addClass("hidden");
            $("#onceOptions").addClass("hidden");

            $("#dailyStartTextBox").val(schedule.startingFrom);
            $("#dailyEndTextBox").val(schedule.until);
            $("#dailyTimeTextBox").val(schedule.time);
            $("#dailyEveryTextBox").val(schedule.every);

        } else if (resolution === "monthly") {

            $("#radioMonthly").prop("checked", true);

            $("#dailyOptions").addClass("hidden");
            $("#weeklyOptions").addClass("hidden");
            $("#monthlyOptions").removeClass("hidden");
            $("#onceOptions").addClass("hidden");

            $("#monthlyEveryTextBox").val(schedule.every);
            $("#monthlyEndTextBox").val(schedule.until);
            $("#monthlyTimeTextBox").val(schedule.time);

            if (schedule.option === "date") {

                $("#radioMonthlyDate").prop("checked", true);

                $("#monthlyDate").removeClass("hidden");
                $("#monthlyDay").addClass("hidden");

                $("#monthlyDateTextBox").val(schedule.date);

            } else if (schedule.option === "day") {

                $("#radioMonthlyDay").prop("checked", true);

                $("#monthlyDate").addClass("hidden");
                $("#monthlyDay").removeClass("hidden");

                $("#monthlyDaySelectOne").val(schedule.one);
                $("#monthlyDaySelectTwo").val(schedule.two);

            }

        }
    }
}