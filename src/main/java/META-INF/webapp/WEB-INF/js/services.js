$(document).ready(function(){

    $("#reloadCacheLink").click(function(){
        var r = confirm("Are you sure you want to force the creation of a new cache from the current inputs? Any data not in current input files will be lost.");

        if (r) {
            alert("Process may take some time. Do not use the ReportTool until you get a complete alert");
            displayServiceNotReadyWindow();
            isNotReady();
            $.ajax({
                type: "GET",
                url : "reloadCache"
            }).done(function(response){
                if (JSON.parse(response) === "error") {
                    alert("An exception occurred. Probably best to terminate the service and reload");
                }
            });
        }
    });

    $("#reloadQueriesLink").click(function(){
        var r = confirm("This will reload the queries from the filesystem. Continue?");

        if (r) {
            alert("Do not use the ReportTool until you get a complete alert");
            displayServiceNotReadyWindow();
            isNotReady();
            $.ajax({
                type : "GET",
                url : "reloadQueries"
            }).done(function(response){
                if (JSON.parse(response) === "error") {
                    alert("An exception occurred. Probably best to terminate the service and reload");
                }
            });
        }
    });

    $("#reloadScheduleLink").click(function(){
        var r = confirm("This will reload the schedule from the filesystem. Continue?");
        displayServiceNotReadyWindow();
        isNotReady();
        if (r) {
            alert("Do not use the ReportTool until you get a complete alert");

            $.ajax({
                type : "GET",
                url : "reloadSchedule"
            }).done(function(response){
                if (JSON.parse(response) === "error") {
                    alert("An exception occurred. Probably best to terminate the service and reload");
                }
            });
        }
    });

    $("#terminateServiceLink").click(function(){
        var r = confirm("Are you sure you want to terminate this service?");
        isNotReady();
        if (r) {
            alert("Terminating");

            $.ajax({
                type : "GET",
                url : "terminateService"
            });
        }
    });

});