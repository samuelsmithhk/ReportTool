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
});