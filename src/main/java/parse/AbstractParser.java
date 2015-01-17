package parse;

import com.google.common.collect.Lists;
import deal.DealProperty;
import mapping.Mapping;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by samuelsmith on 01/11/2014.
 */
public abstract class AbstractParser implements SheetParser {

    private final Logger logger = LoggerFactory.getLogger(AbstractParser.class);

    private final FormulaEvaluator evaluator;
    public final DateTime timestamp;

    public final Mapping mapping;

    public AbstractParser(FormulaEvaluator evaluator, DateTime timestamp, Mapping mapping) {
        this.evaluator = evaluator;
        this.timestamp = timestamp;
        this.mapping = mapping;
    }

    public DealProperty parseCell(String header, Cell cell) {

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
                    String tmp = cell.getStringCellValue();
                    tmp = tmp.replaceAll("[\n\r]", "");
                    if (header != null) tmp = mapping.getMapping(header, tmp).getValue();
                    val = new DealProperty.Value(tmp, DealProperty.Value.ValueType.STRING);
                    retDPB = retDPB.withValue(timestamp, val);
                    return retDPB.build();
                case Cell.CELL_TYPE_FORMULA:
                    return parseCell(header, evaluator.evaluateInCell(cell));
            }
        }

        return null;
    }

    public List<String> getHeaders(Row headerRow, boolean isEverest) {
        logger.info("Getting header row");
        List<String> retList = Lists.newArrayList();

        int count = isEverest ? 0 : 1; //ignore 0 as first col is just a count
        while (count < 99) {
            Cell currentCell = headerRow.getCell(count);

            if (currentCell == null) break;

            DealProperty currentValue = parseCell(null, currentCell);

            if (currentValue.getLatestValue().type != DealProperty.Value.ValueType.BLANK)
                retList.add((String) currentValue.getLatestValue().innerValue);
            else break;

            count++;
        }

        return retList;
    }

}
