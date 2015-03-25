function initNewQuery(name, template, timestamp, sheets, templateFile) {
    var query = {};
    query.name = name;
    query.template = template;
    query.templateFile = templateFile;
    query.timestamp = timestamp;
    query.sheets = sheets;
    return query;
}

function initNewSheet(name, hidden, headers, filterColumn, filterValue, sortBy, groupBy) {
    var sheet = {};
    sheet.name = name;
    sheet.hidden = hidden;
    sheet.headers = headers;
    sheet.filterColumn = filterColumn;
    sheet.filterValue = filterValue;
    sheet.sortBy = sortBy;
    sheet.groupBy = groupBy;

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

function shiftHeaderUp(query, sheetIndex, headerIndex) {
    var temp = query.sheets[sheetIndex].headers[headerIndex - 1];
    query.sheets[sheetIndex].headers[headerIndex - 1] = query.sheets[sheetIndex].headers[headerIndex];
    query.sheets[sheetIndex].headers[headerIndex] = temp;
}

function shiftHeaderDown(query, sheetIndex, headerIndex) {
    var temp = query.sheets[sheetIndex].headers[headerIndex + 1];
    query.sheets[sheetIndex].headers[headerIndex + 1] = query.sheets[sheetIndex].headers[headerIndex];
    query.sheets[sheetIndex].headers[headerIndex] = temp;
}

function moveSheetLeft(query, sheetIndex) {
    var temp = query.sheets[sheetIndex - 1];
    query.sheets[sheetIndex - 1] = query.sheets[sheetIndex];
    query.sheets[sheetIndex] = temp;
}

function moveSheetRight(query, sheetIndex) {
    var temp = query.sheets[sheetIndex + 1];
    query.sheets[sheetIndex + 1] = query.sheets[sheetIndex];
    query.sheets[sheetIndex] = temp;
}

function getNumberOfColumns(query, sheetIndex, headerIndex) {
    return query.sheets[sheetIndex].headers[headerIndex].columns.length;
}

function getNumberOfHeaders(query, sheetIndex) {
    return query.sheets[sheetIndex].headers.length;
}

function getNumberOfSheets(query) {
    return query.sheets.length;
}

function removeColumn(query, sheetIndex, headerIndex, columnIndex) {
    query.sheets[sheetIndex].headers[headerIndex].columns.splice(columnIndex, 1);
}

function removeHeader(query, sheetIndex, headerIndex) {
    query.sheets[sheetIndex].headers.splice(headerIndex, 1);
}

function removeSheet(query, sheetIndex) {
    query.sheets.splice(sheetIndex, 1);
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

function convertQueryObject(toBeConverted) {
    var retQuery = {};
    retQuery.queryName = toBeConverted.name;
    retQuery.sheets = [];
    retQuery.mappedColumns = [];
    retQuery.calculatedColumns = [];

    retQuery.outputTimestamp = toBeConverted.timestamp;
    if (toBeConverted.template) {
        retQuery.template = toBeConverted.templateFile;
    }

    $.each(toBeConverted.sheets, function(sheetIndex, sheet) {
        var convertedSheet = {};
        convertedSheet.sheetName = sheet.name;
        convertedSheet.hidden = sheet.hidden;
        convertedSheet.headers = [];
        convertedSheet.headerGroups = [];

        $.each(sheet.headers, function(headerIndex, header) {
            convertedSheet.headers.push(header.name);
            var convertedHeaderGroup = [];
            $.each(header.columns, function(columnIndex, column){
                if (column.rule.trim() === "") {
                    convertedHeaderGroup.push(column.name)
                } else if (column.rule.indexOf("~~") == -1) {
                    var mappedColumn = {};
                    mappedColumn.reference = "mc" + retQuery.mappedColumns.length;
                    mappedColumn.original = column.rule;
                    mappedColumn.header = column.name;

                    retQuery.mappedColumns.push(mappedColumn);
                    convertedHeaderGroup.push("$" + mappedColumn.reference);
                } else {
                    var calculatedColumn = {};
                    calculatedColumn.reference = "cc" + retQuery.calculatedColumns.length;
                    calculatedColumn.header = column.name;
                    calculatedColumn.condition = {};

                    var rule = column.rule;
                    rule = rule.replace("~~", "");

                    var pos = rule.indexOf("~~");
                    calculatedColumn.condition.firstHalf = rule.substring(0, pos).trim();
                    rule = rule.substring(pos);

                    rule = rule.replace("~~", "");

                    var pos = rule.indexOf("~~");
                    var operator = rule.substring(0, pos).trim();

                    if (operator === "h") {
                        calculatedColumn.condition.operator = "HISTORIC";
                    } else if (operator === "a") {
                        calculatedColumn.condition.operator = "ADD";
                    } else if (operator === "s") {
                        calculatedColumn.condition.operator = "SUBTRACT";
                    } else if (operator === "m") {
                        calculatedColumn.condition.operator = "MULTIPLY";
                    } else if (operator === "d") {
                        calculatedColumn.condition.operator = "DIVIDE";
                    } else if (operator === "c") {
                        calculatedColumn.condition.operator = "CONCAT";
                    } else if (operator === "av") {
                        calculatedColumn.condition.operator = "AVERAGE";
                    } else if (operator === "ag") {
                        calculatedColumn.condition.operator = "AGGREGATE";
                    }
                    rule = rule.substring(pos + 2);

                    var pos = rule.indexOf("~~");
                    calculatedColumn.condition.secondHalf = rule.substring(0, pos).trim();

                    retQuery.calculatedColumns.push(calculatedColumn);
                    convertedHeaderGroup.push("=" + calculatedColumn.reference);
                }
            });
            convertedSheet.headerGroups.push(convertedHeaderGroup);
        });

        convertedSheet.filterColumn = sheet.filterColumn;
        convertedSheet.filterValue = sheet.filterValue;
        convertedSheet.groupBy = sheet.groupBy;
        convertedSheet.sortBy = sheet.sortBy;

        retQuery.sheets.push(convertedSheet);
    });

    return retQuery;
}

function convertQueryObjectToUI(toBeConverted) {
    var retQuery = {};

    retQuery.name = toBeConverted.queryName;

    if (toBeConverted.outputTimestamp === "false") {
        retQuery.timestamp = false;
    } else if (toBeConverted.outputTimestamp === "true") {
        retQuery.timestamp = true;
    } else {
        retQuery.timestamp = toBeConverted.outputTimestamp;
    }

    retQuery.sheets = [];

    retQuery.templateFile = toBeConverted.template;
    if (typeof retQuery.templateFile === "undefined" || retQuery.templateFile.trim() === ""
            || retQuery.templateFile.trim() === "null") {
        retQuery.template = false;
    } else {
        retQuery.template = true;
    }

    $.each(toBeConverted.sheets, function(sheetIndex, sheet) {
        var convertedSheet = {};
        convertedSheet.name = sheet.sheetName;
        convertedSheet.filterColumn = sheet.filterColumn;
        convertedSheet.filterValue = sheet.filterValue;
        convertedSheet.groupBy = sheet.groupBy;
        convertedSheet.sortBy = sheet.sortBy;
        convertedSheet.headers = [];

        if (sheet.hidden === "true") {
            convertedSheet.hidden = true;
        } else if (sheet.hidden === "false") {
            convertedSheet.hidden = false;
        } else {
            convertedSheet.hidden = sheet.hidden;
        }

        $.each(sheet.headers, function(headerIndex, header){
            var convertedHeader = {};
            convertedHeader.name = header;
            convertedHeader.columns = [];

            $.each(sheet.headerGroups[headerIndex], function(columnIndex, column) {
                var convertedColumn = processColumn(toBeConverted, column);
                convertedHeader.columns.push(convertedColumn);
            });

            convertedSheet.headers.push(convertedHeader);
        });

        retQuery.sheets.push(convertedSheet);
    });

    return retQuery;
}

function processColumn(query, column) {
    if (column.indexOf("=") == 0) {
        return processCalculatedColumn(query, column);
    } else if (column.indexOf("$") == 0) {
        return processMappedColumn(query, column);
    } else {
        var retColumn = {};
        retColumn.name = column;
        retColumn.rule = "";
        return retColumn;
    }
}

function processCalculatedColumn(query, column) {
    var retColumn = {};

    $.each(query.calculatedColumns, function(ccI, cc){
        if (cc.reference === column.substring(1)) {
            retColumn.name = cc.header;
            retColumn.rule = processRule(cc.condition);
            return retColumn;
        }
    });

    return retColumn;
}

function processRule(condition) {
    var rule = "~~" + condition.firstHalf + "~~ ";

    if (condition.operator === "HISTORIC") {
        rule += "h ~~" + condition.secondHalf + "~~";
    } else if (condition.operator === "ADD") {
        rule += "a ~~" + condition.secondHalf + "~~";
    } else if (condition.operator === "SUBTRACT") {
        rule += "s ~~" + condition.secondHalf + "~~";
    } else if (condition.operator === "MULTIPLY") {
        rule += "m ~~" + condition.secondHalf + "~~";
    } else if (condition.operator === "DIVIDE") {
        rule += "d ~~" + condition.secondHalf + "~~";
    } else if (condition.operator === "CONCAT") {
        rule += "c ~~" + condition.secondHalf + "~~";
    } else if (condition.operator === "AVERAGE") {
        rule += "av ~~" + condition.secondHalf + "~~";
    } else if (condition.operator === "AGGREGATE") {
        rule += "ag ~~" + condition.secondHalf + "~~";
    }

    return rule;
}

function processMappedColumn(query, column) {
    var retColumn = {};

    $.each(query.mappedColumns, function(mcI, mc) {
        if (mc.reference === column.substring(1)) {
            retColumn.name = mc.header;
            retColumn.rule = mc.original;
            return retColumn;
        }
    });

    return retColumn;
}