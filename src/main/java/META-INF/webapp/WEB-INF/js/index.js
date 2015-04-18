$(document).ready(function(){

    $("#tabbedPanel").tabs();
    $("#scheduleDatePicker").datepicker({
        onSelect: function(dateText, inst){
            requestJobsForDate(dateText);
        }
    });
    requestQueries();
});

function requestJobsForDate(dateText) {
    $.ajax({
        type : "GET",
        url : "getJobsForDate",
        data : {
            "date" : dateText
        }
    }).done(function(response){
        createJobList(dateText, response);
    });
}

function createJobList(dateText, response) {
    var schedule = JSON.parse(response);

    if (response === "error") {
        alert("An exception occurred");
        return;
    }

    $("#jobList").html("");

    var newHtml = "<table><tr><th>Job Name</th><th>Queries</th><th>Email Spec</th><th>Time Rule</th></tr>";

    var odd = true;

    $.each(schedule, function(index, job){
        var jobName = job.job.jobName;
        var queries = job.job.queries;

        var subject = job.job.subject;
        var message = job.job.message;
        var emailTo = job.job.emailTo;



        var emailSpec = "<p><b>Addresses: </b>" + emailTo + "<p><b>Subject:</b> " + subject
            + "<br /><b>Message: </b>" + message;

        var executionTime = job.executionTime;
        var type = job.job.timeRule.type;
        var timeRule = "Execution Time: <b>" + executionTime + "</b>";


        if (type === "RepeatsDaily") {
            var every = job.job.timeRule.every;
            var startingFrom = job.job.timeRule.startingFrom;
            var until = job.job.timeRule.until;

            timeRule += "<br />Runs every <b>" + every + "</b> days<br />Starting from: <b>"
                + startingFrom + "</b><br/>Until: <b>" + until + "</b>";

        } else if (type === "RepeatsWeekly") {
            var days = job.job.timeRule.days;
            var every = job.job.timeRule.every;
            var startingFrom = job.job.timeRule.startingFrom;
            var until = job.job.timeRule.until;

            timeRule += "<br /><br />Runs on these days: <b>" + days + "</b><br />Every <b>" + every
                + "</b> weeks<br />Starting from: <b>" + startingFrom + "</b><br />Until: <b>" + until + "</b>";

        } else if (type === "RepeatsMonthlyDate") {
            var date = job.job.timeRule.dayOfMonth;
            var every = job.job.timeRule.every;
            var until = job.job.timeRule.until;

            timeRule += "<br /><br />Runs on the <b>" + date + "</b> of the month<br /> Every <b>"
                + every + "</b> months<br />Until <b>" + until + "</b>";

        } else if (type === "RepeatsMonthlyDay") {
            var one = job.job.timeRule.one;
            var two = job.job.timeRule.two;
            var every = job.job.timeRule.every;
            var until = job.job.timeRule.until;

            timeRule += "<br /><br />Runs on the <b>" + one + " " + two + "</b> of the month<br /> Every <b>"
                + every + "</b> months<br />Until <b>" + until + "</b>";
        }

        newHtml += '<tr id="' + jobName + '"';

        if (odd) {
            newHtml += ' class="odd scheduleRow"><td>' + jobName + '</td><td>' + queries + '</td><td>' + emailSpec
            + '</td><td>' + timeRule + '</td></tr>';
            odd = false;
        } else {
            newHtml += 'class="scheduleRow"><td>' + jobName + '</td><td>' + queries + '</td><td>' + emailSpec
            + '</td><td>' + timeRule + '</td></tr>';
            odd = true;
        }
    });

    newHtml += "</table>";
    $("#jobList").html(newHtml);

    $(".scheduleRow").click(function(){
        $.ajax({
            type : "GET",
            url : "getJobByName",
            data : {
                "jobName" : $(this).attr("id")
            }
        }).done(function(response){
            var responseObj = JSON.parse(response);
            createJobView(dateText, responseObj);
        });
    });
}

function requestQueries() {
    $.ajax({
        type : "GET",
        url : "getAllQueries"
    }).done(function(response){
        createQueryList(response);
    });
}

function requestQueryNames() {
    $.ajax({
        type : "GET",
        url : "getAllQueryNames"
    }).done(function(response){
        var queryNames = JSON.parse(response);
        addQueryNamesToSelect(queryNames);
    });
}

function requestColumns(dropDownToUpdate, valueToSelect) {
    $.ajax({
        type : "GET",
        url : "getAllColumns"
    }).done(function(response){
        var columns = JSON.parse(response);
        addColumnsToSelects(columns, dropDownToUpdate, valueToSelect);
    });
}

function addQueryNamesToSelect(queryNames) {
    var htmlValue = "";
    $.each(queryNames, function(index, name){
        htmlValue += '<option value="' + name + '">' + name + '</option>';
    });

    $("#querySelectBox").html(htmlValue);
}

