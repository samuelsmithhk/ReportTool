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

    Mapping headerMap;

    public EverestParser(Workbook workbook, DateTime timestamp, Mapping headerMap) {
        super(workbook.getCreationHelper().createFormulaEvaluator(), timestamp);

        logger.info("Creating Everest parser for workbook: " + workbook);

        this.sheet = workbook.getSheetAt(0);
        this.headerMap = headerMap;
    }

    @Override
    public Map<String, Deal> parse() {
        logger.info("Parsing workbook");

        Map<String, Deal> parsedDeals = Maps.newHashMap();

        Row headerRow = sheet.getRow(0);
        List<String> headers = getHeaders(headerRow, true);

        int rCount = 1;

        while (rCount < 99) {
            logger.info("Parsing row: " + rCount);

            Row currentRow = sheet.getRow(rCount);

            DealProperty firstDP = parseCell(currentRow.getCell(0));
            if (firstDP == null) break;
            if (firstDP.getLatestValue().type == DealProperty.Value.ValueType.BLANK) break;

            if (!(isGroupHeaderRow(currentRow))) {

                Map<String, DealProperty> dealProperties = Maps.newHashMap();
                String opportunity = null;

                for (int cCount = 0; cCount < headers.size(); cCount++) {
                    Cell currentCell = currentRow.getCell(cCount);
                    DealProperty currentVal = parseCell(currentCell);

                    String header = headers.get(cCount);

                    if (header.equals("Opportunity"))
                        opportunity = (String) currentVal.getLatestValue().innerValue;

                    String mappedHeader = headerMap.getMapping(header);

                    if (mappedHeader.equals("Country"))
                        currentVal = mappedCountry(currentVal);

                    dealProperties.put(mappedHeader, currentVal);

                }

                Deal currentDeal = new Deal(dealProperties);

                parsedDeals.put(opportunity, currentDeal);
            }
            rCount++;
        }

        return parsedDeals;
    }


    public DealProperty mappedCountry(DealProperty toMap) {
        String preMap = (String) toMap.getLatestValue().innerValue;
        String mapped = null;

        if (preMap.contains("China")) mapped = "China";
        else if (preMap.contains("Thailand")) mapped = "SE Asia";
        else if (preMap.contains("Australia")) mapped = "Australia";

        if (mapped == null)
            return toMap;

        DealProperty.Value newValue = new DealProperty.Value(mapped, DealProperty.Value.ValueType.STRING);

        DealProperty.DealPropertyBuilder dpb = new DealProperty.DealPropertyBuilder();
        DealProperty newDP = dpb.withValue(timestamp, newValue).build();

        return newDP;
    }

    public boolean isGroupHeaderRow(Row row) {

        Cell testCell = row.getCell(1);
        DealProperty testDP = parseCell(testCell);

        if (testDP.getLatestValue().type == DealProperty.Value.ValueType.BLANK) {
            return true;
        }

        return false;
    }
}
