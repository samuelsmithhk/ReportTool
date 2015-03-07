$(document).ready(function(){

    $("#tabbedPanel").tabs();

    $("#sheetsTabs").tabs();
    $("#sheet0HeadersAccordion").accordion();

    $.ajax({
        type : "GET",
        url : "getAllQueries",
    }).done(function(response){
        createQueryList(response);
    });
});

function requestColumns() {
    $.ajax({
        type : "GET",
        url : "getAllColumns"
    }).done(function(response){
        var columns = JSON.parse(response);
        addColumnsToSelects(columns);
    });
}

function addColumnsToSelects(columnsToAdd) {
    var htmlValue = "<option value=\"RAWVAL\"></option>";

    $.each(columnsToAdd, function(index, column) {
        htmlValue += "<option value=\"" + column + "\">" + column + "</option>";
    });

    $(".columnSelectBox").html(htmlValue);
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

function hideAddQueryWindow() {
    $('#dialogUnderlay, .dialogBox').hide();
}

function createQueryList(queryListString) {
    var queryList = JSON.parse(queryListString);

    var htmlValue = ""

    $.each(queryList, function(index, query){
        //alert(JSON.stringify(query))
        var partialHtml = "";

        var name = query.queryName;

        partialHtml += "<h3>" + name + "</h3><div>";

        partialHtml += "<button id=\"exe-" + name + "\" class=\"executeButton\">Execute Query</button><br />"


        if (query.template != null)
            partialHtml += "<p>Uses template: <b>" + query.template + "</b>.<br />";
        else
            partialHtml += "<p><b>Does not</b> use a template.<br />";

        if (query.outputTimestamp == true)
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


        partialHtml += "</div>"

        htmlValue += partialHtml;
    });

    $("#queriesAccordion").html(htmlValue);
    $("#queriesAccordion").accordion();
    $(".executeButton").button();

    $(".executeButton").click(function(){
        var queryName = $(this).attr("id");
        queryName = queryName.substring(4);

        var r = confirm("Do you want to execute " + queryName);
        if (r == true) {
            $(".executeButton").attr("disabled", true);
            $.ajax({
                type : "POST",
                url : "executeQuery",
                data : {
                    "queryName" : queryName
                    }
            }).done(function(data) {
                var result = JSON.parse(data);

                if (result.result == true) {
                    alert("Successfully executed " + result.queryName);
                    $(".executeButton").removeAttr("disabled");
                } else {
                    alert("Failed to execute " + result.queryName);
                    $(".executeButton").removeAttr("disabled");
                }
            });
        }
    });
}