function addColumnsToSelects(columnsToAdd, dropDownToUpdate, valueToSelect) {
    var htmlValue = "<option value=\"RAWVAL\"></option>";

    $.each(columnsToAdd, function(index, column) {
        htmlValue += "<option value=\"" + column + "\">" + column + "</option>";
    });

    if (!(typeof dropDownToUpdate === "undefined")) {
        $("#" + dropDownToUpdate).html(htmlValue);
            if (!(typeof dropDownToUpdate === "undefined")) {
                $("#" + dropDownToUpdate).val(valueToSelect);
            }
    } else {
        $(".columnSelectBox").html(htmlValue);
    }
}

function displayAddQueryWindow() {
    // get the screen height and width
    var maskHeight = $(document).height();
    var maskWidth = $(window).width();

    // calculate the values for center alignment
    var dialogTop =  0;
    var dialogLeft = (maskWidth/2) - ($('#queriesDialog').width()/2);

    // assign values to the overlay and dialog box
    $('#dialogUnderlay').css({height:maskHeight, width:maskWidth}).show();
    $('#queriesDialog').css({top:dialogTop, left:dialogLeft}).show();
}

function displayNewJobWindow(){
    // get the screen height and width
    var maskHeight = $(document).height();
    var maskWidth = $(window).width();

    // calculate the values for center alignment
    var dialogTop =  0;
    var dialogLeft = (maskWidth/2) - ($('#queriesDialog').width()/2);

    // assign values to the overlay and dialog box
    $('#dialogUnderlay').css({height:maskHeight, width:maskWidth}).show();
    $('#jobDialog').css({top:dialogTop, left:dialogLeft}).show();
}

function displayViewJobWindow() {
    // get the screen height and width
    var maskHeight = $(document).height();
    var maskWidth = $(window).width();

    // calculate the values for center alignment
    var dialogTop =  0;
    var dialogLeft = (maskWidth/2) - ($('#queriesDialog').width()/2);

    // assign values to the overlay and dialog box
    $('#dialogUnderlay').css({height:maskHeight, width:maskWidth}).show();
    $('#scheduleDialog').css({top:dialogTop, left:dialogLeft}).show();
}

function hideDialog() {
    $('#dialogUnderlay, .dialogBox').hide();
}

