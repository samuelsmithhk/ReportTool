package parse;

import com.google.common.collect.Maps;
import deal.Deal;
import deal.DealProperty;
import mapping.ICDateMapping;
import mapping.Mapping;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by samuelsmith on 08/02/2015.
 */
public class DCICSummaryParser extends AbstractParser {

    private final Logger logger = LoggerFactory.getLogger(DCICSummaryParser.class);

    private final Workbook workbook;

    private final ICDateMapping icdm;

    public DCICSummaryParser(Workbook workbook, DateTime timestamp, Mapping mapping, ICDateMapping icdm) {
        super(workbook.getCreationHelper().createFormulaEvaluator(), timestamp, mapping);

        this.workbook = workbook;
        this.icdm = icdm;
    }

    @Override
    public Map<String, Deal> parse() throws ParserException {
        logger.info("Parsing workbook");
        Map parsedDeals = Maps.newHashMap();

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet currentSheet = workbook.getSheetAt(i);
            String csName = currentSheet.getSheetName();

            if (!(csName.contains("Summary")))
                parsedDeals.putAll(parseSheet(workbook.getSheetAt(i)));
        }

        return parsedDeals;
    }

    private Map<String, Deal> parseSheet(Sheet sheet) {
        logger.info("Parsing sheet: " + sheet.getSheetName());
        Map parsedDeals = Maps.newHashMap();

        int headerRowIndex = getHeaderRowIndex(sheet);
        int firstColIndex = getStartingColIndex(sheet, headerRowIndex) - 1;

        Row headerRow = sheet.getRow(headerRowIndex);
        List<String> headers = getHeaders(headerRow);

        int rCount = headerRowIndex + 1;
        Row currentRow;

        while (rCount < 99) {
            logger.info("Parsing row " + rCount);

            currentRow = sheet.getRow(rCount);

            DealProperty firstDP = parseCell(null, currentRow.getCell(firstColIndex));
            if (firstDP == null) break;
            if (firstDP.getLatestValue().type == DealProperty.Value.ValueType.BLANK) break;

            Map<String, DealProperty> dealProperties = Maps.newHashMap();

            DealProperty.DealPropertyBuilder dpb = new DealProperty.DealPropertyBuilder();
            dpb = dpb.withValue(timestamp, new DealProperty.Value("DC_IC_SUMMARY", DealProperty.Value.ValueType.STRING));
            dealProperties.put("Source Type", dpb.build());

            String opportunity = null;

            boolean endOfRow = false;
            int cCount = firstColIndex;
            int headerIndex = 0;
            while ((!endOfRow) && (cCount < 50)) {
                Cell testCell = sheet.getRow(headerRowIndex).getCell(cCount);
                if ((testCell == null) || (testCell.getStringCellValue().equals(""))) {
                    cCount++;
                    continue;
                }

                //test the cell above to see if it has a part of the header
                String aboveHeader = "";
                Cell aboveCell = sheet.getRow(headerRowIndex - 1).getCell(cCount);
                DealProperty aboveDP = null;
                if ((aboveCell != null) && (!(aboveCell.getStringCellValue().equals(""))))
                    aboveDP = parseCell(null, aboveCell);
                if ((aboveDP != null) && (aboveDP.getLatestValue().type == DealProperty.Value.ValueType.STRING))
                    aboveHeader = aboveDP.getLatestValue().innerValue + " ";

                String header = aboveHeader + headers.get(headerIndex);
                headerIndex++;
                String mappedHeader = mapping.getHeaderMapping(header);
                Cell currentCell = currentRow.getCell(cCount);
                DealProperty currentVal = parseCell(mappedHeader, currentCell);

                if (mappedHeader.equals("Company Name"))
                    opportunity = (String) currentVal.getLatestValue().innerValue;

                dealProperties.put(mappedHeader, currentVal);

                if (mappedHeader.equals("Deal Code Name")) {
                    dealProperties.put("Date Shown on IC Report", getDateShowedMapping(currentVal));
                }

                if (headerIndex >= headers.size()) endOfRow = true;
                cCount++;
            }

            Deal currentDeal = new Deal(dealProperties);

            parsedDeals.put(opportunity, currentDeal);
            rCount++;
        }

        logger.info("Parsed from sheet: " + parsedDeals);

        return parsedDeals;
    }

    public DealProperty getDateShowedMapping(DealProperty val) {
        String result = icdm.getMapping(val.getLatestValue().innerValue.toString());
        DealProperty.DealPropertyBuilder dpb = new DealProperty.DealPropertyBuilder();
        dpb.withValue(timestamp, new DealProperty.Value(result, DealProperty.Value.ValueType.STRING));
        return dpb.build();
    }

}
