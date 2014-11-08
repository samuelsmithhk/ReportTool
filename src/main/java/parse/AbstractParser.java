package parse;

import deal.DealProperty;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by samuelsmith on 01/11/2014.
 */
public abstract class AbstractParser implements SheetParser {

    private final Logger logger = LoggerFactory.getLogger(AbstractParser.class);

    private final FormulaEvaluator evaluator;
    public final DateTime timestamp;

    public AbstractParser(FormulaEvaluator evaluator, DateTime timestamp) {
        this.evaluator = evaluator;
        this.timestamp = timestamp;
    }

    public DealProperty parseCell(Cell cell) {

        logger.info("Parsing cell: " + cell);

        if (cell != null) {
            DealProperty.DealPropertyBuilder retDPB = new DealProperty.DealPropertyBuilder();
            DealProperty.Value val;

            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_BOOLEAN:
                    val = new DealProperty.Value(cell.getBooleanCellValue(), DealProperty.Value.ValueType.BOOLEAN);
                    retDPB = retDPB.withValue(timestamp, val);
                    return retDPB.build();
                case Cell.CELL_TYPE_BLANK:
                    val = new DealProperty.Value(null, DealProperty.Value.ValueType.BLANK);
                    retDPB = retDPB.withValue(timestamp, val);
                    return retDPB.build();
                case Cell.CELL_TYPE_ERROR:
                    val = new DealProperty.Value("ERROR", DealProperty.Value.ValueType.STRING);
                    retDPB = retDPB.withValue(timestamp, val);
                    return retDPB.build();
                case Cell.CELL_TYPE_NUMERIC:
                    val = new DealProperty.Value(cell.getNumericCellValue(), DealProperty.Value.ValueType.NUMERIC);
                    retDPB = retDPB.withValue(timestamp, val);
                    return retDPB.build();
                case Cell.CELL_TYPE_STRING:
                    val = new DealProperty.Value(cell.getStringCellValue(), DealProperty.Value.ValueType.STRING);
                    retDPB = retDPB.withValue(timestamp, val);
                    return retDPB.build();
                case Cell.CELL_TYPE_FORMULA:
                    return parseCell(evaluator.evaluateInCell(cell));
            }
        }

        return null;
    }

}
