package parse;

import com.google.common.collect.Maps;
import deal.Deal;
import deal.DealProperty;
import mapping.Mapping;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by samuelsmith on 08/12/2014.
 */
public class EverestParser extends AbstractParser {

    private final Logger logger = LoggerFactory.getLogger(EverestParser.class);

    public final Sheet sheet;

    public EverestParser(Workbook workbook, DateTime timestamp, Mapping mapping) {
        super(workbook.getCreationHelper().createFormulaEvaluator(), timestamp, mapping);

        logger.info("Creating Everest parser for workbook: " + workbook);

        this.sheet = workbook.getSheetAt(0);
    }

    @Override
    public Map<String, Deal> parse() throws ParserException {
        logger.info("Parsing workbook");

        Map<String, Deal> parsedDeals = Maps.newHashMap();

        int headerRowIndex = getHeaderRowIndex(sheet);
        if (headerRowIndex == -1) throw new ParserException("Unable to find header row in sheet: " + sheet);

        int startingColIndex = getStartingColIndex(sheet, headerRowIndex);
        if (startingColIndex == -1) throw new ParserException("Unable to find starting column index in sheet: "
                + sheet);

        Row headerRow = sheet.getRow(headerRowIndex);

        List<String> headers = getHeaders(headerRow);

        int rCount = headerRowIndex + 1;
        Row currentRow;

        while (rCount < 99) {
            currentRow = sheet.getRow(rCount);

            if (currentRow == null) break;

            DealProperty firstDP = parseCell(null, currentRow.getCell(startingColIndex - 1));
            if (firstDP == null)
                if (parsedDeals.size() > 0) break;
                else continue;
            if (firstDP.getLatestValue().type == DealProperty.Value.ValueType.BLANK)
                if (parsedDeals.size() > 0) break;
                else continue;

            Map<String, DealProperty> dealProperties = Maps.newHashMap();
            String opportunity = null;

            for (int cCount = startingColIndex; cCount < headers.size() + startingColIndex; cCount++) {
                String header = headers.get(cCount - startingColIndex);
                String mappedHeader = mapping.getHeaderMapping(header);
                Cell currentCell = currentRow.getCell(cCount);
                DealProperty currentVal = parseCell(mappedHeader, currentCell);

                if (mappedHeader.equals("Company"))
                    opportunity = (String) currentVal.getLatestValue().innerValue;

                dealProperties.put(mappedHeader, currentVal);
            }

            Deal currentDeal = new Deal(dealProperties);

            parsedDeals.put(opportunity, currentDeal);
            rCount++;
        }

        logger.info("Parsed from workbook: " + parsedDeals);
        return parsedDeals;
    }


}