function createQueryList(queryListString) {
    var queryList = JSON.parse(queryListString);

    var htmlValue = '<div id="queriesAccordion">';

    $.each(queryList, function(index, query){
        //alert(JSON.stringify(query))
        var partialHtml = "";

        var name = query.queryName;

        partialHtml += "<h3>" + name + "</h3><div>";

        //partialHtml += '<button id="exe-' + name + '" class="executeButton">Execute Query</button>&nbsp;&nbsp;'
        //+ '<button id="edit-'+ name + '" class="editQueryButton">Edit Query</button><br />';

        partialHtml += '<button id="edit-' + name + '" class="editQueryButton">Edit Query</button>&nbsp;&nbsp;'
        + 'Enter <b>email address</b> to send executed query to: <input id="email-' + name
        + '"/>&nbsp;<em>(leave box blank to not email query result)</em>&nbsp;&nbsp;<button id="exe-'
        + name + '" class="executeButton">Execute Query</button><br />';


        if (query.template != null)
            partialHtml += "<p>Uses template: <b>" + query.template + "</b>.<br />";
        else
            partialHtml += "<p><b>Does not</b> use a template.<br />";

        if (query.outputTimestamp === "true" || query.outputTimestamp == true)
            partialHtml += "Outputs <b>include a timestamp</b>.</p>";
        else
            partialHtml += "Outputs <b>do not include a timestamp</b>.</p>";


        partialHtml += "<table><tr><th>Sheet Name</th><th>Hidden in Output?</th>"
                     + "<th>Filter Rule</th><th>Sort By</th><th>Group By</th><th>Columns</th></tr>";

        var odd = true;

        $.each(query.sheets, function(i, sheet){
            partialHtml += "<tr";

            if (odd == true){
                partialHtml += " class=\"odd\">";
                odd = false;
            } else {
                partialHtml += ">";
                odd = true;
            }

            //Sheet name
            partialHtml += "<td>" + sheet.sheetName + "</td>";

            //Hidden?
            partialHtml += "<td>" + sheet.hidden +"</td>";

            //Filter rule
            if ((sheet.filterColumn == null) || (sheet.filterColumn.trim() === "") ||
             (sheet.filterColumn.trim() === "null"))
                partialHtml += "<td>No filter rule</td>";
            else
                partialHtml += "<td>WHERE " + sheet.filterColumn + " EQUALS " + sheet.filterValue + "</td>";

            //Sort by
            partialHtml += "<td>" + sheet.sortBy + "</td>";

            //group by
            partialHtml += "<td>" + sheet.groupBy + "</td>";

            //columns
            partialHtml += "<td>";
            $.each(sheet.headers, function(headerIndex, header){
                partialHtml += "<b>" + header + "</b>:" + JSON.stringify(sheet.headerGroups[headerIndex]) + "<br />";
            });

            partialHtml += "</td></tr>";
        });

        partialHtml += "</table>";

        var mappedColumns = query.mappedColumns;
        var calculatedColumns = query.calculatedColumns;

        if (mappedColumns.length != 0) {
            partialHtml += "<table><tr><th>Reference</th><th>Original</th><th>Mapping</th></tr>";

            var odd = true;
            $.each(mappedColumns, function(mcI, mc){
                partialHtml += "<tr";

                if (odd == true) {
                    partialHtml += " class=\"odd\"><td>";
                    odd = false;
                } else {
                    partialHtml += "><td>";
                    odd = true;
                }

                partialHtml += mc.reference + "</td><td>" + mc.original + "</td><td>" + mc.header
                + "</td></tr>";
            });

            partialHtml += "</table>";
        }

        if (calculatedColumns.length != 0) {
                    partialHtml += "<table><tr><th>Reference</th><th>Header</th><th>First Parameter</th>"
                        + "<th>Operator</th><th>Second Parameter</th></tr>";

                    var odd = true;
                    $.each(calculatedColumns, function(ccI, cc){
                        partialHtml += "<tr";

                        if (odd == true) {
                            partialHtml += " class=\"odd\"><td>";
                            odd = false;
                        } else {
                            partialHtml += "><td>";
                            odd = true;
                        }

                        partialHtml += cc.reference + "</td><td>" + cc.header + "</td><td>" + cc.condition.firstHalf
                        + "</td><td>" + cc.condition.operator + "</td><td>" + cc.condition.secondHalf + "</td></tr>";
                    });

                    partialHtml += "</table>";
        }

        partialHtml += '<button id="remove-' + name + '" class="removeQueryButton">Remove Query</button>';


        partialHtml += "</div>";

        htmlValue += partialHtml;
    });

    htmlValue += "</div>"
    $("#queryList").html(htmlValue);
    $("#queriesAccordion").accordion({
        heightStyle: "content"
    });

    $(".editQueryButton").button().click(function(){
        var queryName = $(this).attr("id");
        queryName = queryName.substring(5);

        var r = confirm("Do you want to edit " + queryName);
        if (r) {
            $.ajax({
                type : "GET",
                url : "getQuery",
                data : {
                    "queryName" : queryName
                }
            }).done(function(data){
                var unprocessedQuery = JSON.parse(data);

                if (unprocessedQuery.result == false) {
                    alert("Unable to retrieve query for editing");
                    return;
                }

                 currentQuery = convertQueryObjectToUI(unprocessedQuery);
                createEditorWindow();
            });
        }
    });

    $(".removeQueryButton").button().click(function(){
        var queryName = $(this).attr("id");
        queryName = queryName.substring(7);

        var r = confirm("Do you want to remove " + queryName + "?");
        if (r) {
        }
            $.ajax({
                type : "POST",
                url : "removeQuery",
                data : {
                    "queryName" : queryName
                }
            }).done(function(data){
                requestQueries();
            });
    });

    $(".executeButton").button().click(function(){
        var queryName = $(this).attr("id");
        queryName = queryName.substring(4);

        var emailTextValue = $("#email-" + queryName).val();
        var emailAddress = "";

        if (typeof emailTextValue === "undefined" || emailTextValue.trim() === "") {
            emailAddress = "N/A";
        } else {
            var re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
            if (re.test(emailTextValue)) {
                emailAddress = emailTextValue;
            } else {
                alert("Invalid email address");
                return;
            }
        }

        var r;
        if (emailAddress === "N/A") {
            r = confirm("Do you want to execute without emailing result?");
        } else {
            r = confirm("Do you want to execute and send result to " + emailAddress + "?");
        }

        if (r) {
            $(".executeButton").attr("disabled", true);
            $(".editQueryButton").attr("disabled", true);
            $.ajax({
                type : "POST",
                url : "executeQuery",
                data : {
                    "queryName" : queryName,
                    "emailAddress" : emailAddress
                    }
            }).done(function(data) {
                var result = JSON.parse(data);

                if (result.result == true) {
                    alert("Successfully executed " + result.queryName);
                    $(".executeButton").removeAttr("disabled");
                    $(".editQueryButton").removeAttr("disabled");
                } else {
                    alert("Failed to execute " + result.queryName);
                    $(".executeButton").removeAttr("disabled");
                    $(".editQueryButton").removeAttr("disabled");
                }
            });
        }
    });
}