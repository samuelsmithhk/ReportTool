package parse;

import com.sun.xml.internal.ws.util.pipe.AbstractSchemaValidationTube;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

/**
 * Created by samuelsmith on 01/11/2014.
 */
public abstract class AbstractParser implements SheetParser {

    private final FormulaEvaluator evaluator;

    public AbstractParser(FormulaEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public String parseCell(Cell cell) {

        if (cell != null) {
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case Cell.CELL_TYPE_BLANK:
                    return "";
                case Cell.CELL_TYPE_ERROR:
                    return "ERROR";
                case Cell.CELL_TYPE_NUMERIC:
                    return String.valueOf(cell.getNumericCellValue());
                case Cell.CELL_TYPE_STRING:
                    return cell.getStringCellValue().trim();
                case Cell.CELL_TYPE_FORMULA:
                    return parseCell(evaluator.evaluateInCell(cell));
            }
        }

        return null;
    }

}
