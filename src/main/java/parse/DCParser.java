package parse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import deal.Deal;
import org.apache.poi.ss.usermodel.*;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Created by samuelsmith on 28/10/2014.
 */
public class DCParser implements SheetParser {

    public final Sheet sheet;
    private final FormulaEvaluator evaluator;

    public DCParser(Workbook workbook) {
        this.sheet = workbook.getSheetAt(0);
        this.evaluator = workbook.getCreationHelper().createFormulaEvaluator();
    }

    @Override
    public List<Deal> parse() {
        List<Deal> deals = Lists.newArrayList();

        Row headerRow = sheet.getRow(4);

        List<String> headers = getHeaders(headerRow);

        int rCount = 5;
        Row currentRow = null;

        while (rCount < 99) {
            currentRow = sheet.getRow(rCount);

            String firstValue = parseCell(currentRow.getCell(0));
            if (firstValue.equals("")) break;

            Map<String, String> dealProperties = Maps.newHashMap();
            String opportunity = null;

            for (int cCount = 1; cCount <= headers.size(); cCount++) {
                Cell currentCell = currentRow.getCell(cCount);
                String currentVal = parseCell(currentCell);

                if (headers.get(cCount - 1).equals("Company")) opportunity = currentVal;
                else dealProperties.put(headers.get(cCount), currentVal);
            }

            Deal currentDeal = new Deal(opportunity, dealProperties);

            System.out.println(currentDeal);

            deals.add(currentDeal);
            rCount++;
        }

        return deals;
    }

    public String parseCell(Cell cell) {

        if (cell != null) {
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_BOOLEAN :
                    return String.valueOf(cell.getBooleanCellValue());
                case Cell.CELL_TYPE_BLANK :
                    return "";
                case Cell.CELL_TYPE_ERROR :
                    return "ERROR";
                case Cell.CELL_TYPE_NUMERIC :
                    return String.valueOf(cell.getNumericCellValue());
                case Cell.CELL_TYPE_STRING :
                    return cell.getStringCellValue().trim();
                case Cell.CELL_TYPE_FORMULA :
                    return parseCell(evaluator.evaluateInCell(cell));
            }
        }

        return null;
    }

    public List<String> getHeaders(Row headerRow) {
        List<String> retList = Lists.newArrayList();
        boolean end = false;

        int count = 1; //ignore 0 as first col is just a count
        while (count < 99) {
            Cell currentCell = headerRow.getCell(count);
            String currentValue = parseCell(currentCell);

            if (!currentValue.equals("")) retList.add(currentValue);
            else break;

            count++;
        }

        return retList;
    }
}
