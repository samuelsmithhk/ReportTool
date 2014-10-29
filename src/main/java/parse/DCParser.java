package parse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import deal.Deal;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Created by samuelsmith on 28/10/2014.
 */
public class DCParser implements SheetParser {

    public final Sheet sheet;

    public DCParser(Sheet sheet) {
        this.sheet = sheet;
    }

    @Override
    public List<Deal> parse() {
        List<Deal> deals = Lists.newArrayList();

        Row headerRow = sheet.getRow(4);

        List<String> headers = getHeaders(headerRow);

        int rCount = 5;
        Row currentRow = null;

        while (rCount < 99) {
            if (currentRow != null) {
                Cell firstCell = currentRow.getCell(0);
                if (firstCell.getStringCellValue().trim().equals("")) break;
            }

            Map<String, String> dealProperties = Maps.newHashMap();
            String opportunity = null;

            currentRow = sheet.getRow(rCount);

            for (int cCount = 1; cCount < headers.size(); cCount++) {
                Cell currentCell = currentRow.getCell(cCount);
                String currentVal = currentCell.getRichStringCellValue().getString().trim();

                if (headers.get(cCount).equals("Company")) opportunity = currentVal;
                else dealProperties.put(headers.get(cCount), currentVal);
            }

            Deal currentDeal = new Deal(opportunity, dealProperties);

            System.out.println(currentDeal);

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
            String currentValue = currentCell.getStringCellValue().trim();

            if (!currentValue.equals("")) retList.add(currentValue);
            else break;

            count++;
        }

        return retList;
    }
}
