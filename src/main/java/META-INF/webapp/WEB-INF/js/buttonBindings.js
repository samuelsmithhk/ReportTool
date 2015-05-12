var currentQuery;

$(document).ready(function(){

    $(".button").button();

    $("#addQueryButton").click(function(){
        var header = initNewHeader("Header 0", []);
        var sheet = initNewSheet("Sheet 0", null, "n", false, [header]);
        currentQuery = initNewQuery("Query 0", false, false, [sheet]);
        initQueryCheckboxes(currentQuery);
        createSheetsUIForQuery(currentQuery);
        displayAddQueryWindow();
    });

    $("#newJobButton").click(function(){
        clearJobEditorWindow();
        requestQueryNames();
        displayNewJobWindow();
    });

    $("#addSheetButton").click(function(){
        var numberOfSheets = getNumberOfSheets(currentQuery);
        var header = initNewHeader("Header 0", []);
        var sheet = initNewSheet("Sheet " + numberOfSheets, null, "n", false, [header]);
        currentQuery.sheets.push(sheet);
        createSheetsUIForQuery(currentQuery);
    });

    $("#cancelQueryButton").click(function(){
        var r = confirm("Are you sure you want to cancel this query?");
        if (r) {
            hideDialog();
        }
    });

    $("#saveQueryButton").click(function(){
        currentQuery.name = $("#queryNameTextBox").val();

        currentQuery.timestamp = $("#outputTimestampCB").is(':checked');
        currentQuery.template = $("#useTemplateCB").is(":checked");
        currentQuery.templateFile = $("#templateSelect").val();

        $.each(currentQuery.sheets, function(sheetIndex, sheet){

            var prioritySS = $("#sheet" + sheetIndex + "-prioritySSSelect").val();
            var fallback = $("#sheet" + sheetIndex + "-fallbackSelect").val();

            if (prioritySS === "RAWVAL" || typeof prioritySS === "undefined") {
                sheet.prioritySS = "";
                sheet.fallback = false;
            } else {
                sheet.prioritySS = prioritySS;
                sheet.fallback = fallback;
            }


            var filterColumn = $("#sheet" + sheetIndex + "-filterColumnSelect").val();
            var filterValue = $("#sheet" + sheetIndex + "-filterValueTextBox").val();
            var sortBy = $("#sheet" + sheetIndex + "-sortBySelect").val();
            var groupBy = $("#sheet" + sheetIndex + "-groupBySelect").val();

            if (filterColumn === "RAWVAL" || typeof filterColumn === "undefined") {
                sheet.filterColumn = "";
                sheet.filterValue = "";
            } else {
                sheet.filterColumn = filterColumn;
                sheet.filterValue = filterValue;
            }

            if (sortBy === "RAWVAL" || typeof sortBy === "undefined") {
                sheet.sortBy = "";
            } else {
                sheet.sortBy = sortBy;
            }

            if (groupBy === "RAWVAL" || typeof groupBy === "undefined") {
                sheet.groupBy = "";
            } else {
                sheet.groupBy = groupBy;
            }
        });

        if (validateSaveQuery(currentQuery)) {
            var r = confirm("Are you sure you want to save this query?");
            if (r) {
                var toSave = convertQueryObject(currentQuery);
                $.ajax({
                    url : "saveQuery",
                    method : "POST",
                    data : {
                        "query" : JSON.stringify(toSave)
                    }
                }).done(function(response){
                    requestQueries();
                });

                hideDialog();
            }
        }
    });

    $("#closeJobViewerButton").click(function(){
        var r = confirm("Are you sure you want to close?");
        if (r) {
            hideDialog();
        }
    });

    $("#cancelJobButton").click(function(){
        var r = confirm("Are you sure you want to cancel?");
        if (r) {
            hideDialog();
        }
    });

    $("#editJobButton").click(function(){
        var jobName = $("#jobNameLabel").html();

        var r = confirm("Do you want to edit " + jobName + "? This will effect all instances of this job");
        if (r) {
            $.ajax({
                type : "GET",
                url : "getJobByName",
                data : {
                    "jobName" : jobName
                }
            }).done(function(data){
                var job = JSON.parse(data);

                if (job.result === 'error') {
                    alert("Unable to retrieve job for editing");
                    return;
                }

                createJobEditorWindow(job);
            });
        }
    });

    $("#saveJobButton").click(function(){
        validateAndSaveJob();
    });

    $("#removeJobButton").click(function(){
        var toRemove = $("#jobNameLabel").html();

        var r = confirm("Are you sure you want to remove " + toRemove + "?");
        if (r) {
            $.ajax({
                url : "removeJob",
                method : "POST",
                data : {
                    "jobName" : toRemove
                }
            }).done(function(response){
                if (response === "success") {
                    hideDialog();
                    requestJobsForDate($("#dateText").html());
                } else {
                    alert("An error occurred trying to remove " + toRemove);
                }
            });
        }
    });

    $("input[type=radio][name=scheduleRuleRadio]").change(function(){
        var option = $(this).attr('id');

        if (option === "radioOnce") {
            $("#onceOptions").removeClass("hidden");
            $("#dailyOptions").addClass("hidden");
            $("#weeklyOptions").addClass("hidden");
            $("#monthlyOptions").addClass("hidden");
        } else if (option === "radioDaily") {
            $("#onceOptions").addClass("hidden");
            $("#dailyOptions").removeClass("hidden");
            $("#weeklyOptions").addClass("hidden");
            $("#monthlyOptions").addClass("hidden");
        } else if (option === "radioWeekly") {
            $("#onceOptions").addClass("hidden");
            $("#dailyOptions").addClass("hidden");
            $("#weeklyOptions").removeClass("hidden");
            $("#monthlyOptions").addClass("hidden");
        } else if (option === "radioMonthly") {
            $("#onceOptions").addClass("hidden");
            $("#dailyOptions").addClass("hidden");
            $("#weeklyOptions").addClass("hidden");
            $("#monthlyOptions").removeClass("hidden");
        }
    });


    $("input[type=radio][name=radioMonthlyOption]").change(function(){
        var option = $(this).attr('id');

        if (option === "radioMonthlyDate") {
            $("#monthlyDate").removeClass("hidden");
            $("#monthlyDay").addClass("hidden");
        } else if (option === "radioMonthlyDay") {
            $("#monthlyDate").addClass("hidden");
            $("#monthlyDay").removeClass("hidden");
        }
    });
});