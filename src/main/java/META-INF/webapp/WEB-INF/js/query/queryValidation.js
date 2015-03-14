function validateSaveColumn() {
    if ($("#directColumnOption").is(":checked")) {
        return validateDirectColumnSave();
    } else { //columnType == calculatedColumnOption
        return validateCalculatedColumnSave();
    }
}

function validateDirectColumnSave() {
    var selectedColumn = $("#directColumnList").val();

    if (selectedColumn === "RAWVAL") {
        alert("Select a column");
        return false;
    }

    return true;
}

function validateCalculatedColumnSave() {
    var columnName = $("#columnNameTextBox").val();

    if (columnName.trim() === "") {
        alert("Insert column name");
        return false;
    }

    var operator = $("#operatorSelect").val();

    if (operator === "h") {
        var param1Val = $("#param1SelectColumn").val();

        if (param1Val === "RAWVAL") {
            alert("First parameter must be column for historic operator");
            return false;
        }

        var param2Val = $("#historicDays").val();

        if (!(/[0-9]+/.test(param2Val))) {
            alert("Second parameter must be numbers only for historic operator");
            return false;
        }

        return true;
    }

    var param1SelectVal = $("#param1SelectColumn").val();
    var param1RawVal = $("#param1RawValueTextBox").val();

    if (param1SelectVal === "RAWVAL" && param1RawVal.trim() === "") {
        alert("Enter a first parameter");
        return false;
    }

    if (operator === "a" || operator === "s" || operator === "m" || operator === "d" || operator === "c") {
        var param2SelectVal = $("#param2SelectColumn").val();
        var param2RawVal = $("#param2RawValueTextBox").val();

        if (param2SelectVal === "RAWVAL" && param2RawVal.trim() === "") {
            alert("Enter a second parameter");
            return false;
        }
        return true;
    } else {
        var rangeVal = $("#valueRange").val();

        if (rangeVal.trim() === "") {
            alert("Insert a second parameter");
            return false;
        }
    }

    return true;
}