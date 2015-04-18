function createJobView(response) {
    if (response === "error") {
        alert("An error occurred");
    }

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
                createJobView(JSON.parse(newResponse));
            });
        }
    });

    displayViewJobWindow();
}