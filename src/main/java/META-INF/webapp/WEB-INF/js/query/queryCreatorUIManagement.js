function updateColumnsTable(query, sheetIndex, headerIndex) {
    var newHtml = "<tr><th>Column</th><th>Rule</th></tr>";
    var cols = query.sheets[sheetIndex].headers[headerIndex].columns;

    var odd = true;
    $.each(cols, function(colIndex, col){
        newHtml += "<tr";

        if (odd === true) {
            newHtml += " class=\"odd\"";
            odd = false;
        } else {
            odd = true;
        }

        var colLocator = "sheet" + sheetIndex + "-header" + headerIndex + "-col" + colIndex;

        newHtml += "><td>" + col.name + "</td><td>" + col.rule + "</td><td><button id=\"" + colLocator +
         "-editButton\">Edit</button></td><td><button id=\"" + colLocator
             + "-removeButton\">Remove</button></td><td><button id=\"" + colLocator
             + "-moveUpButton\">Move Up</button></td><td><button id=\"" + colLocator
             + "-moveDownButton\">Move Down</button></td></tr>";

    });

    $("#sheet" + sheetIndex + "-header" + headerIndex + "-columnsTable").html(newHtml);

    $.each(cols, function(colIndex, col){
        var colLocator = "sheet" + sheetIndex + "-header" + headerIndex + "-col" + colIndex;

        $("#" + colLocator + "-removeButton").click(function(){
            alert("remove clicked");
        });

        $("#" + colLocator + "-editButton").click(function(){
            switchToColumnEditor(query, sheetIndex, headerIndex, colIndex);
        });

        $("#" + colLocator + "-moveUpButton").click(function(){
            alert("move up clicked");
        });

        $("#" + colLocator + "-moveDownButton").click(function(){
            alert("move down clicked");
        });
    });
}

function switchToColumnListViewer() {
}

function switchToColumnEditor(query, sheetIndex, headerIndex, columnIndex) {
    //first we need to build the html
    var columnEditorHtml = '<div id="sheet' + sheetIndex + '-header' + headerIndex
        + '-columnCreator" class="hidden"><div id="columnTypeButtonSet">'
        + '<input type="radio" id="directColumnOption" name="columnType" checked>'
        + '<label for="directColumnOption">Direct from Cache</label>'
        + '<input type="radio" id="calculatedColumnOption" name="columnType">'
        + '<label for="calculatedColumnOption">Calculated Column</label></div> <br /><div id="directColumn">'
        + '<label for="directColumnList">Pick Column: </label><select id="directColumnList" class="columnSelectBox">'
        + '</select><br /><label for="overwriteNameTextBox">Overwrite Name on Output: (optional)</label>'
        + '<input id="overwriteNameTextBox"></div><div id="calculatedColumn" class="hidden">'
        + '<label for="columnNameTextBox">Name on Output: </label><input id="columnNameTextBox"><table><tr>'
        + '<th>First Parameter</th><th>Operator</th><th>Second Parameter</th></tr><tr class="odd"><td>'
        + '<label for="param1SelectColumn">Pick Column: </label>'
        + '<select id="param1SelectColumn" class="paramSelectColumn columnSelectBox"></select> <br />'
        + '<label for="param1RawValueTextBox">Raw Value: </label><input id="param1RawValueTextBox"></td><td>'
        + '<select id="operatorSelect"><option value="h">HISTORIC</option><option value="a">ADD</option>'
        + '<option value="s">SUBTRACT</option><option value="m">MULTIPLY</option><option value="d">DIVIDE</option>'
        + '<option value="ag">AGGREGATE</option><option value="av">AVERAGE</option><option value="c">CONCAT</option>'
        + '</select></td><td><div id="secondParamHistoric"><label for="historicDays">Number of Days Back: </label>'
        + '<input id="historicDays"></div><div id="secondParamNormal" class="hidden">'
        + '<label for="param2SelectColumn">Pick Column: </label>'
        + '<select id="param2SelectColumn" class="paramSelectColumn columnSelectBox"></select> <br />'
        + '<label for="param2RawValueTextBox">Raw Value: </label><input id="param2RawValueTextBox"></div>'
        + '<div id="secondParamRange" class="hidden"><label for="valueRange">Range: </label><input id="valueRange">'
        + '</div></td></tr></table></div> <br /><br />'
        + '<button id="sheet0-header0-saveColumnButton" class="saveColumnButton button">Save Column</button>'
        + '<button id="cancelColumnButton" class="button">Cancel Column</button></div>';

        $("#sheet" + sheetIndex + "HeadersAccordion div").html(columnEditorHtml);

        $("#columnTypeButtonSet").buttonset().change(function(){
                if ($("#directColumnOption").is(":checked")) {
                    $("#directColumn").show();
                    $("#calculatedColumn").hide();
                } else {
                    $("#directColumn").hide();
                    $("#calculatedColumn").show();
                }
            });

        $(".button").button();
        $("#operatorSelect").on("change", function(){
                var val = $(this).val();

                if (val === "h") {
                    $("#secondParamHistoric").show();
                    $("#secondParamNormal").hide();
                    $("#secondParamRange").hide();
                } else if (val === "a" || val === "s" || val === "m" || val === "d" || val === "c") {
                    $("#secondParamNormal").show();
                    $("#secondParamHistoric").hide();
                    $("#secondParamRange").hide();
                } else { //val === ag || av
                    $("#secondParamRange").show();
                    $("#secondParamHistoric").hide();
                    $("#secondParamNormal").hide();
                }
            });

        $(".paramSelectColumn").on("change", function(){
                var value = $(this).val();
                var whichParam = $(this).attr("id").substring(5, 6);
                var inputToUpdate = $("#param" + whichParam + "RawValueTextBox");

                if (value === "RAWVAL") {
                    inputToUpdate.removeAttr("disabled");
                    inputToUpdate.val("");
                } else {
                    inputToUpdate.attr("disabled", true);
                    inputToUpdate.val("SELECT BLANK ABOVE");
                }
            });
        $(".saveColumnButton").click(function() {
            var sheetIndex = $(this).attr("id").substring(5, 6);
            var headerIndex = $(this).attr("id").substring(13, 14);

            if(validateSaveColumn()) {
                var r = confirm("Do you want to save column?");
                if (r) {
                 var column = createColumn();
                 addColumnToHeader(currentQuery, sheetIndex, headerIndex, column);
                 updateColumnsTable(currentQuery, sheetIndex, headerIndex);
                 $(".sheet" + sheetIndex + "-header" + headerIndex).show();
                 $("#sheet" + sheetIndex + "-header" + headerIndex + "-columnCreator").hide();
             }
            }
        });

        requestColumns();

        $("#sheet" + sheetIndex + "-header" + headerIndex + "-columnCreator").show();


    var col = query.sheets[sheetIndex].headers[headerIndex].columns[columnIndex];

    var name = col.name;
    var rule = col.rule;

    if (rule.trim() === "") { //this is a direct column with no mapping
    } else if (rule.indexOf("~~") == -1) { //this is a direct column with mapping
    }
}