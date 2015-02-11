package parse;

import com.google.common.collect.Maps;
import deal.Deal;
import deal.DealProperty;
import mapping.Mapping;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by samuelsmith on 28/10/2014.
 */
public class DCPipelineParser extends AbstractParser {

    private final Logger logger = LoggerFactory.getLogger(DCPipelineParser.class);

    public final Sheet sheet;

    public DCPipelineParser(Workbook workbook, DateTime timestamp, Mapping mapping) {
        super(workbook.getCreationHelper().createFormulaEvaluator(), timestamp, mapping);

        logger.info("Creating Deal Central Parser for workbook: " + workbook);

        this.sheet = workbook.getSheetAt(0);
    }

    @Override
    public Map<String, Deal> parse() {
        logger.info("Parsing workbook");

        Map<String, Deal> parsedDeals = Maps.newHashMap();

        int headerRowIndex = getHeaderRowIndex(sheet);

        Row headerRow = sheet.getRow(headerRowIndex);

        List<String> headers = getHeaders(headerRow);

        int rCount = headerRowIndex + 1;
        Row currentRow;

        while (rCount < 99) {
            logger.info("Parsing row " + rCount);

            currentRow = sheet.getRow(rCount);

            DealProperty firstDP = parseCell(null, currentRow.getCell(0));
            if (firstDP == null) break;
            if (firstDP.getLatestValue().type == DealProperty.Value.ValueType.BLANK) break;

            Map<String, DealProperty> dealProperties = Maps.newHashMap();

            DealProperty.DealPropertyBuilder dpb = new DealProperty.DealPropertyBuilder();
            dpb = dpb.withValue(timestamp, new DealProperty.Value("DC_PIPELINE", DealProperty.Value.ValueType.STRING));
            dealProperties.put("Source Type", dpb.build());

            String opportunity = null;

            for (int cCount = 0; cCount < headers.size(); cCount++) {
                String header = headers.get(cCount);
                String mappedHeader = mapping.getHeaderMapping(header);
                Cell currentCell = currentRow.getCell(cCount);
                DealProperty currentVal = parseCell(mappedHeader, currentCell);

                if (mappedHeader.equals("Company Name"))
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