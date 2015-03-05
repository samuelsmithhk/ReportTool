package parse;

import com.google.common.collect.Lists;
import deal.DealProperty;
import mapping.Mapping;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


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
        DealProperty.DealPropertyBuilder retDPB = new DealProperty.DealPropertyBuilder();
        DealProperty.Value<java.io.Serializable> val;

        if (cell == null) {
            val = new DealProperty.Value<java.io.Serializable>(null, DealProperty.Value.ValueType.BLANK);
            retDPB = retDPB.withValue(timestamp, val);
            return retDPB.build();
        }

        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BOOLEAN:
                val = new DealProperty.Value<java.io.Serializable>(cell.getBooleanCellValue(),
                        DealProperty.Value.ValueType.BOOLEAN);
                retDPB = retDPB.withValue(timestamp, val);
                return retDPB.build();
            case Cell.CELL_TYPE_BLANK:
                val = new DealProperty.Value<java.io.Serializable>(null, DealProperty.Value.ValueType.BLANK);
                retDPB = retDPB.withValue(timestamp, val);
                return retDPB.build();
            case Cell.CELL_TYPE_ERROR:
                val = new DealProperty.Value<java.io.Serializable>("ERROR", DealProperty.Value.ValueType.STRING);
                retDPB = retDPB.withValue(timestamp, val);
                return retDPB.build();
            case Cell.CELL_TYPE_NUMERIC:
                val = new DealProperty.Value<java.io.Serializable>(cell.getNumericCellValue(),
                        DealProperty.Value.ValueType.NUMERIC);
                retDPB = retDPB.withValue(timestamp, val);
                return retDPB.build();
            case Cell.CELL_TYPE_STRING:
                String tmp = cell.getStringCellValue();
                tmp = tmp.replaceAll("[\n\r\\xA0]", "").trim();
                if (header != null) tmp = mapping.getMapping(header, tmp).getValue();
                val = new DealProperty.Value<java.io.Serializable>(tmp, DealProperty.Value.ValueType.STRING);
                retDPB = retDPB.withValue(timestamp, val);
                return retDPB.build();
            case Cell.CELL_TYPE_FORMULA:
                return parseCell(header, evaluator.evaluateInCell(cell));
        }

        return null; //should never reach
    }

    public List<String> getHeaders(Row headerRow) {
        logger.info("Getting header row");
        List<String> retList = Lists.newArrayList();

        int emptyCells = 0; //break after 3 empty cells in a row

        int count = 0;
        while (count < 200) {
            Cell currentCell = headerRow.getCell(count);
            count++;
            if (currentCell == null) continue;
            if (currentCell.getStringCellValue().equals("No") || currentCell.getStringCellValue().equals("#")) continue;

            DealProperty currentValue = parseCell(null, currentCell);

            if (currentValue.getLatestValue().type != DealProperty.Value.ValueType.BLANK) {
                retList.add((String) currentValue.getLatestValue().innerValue);
                emptyCells = 0;
            } else if (retList.size() > 0) {
                if (emptyCells == 2) {
                    break;
                }
                emptyCells++;
            }

        }

        return retList;
    }

    protected int getHeaderRowIndex(Sheet sheet) {
        logger.info("Finding header index");
        int row;
        for (row = 0; row < 100; row++) {
            if (getStartingColIndex(sheet, row) != -1)
                return row;
        }

        return -1;
    }

    protected int getStartingColIndex(Sheet sheet, int row) {
        logger.info("Finding starting col in row " + row);
        Row temp = sheet.getRow(row);


        for (int i = 0; i < 10; i++) {
            if (temp == null) continue;
            Cell c = temp.getCell(i);
            if (c == null) continue;
            String text = c.getStringCellValue();
            if (text == null) continue;
            if ((text.equals("#")) || (text.contains("No")) || (text.equals("InvId")))
                return c.getColumnIndex() + 1;
        }

        return -1;
    }

    protected class ParserException extends Exception {
        public ParserException(String e) {
            super(e);
        }
    }

}
