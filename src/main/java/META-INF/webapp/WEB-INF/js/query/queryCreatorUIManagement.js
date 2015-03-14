function initQueryCheckboxes(query) {
    $("#outputTimestampCB").prop("selected", query.timestamp);

    $("#outTimestampCB").change(function(){
        query.timestamp = $(this).is(":checked");
    });

    $("#useTemplateCB").prop("selected", query.template);

    $("#useTemplateCB").change(function(){
        query.template = $(this).is(":checked");

        if ($(this).is(":checked")) {
            var temp = "<option>LOADING</option>";
            $("#templateSelect").html(temp);
            $("#templateSelect").prop("disabled", true);

            $.ajax({
                type : "GET",
                url : "/getAllTemplates"
            }).done(function(response){
                var templates = JSON.parse(response);
                var newHtml = "";
                $.each(templates, function(templateIndex, template) {
                    newHtml += '<option val="' + template + '">' + template + '</option>';
                });

                $("#templateSelect").html(newHtml);
                $("#templateSelect").prop("disabled", false);
            });
        } else {
            $("#templateSelect").prop("disabled", true);
        }
    });
}

function createSheetsUIForQuery(query) {
    var newHtml = '<div id="sheetsTabs"><ul>';

    $.each(query.sheets, function(sheetIndex, sheet){
        newHtml += '<li><a href="#sheet' + sheetIndex + '">' + sheet.name + '</a></li>';
    });

    newHtml += "</ul>";

    $.each(query.sheets, function(sheetIndex, sheet){
        newHtml += '<div id="sheet' + sheetIndex + '"><button id="sheet' + sheetIndex
        + '-moveSheetLeftButton" class="button">Move Sheet Left</button><button id="sheet'
        + sheetIndex + '-moveSheetRightButton" class="button">Move Sheet Right</button><br /><br />'
        + '<label for="sheet' + sheetIndex
        + '-nameTextBox">Sheet Name: </label><input id="sheet' + sheetIndex + '-nameTextBox"/>&nbsp;'
        + '<button id="sheet' + sheetIndex + '-saveSheetNameButton" class="button">Save Sheet Name</button>'
        + '<br /><br /><div id="sheet' + sheetIndex + '-headers"></div><br />'
        + '<input type="checkbox" id="sheet' + sheetIndex + '-hideOutputCB">Hide this sheet in output?<br /><br />'
        + '<button id="sheet' + sheetIndex + '-removeSheetButton" class="button">Remove Sheet</button></div>';
    });

    newHtml += "</div>"

    $("#sheets").html(newHtml);
    $("#sheetsTabs").tabs({
        create : function(event, ui) {
            createHeadersUIForSheet(query, 0);
        },
        activate : function(event, ui) {
            var sheetIndex = parseInt(ui.newTab.find("a").attr("href").substring(6));
            createHeadersUIForSheet(query, sheetIndex);
        }
    });

    $.each(query.sheets, function(sheetIndex, sheet){
        $("#sheet" + sheetIndex + "-nameTextBox").val(sheet.name);

        $("#sheet" + sheetIndex + "-moveSheetLeftButton").button().click(function(){
            if (sheetIndex > 0) {
                moveSheetLeft(query, sheetIndex);
                createSheetsUIForQuery(query);
            }
        });

        $("#sheet" + sheetIndex + "-moveSheetRightButton").button().click(function(){
            if (sheetIndex < (getNumberOfSheets(query) - 1)) {
                moveSheetRight(query, sheetIndex);
                createSheetsUIForQuery(query);
            }
        });

        $("#sheet" + sheetIndex + "-saveSheetNameButton").button().click(function(){
            query.sheets[sheetIndex].name = $("#sheet" + sheetIndex + "-nameTextBox").val();
            createSheetsUIForQuery(query, sheetIndex);
        });

        $("#sheet" + sheetIndex + "-removeSheetButton").button().click(function(){
            if (getNumberOfSheets(query) <= 1) {
                alert("Unable to remove sheet, as at least one sheet is required");
            } else {
                var r = confirm("Do you want to remove this sheet?");
                if (r) {
                    removeSheet(query, sheetIndex);
                    createSheetsUIForQuery(query, sheetIndex);
                }
            }
        });

        $("#sheet" + sheetIndex + "-hideOutputCB").prop("checked", query.sheets[sheetIndex].hidden)

        $("#sheet" + sheetIndex + "-hideOutputCB").change(function(){
            query.sheets[sheetIndex].hidden = $(this).is(":checked");
        });
    });
}

