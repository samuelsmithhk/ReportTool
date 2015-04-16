var currentQuery;

$(document).ready(function(){

    $(".button").button();

    $("#addQueryButton").click(function(){
        var header = initNewHeader("Header 0", []);
        var sheet = initNewSheet("Sheet 0", false, [header]);
        currentQuery = initNewQuery("Query 0", false, false, [sheet]);
        initQueryCheckboxes(currentQuery);
        createSheetsUIForQuery(currentQuery);
        displayAddQueryWindow();
    });

    $("#addSheetButton").click(function(){
        var numberOfSheets = getNumberOfSheets(currentQuery);
        var header = initNewHeader("Header 0", []);
        var sheet = initNewSheet("Sheet " + numberOfSheets, false, [header]);
        currentQuery.sheets.push(sheet);
        createSheetsUIForQuery(currentQuery);
    });

    $("#cancelQueryButton").click(function(){
        var r = confirm("Are you sure you want to cancel this query?");
        if (r) {
            hideAddQueryWindow();
        }
    });

    $("#saveQueryButton").click(function(){
        currentQuery.name = $("#queryNameTextBox").val();

        currentQuery.timestamp = $("#outputTimestampCB").is(':checked');
        currentQuery.template = $("#useTemplateCB").is(":checked");
        currentQuery.templateFile = $("#templateSelect").val();

        $.each(currentQuery.sheets, function(sheetIndex, sheet){
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

                hideAddQueryWindow();
            }
        }
    });
});