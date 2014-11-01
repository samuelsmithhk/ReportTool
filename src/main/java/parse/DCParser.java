package parse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import deal.Deal;
import deal.DealProperty;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.DateTime;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Created by samuelsmith on 28/10/2014.
 */
public class DCParser extends AbstractParser {

    public final Sheet sheet;

    public DCParser(Workbook workbook, DateTime timestamp) {
        super(workbook.getCreationHelper().createFormulaEvaluator(), timestamp);
        this.sheet = workbook.getSheetAt(0);
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

            DealProperty firstDP = parseCell(currentRow.getCell(0));
            if (firstDP.getLatestValue().type == DealProperty.Value.ValueType.BLANK) break;

            Map<String, DealProperty> dealProperties = Maps.newHashMap();
            String opportunity = null;

            for (int cCount = 1; cCount <= headers.size(); cCount++) {
                Cell currentCell = currentRow.getCell(cCount);
                DealProperty currentVal = parseCell(currentCell);

                if (headers.get(cCount - 1).equals("Company"))
                    opportunity = (String) currentVal.getLatestValue().innerValue;
                else dealProperties.put(headers.get(cCount - 1), currentVal);
            }

            Deal currentDeal = new Deal(opportunity, dealProperties);

            deals.add(currentDeal);
            rCount++;
        }

        return deals;
    }

    public List<String> getHeaders(Row headerRow) {
        List<String> retList = Lists.newArrayList();
        boolean end = false;

        int count = 1; //ignore 0 as first col is just a count
        while (count < 99) {
            Cell currentCell = headerRow.getCell(count);
            DealProperty currentValue = parseCell(currentCell);

            if (currentValue.getLatestValue().type != DealProperty.Value.ValueType.BLANK)
                retList.add((String) currentValue.getLatestValue().innerValue);
            else break;

            count++;
        }

        return retList;
    }
}