function createHeadersUIForSheet(query, sheetIndex) {
    var newHtml = '<div id="sheet' + sheetIndex + '-headersAccordion" class="headerAccordion">';

    $.each(query.sheets[sheetIndex].headers, function(headerIndex, header){
        newHtml += '<h3 id="headerIndex-' + headerIndex + '">' + header.name + '</h3>';
        newHtml += '<div id="sheet' + sheetIndex + '-header' + headerIndex + '-accDiv"></div>';
    });

    newHtml += '</div><br /><button id="sheet' + sheetIndex + '-addHeaderButton" class="button">Add Header</button>';

    $("#sheet" + sheetIndex + "-headers").html(newHtml);

    $("#sheet" + sheetIndex + "-headersAccordion").accordion({
        heightStyle : "content",
        create : function(event, ui){
            updateColumnsTable(query, sheetIndex, 0);
        },
        activate: function(event, ui){
            var headerIndex = parseInt(ui.newHeader.attr("id").substring(12));
            updateColumnsTable(query, sheetIndex, headerIndex);
        }
    });

    $("#sheet" + sheetIndex + "-addHeaderButton").button().click(function(){
        var header = initNewHeader("Header " + getNumberOfHeaders(query, sheetIndex), []);
        query.sheets[sheetIndex].headers.push(header);
        createHeadersUIForSheet(query, sheetIndex);
    });


}

