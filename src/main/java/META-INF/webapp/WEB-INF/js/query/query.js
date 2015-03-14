function initNewQuery(name, template, timestamp, sheets) {
    var query = {};
    query.name = name;
    query.template = template;
    query.timestamp = timestamp;
    query.sheets = sheets;
    return query;
}

function initNewSheet(name, hidden, headers) {
    var sheet = {};
    sheet.name = name;
    sheet.hidden = hidden;
    sheet.headers = headers;

    return sheet;
}

function initNewHeader(name, columns) {
    var header = {};
    header.name = name;
    header.columns = columns;
    return header;
}

function initNewColumn(name, rule){
    var column = {};
    column.name = name;
    column.rule = rule;
    return column;
}

function addColumnToHeader(query, sheetIndex, headerIndex, column) {
    query.sheets[sheetIndex].headers[headerIndex].columns.push(column);
}

function createColumn() {
    if ($("#directColumnOption").is(":checked")) {
        return createDirectColumn();
    } else {
        return createCacheColumn();
    }
}

function shiftColumnUp(query, sheetIndex, headerIndex, colIndex) {
    var temp = query.sheets[sheetIndex].headers[headerIndex].columns[colIndex - 1];
    query.sheets[sheetIndex].headers[headerIndex].columns[colIndex - 1] =
        query.sheets[sheetIndex].headers[headerIndex].columns[colIndex];
    query.sheets[sheetIndex].headers[headerIndex].columns[colIndex] = temp;
}

function shiftColumnDown(query, sheetIndex, headerIndex, colIndex) {
    var temp = query.sheets[sheetIndex].headers[headerIndex].columns[colIndex + 1];
    query.sheets[sheetIndex].headers[headerIndex].columns[colIndex + 1] =
        query.sheets[sheetIndex].headers[headerIndex].columns[colIndex];
    query.sheets[sheetIndex].headers[headerIndex].columns[colIndex] = temp;
}

function getNumberOfColumns(query, sheetIndex, headerIndex) {
    return query.sheets[sheetIndex].headers[headerIndex].columns.length;
}

function getNumberOfHeaders(query, sheetIndex) {
    return query.sheets[sheetIndex].headers.length;
}

function removeColumn(query, sheetIndex, headerIndex, columnIndex) {
    query.sheets[sheetIndex].headers[headerIndex].columns.splice(columnIndex, 1);
}

function editColumn(query, sheetIndex, headerIndex, columnIndex) {
    query.sheets[sheetIndex].headers[headerIndex].columns[columnIndex] = createColumn();
}

function createDirectColumn() {
    var value = $("#directColumnList").val();
    var override = $("#overwriteNameTextBox").val();

    if (override.trim() === "") {
        return initNewColumn(value, "");
    } else {
        return initNewColumn(override, value);
    }
}

function createCacheColumn() {
    var name = $("#columnNameTextBox").val();
    var operator = $("#operatorSelect").val();

    if (operator === "h") {
        var firstParam = $("#param1SelectColumn").val();
        var secondParam = $("#historicDays").val();
        var rule = "~~" + firstParam + "~~ h ~~" + secondParam + "~~";
        return initNewColumn(name, rule);
    }

    var firstSelect = $("#param1SelectColumn").val();
    var firstRV = $("#param1RawValueTextBox").val();

    var rule = "~~";
    if (firstSelect === "RAWVAL") {
        rule += "##" + firstRV;
    } else {
        rule += firstSelect;
    }
    rule += "~~ " + operator + " ~~";

    if (operator === "a" || operator === "s" || operator === "m" || operator === "d" || operator === "c") {
        var secondSelect = $("#param2SelectColumn").val();
        var secondRV = $("#param2RawValueTextBox").val();

        if (secondSelect === "RAWVAL") {
            rule += "##" + secondRV + "~~";
        } else {
            rule += secondSelect + "~~";
        }
        return initNewColumn(name, rule);
    } else { //operator === av or ag
        var secondParam = $("#valueRange").val();
        rule += secondParam + "~~";
        return initNewColumn(name, rule);
    }
}