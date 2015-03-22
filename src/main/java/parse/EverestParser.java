package parse;

import com.google.common.collect.Maps;
import deal.Deal;
import deal.DealProperty;
import mapping.Mapping;
import mapping.CRGMapping;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class EverestParser extends AbstractParser {

    private final Logger logger = LoggerFactory.getLogger(EverestParser.class);
    private final CRGMapping cagMapping;

    public final Sheet sheet;

    public EverestParser(Workbook workbook, DateTime timestamp, Mapping mapping, CRGMapping cagMapping) {
        super(workbook.getCreationHelper().createFormulaEvaluator(), timestamp, mapping);

        logger.info("Creating Everest parser for workbook: " + workbook);

        this.sheet = workbook.getSheetAt(0);
        this.cagMapping = cagMapping;
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

        while (rCount < 999) {
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

            DealProperty.DealPropertyBuilder dpb = new DealProperty.DealPropertyBuilder();
            dpb = dpb.withValue(timestamp, new DealProperty.Value<String>("EVEREST", DealProperty.Value.ValueType.STRING));
            dealProperties.put("Source Type", dpb.build());

            String companyName = null;

            for (int cCount = startingColIndex; cCount < headers.size() + startingColIndex; cCount++) {
                String header = headers.get(cCount - startingColIndex);
                String mappedHeader = mapping.getHeaderMapping(header);
                Cell currentCell = currentRow.getCell(cCount);
                DealProperty currentVal = parseCell(mappedHeader, currentCell);

                if (mappedHeader.equals("Company Name"))
                    companyName = (String) currentVal.getLatestValue().innerValue;

                dealProperties.put(mappedHeader, currentVal);

                if (header.equals("Country of Incorporation")) {
                    dealProperties.putAll(getInternalMappings(currentVal));
                }
            }

            Deal currentDeal = new Deal(dealProperties);

            parsedDeals.put(companyName, currentDeal);
            rCount++;
        }

        return parsedDeals;
    }

    private Map<String, DealProperty> getInternalMappings(DealProperty val) {
        Map<String, DealProperty> retMap = Maps.newHashMap();

        String coi = val.getLatestValue().innerValue.toString();
        Map<String, String> result = cagMapping.getMapping(coi);

        if (mapping == null) return retMap;

        for (Map.Entry<String, String> entry : result.entrySet()) {
            DealProperty.DealPropertyBuilder dpb = new DealProperty.DealPropertyBuilder();
            DealProperty dp = dpb.withValue
                    (timestamp, new DealProperty.Value<String>(entry.getValue(),
                            DealProperty.Value.ValueType.STRING)).build();

            retMap.put(entry.getKey(), dp);
        }

        return retMap;
    }
}