function updateColumnsTable(query, sheetIndex, headerIndex) {
    var headerLocator = "sheet" + sheetIndex + "-header" + headerIndex;

    var newHtml = '<button id="' + headerLocator +'-moveHeaderUp" class="button '
        + headerLocator + '">Move Header Up</button><button id="' + headerLocator
        + '-moveHeaderDown" class="button ' + headerLocator + '">Move Header Down</button>'
        + '<button id="' + headerLocator + '-removeHeaderButton" class="button '
        + headerLocator + '">Remove Header</button><br /><br />'
        + '<label for="' + headerLocator + '-nameTextBox" class="'
        + headerLocator + '">Header Name: </label>'
        + '<input id="' + headerLocator + '-nameTextBox" class="'
        + headerLocator + '">&nbsp;&nbsp;<button id="'
         + headerLocator + '-saveHeaderNameButton" class="button '
                + headerLocator + '">Save Header Name</button><br /><br />'
        + '<button id="' + headerLocator + '-addColumnButton" class="button '
        + headerLocator + ' addColumnButton">Add Column</button>'
        + '<table id="' + headerLocator +'-columnsTable" class="'
        + headerLocator +'"><tr><th>Column</th><th>Rule</th></tr>';

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

        var colLocator = headerLocator + "-col" + colIndex;

        newHtml += "><td>" + col.name + "</td><td>" + col.rule + "</td><td><button id=\"" + colLocator +
         "-editButton\">Edit</button></td><td><button id=\"" + colLocator
             + "-removeButton\">Remove</button></td><td><button id=\"" + colLocator
             + "-moveUpButton\">Move Up</button></td><td><button id=\"" + colLocator
             + "-moveDownButton\">Move Down</button></td></tr>";

    });

    newHtml += "</table>"

    $("#" + headerLocator + '-accDiv').html(newHtml);
    $("#" + headerLocator + "-nameTextBox").val(query.sheets[sheetIndex].headers[headerIndex].name);

    $("#" + headerLocator + "-moveHeaderUp").button().click(function(){
        if (headerIndex > 0) {
            shiftHeaderUp(query, sheetIndex, headerIndex);
            createHeadersUIForSheet(query, sheetIndex);
        }
    });

    $("#" + headerLocator + "-moveHeaderDown").button().click(function() {
        if (headerIndex < (getNumberOfHeaders(query, sheetIndex) - 1)) {
            shiftHeaderDown(query, sheetIndex, headerIndex);
            createHeadersUIForSheet(query, sheetIndex);
        }
    });

    $("#" + headerLocator + "-removeHeaderButton").button().click(function(){
        if (getNumberOfHeaders(query, sheetIndex) <= 1) {
            alert("Unable to remove header as it is the only one for this sheet");
        } else {
            var r = confirm("Are you sure you want to remove this header?");
            if (r) {
                removeHeader(query, sheetIndex, headerIndex);
                createHeadersUIForSheet(query, sheetIndex);
            }
        }
    });

    $("#" + headerLocator + "-addColumnButton").button().click(function(){
        switchToColumnEditor(currentQuery, sheetIndex, headerIndex);
    });

    $("#" + headerLocator + "-saveHeaderNameButton").button().click(function(){
        query.sheets[sheetIndex].headers[headerIndex].name = $("#" + headerLocator + "-nameTextBox").val();
        createHeadersUIForSheet(query, sheetIndex);
    });


    $.each(cols, function(colIndex, col){
        var colLocator = "sheet" + sheetIndex + "-header" + headerIndex + "-col" + colIndex;

        $("#" + colLocator + "-editButton").click(function(){
            switchToColumnEditor(query, sheetIndex, headerIndex, colIndex);
        });

        $("#" + colLocator + "-removeButton").click(function(){
            var r = confirm("Do you want to remove this column?");
            if (r) {
                removeColumn(query, sheetIndex, headerIndex, colIndex);
                updateColumnsTable(currentQuery, sheetIndex, headerIndex);
            }
        });


        $("#" + colLocator + "-moveUpButton").click(function(){
            if (colIndex > 0) {
                shiftColumnUp(currentQuery, sheetIndex, headerIndex, colIndex);
                updateColumnsTable(currentQuery, sheetIndex, headerIndex);
            }
        });

        $("#" + colLocator + "-moveDownButton").click(function(){
            if (colIndex < (getNumberOfColumns(currentQuery, sheetIndex, headerIndex) - 1)) {
                shiftColumnDown(currentQuery, sheetIndex, headerIndex, colIndex);
                updateColumnsTable(currentQuery, sheetIndex, headerIndex);
            }
        });
    });
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
        + '<button id="saveColumnButton" class="button">Save Column</button>'
        + '<button id="cancelColumnButton" class="button">Cancel Column</button></div>';

    $("#sheet" + sheetIndex + "-header" + headerIndex + '-accDiv').html(columnEditorHtml);

    $("#directColumnOption").prop("checked", true);

    $("#columnTypeButtonSet").buttonset().change(function(){setColumnCreatorForType()});

    $(".button").button();

    $("#operatorSelect").on("change", function(){
        changeOperatorTypeInColumnCreator();
    });

    $(".paramSelectColumn").on("change", function(){
        changeParamSelectBoxInColumnCreator($(this));
    });

    $("#saveColumnButton").click(function() {
        if(validateSaveColumn()) {
            var r = confirm("Do you want to save column?");
            if (r) {
                if (typeof columnIndex === "undefined") {
                    var column = createColumn();
                    addColumnToHeader(currentQuery, sheetIndex, headerIndex, column);
                    updateColumnsTable(currentQuery, sheetIndex, headerIndex);
                } else {
                    editColumn(currentQuery, sheetIndex, headerIndex, columnIndex);
                    updateColumnsTable(currentQuery, sheetIndex, headerIndex);
                    $(".sheet" + sheetIndex + "-header" + headerIndex).show();
                    $("#sheet" + sheetIndex + "-header" + headerIndex + "-columnCreator").hide();
                }
            }
        }
    });

    $("#cancelColumnButton").click(function(){
        var r = confirm("Are you sure you want to cancel?");
        if (r) {
            clearColumnEditor();
            updateColumnsTable(currentQuery, sheetIndex, headerIndex);
        }
    });



    if (!(typeof columnIndex === "undefined")) {
        var col = query.sheets[sheetIndex].headers[headerIndex].columns[columnIndex];

        var name = col.name;
        var rule = col.rule;

        if (rule.trim() === "") { //this is a direct column with no mapping
            $("#directColumnOption").prop("checked", true);
            $("#calculatedColumnOption").prop("checked", false);
            setColumnCreatorForType();
            requestColumns("directColumnList", col.name);
        } else if (rule.indexOf("~~") == -1) { //this is a direct column with mapping
            $("#directColumnOption").prop("checked", true);
            $("#calculatedColumnOption").prop("checked", false);
            setColumnCreatorForType();
            requestColumns("directColumnList", col.rule);
            $("#overwriteNameTextBox").val(col.name)
        } else {
            $("#directColumnOption").prop("checked", false);
            $("#calculatedColumnOption").prop("checked", true);
            setColumnCreatorForType();

            var rule = col.rule;
            rule = rule.replace("~~", "");

            var pos = rule.indexOf("~~");
            var param1 = rule.substring(0, pos).trim();
            rule = rule.substring(pos);

            rule = rule.replace("~~", "");

            pos = rule.indexOf("~~");
            var operator = rule.substring(0, pos).trim();
            rule = rule.substring(pos);

            rule = rule.replace("~~", "");

            pos = rule.indexOf("~~");
            var param2 = rule.substring(0, pos).trim();

            $("#operatorSelect").val(operator);
            changeOperatorTypeInColumnCreator();

            if (operator === "h") {
                requestColumns("param1SelectColumn", param1);
                $("#historicDays").val(param2);
            } else if (operator === "a" || operator === "s" || operator === "d" || operator === "m" || operator === "c") {
                if (param1.indexOf("##") != -1) {
                    requestColumns("param1SelectColumn", "RAWVAL");
                    $("#param1RawValueTextBox").val(param1.substring(2));
                } else {
                    requestColumns("param1SelectColumn", param1);
                }

                if (param2.indexOf("##") != -1) {
                    requestColumns("param2SelectColumn", "RAWVAL");
                    $("#param2RawValueTextBox").val(param1.substring(2));
                } else {
                    requestColumns("param2SelectColumn", param2);
                }
            } else if (operator === "ag" || operator === "av") {
                requestColumns("param1SelectColumn", param1);
                $("#valueRange").val(param2);
            }
        }
    } else {
        requestColumns();
    }

    $("#sheet" + sheetIndex + "-header" + headerIndex + "-columnCreator").show();

}

function setColumnCreatorForType() {
    if ($("#directColumnOption").is(":checked")) {
        $("#directColumn").show();
        $("#calculatedColumn").hide();
    } else {
        $("#directColumn").hide();
        $("#calculatedColumn").show();
    }
}

function changeOperatorTypeInColumnCreator() {
    var val = $("#operatorSelect").val();

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
}

function changeParamSelectBoxInColumnCreator(hasChanged) {
    var value = hasChanged.val();
    var whichParam = hasChanged.attr("id").substring(5, 6);
    var inputToUpdate = $("#param" + whichParam + "RawValueTextBox");

    if (value === "RAWVAL") {
        inputToUpdate.removeAttr("disabled");
        inputToUpdate.val("");
    } else {
        inputToUpdate.attr("disabled", true);
        inputToUpdate.val("SELECT BLANK ABOVE");
    }
}

function clearColumnEditor() {
    $("#directColumnOption").prop("checked", true);
    $("#calculatedColumnOption").prop("checked", false);

    $("#overwriteNameTextBox").val("");
    $("#columnNameTextBox").val("");
    $("#param1RawValueTextBox").val("");
    $("#param2RawValueTextBox").val("");
    $("#historicDays").val("");
    $("#valueRange").val("");
}