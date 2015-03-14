var currentQuery;

$(document).ready(function(){

    $(".button").button();

     $("#addQueryButton").click(function(){

            var header = initNewHeader("Header 0", []);
            var sheet = initNewSheet("Sheet 0", false, [header]);
            currentQuery = initNewQuery("Query 0", false, false, [sheet]);
            createHeadersUIForSheet(currentQuery, 0);
            displayAddQueryWindow();
        });

    $("#cancelQueryButton").click(function(){
                hideAddQueryWindow();
    });

    $(".addColumnButton").click(function() {
        var sheetIndex = $(this).attr("id").substring(5, 6);
        var headerIndex = $(this).attr("id").substring(13, 14);
        switchToColumnEditor(currentQuery, sheetIndex, headerIndex);
    });
});