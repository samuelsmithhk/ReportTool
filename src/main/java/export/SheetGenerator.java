package export;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import query.Group;
import query.QueryResult;
import query.QueryResultDeal;

import java.util.List;
import java.util.Set;

/**
 * Created by samuelsmith on 16/11/2014.
 */
public class SheetGenerator {

    public static Workbook basicSheetGenerator(QueryResult deals) {
        Workbook retWB = new XSSFWorkbook();

        Sheet sheet = retWB.createSheet("Results");
        sheet.setAutobreaks(true);

        Set<String> headers = GeneratorUtils.getHeadersFromQueryResult(deals);
        sheet.setDefaultColumnWidth(headers.size());

        Row headerRow = sheet.createRow(0);
        int n = 0;
        for (String header : headers) { headerRow.createCell(n).setCellValue(header); n++; }

        return retWB;
    }

    private static class GeneratorUtils {

        static Set<String> getHeadersFromQueryResult(QueryResult deals) {
            List<Group> values = deals.valuesGrouped;
            List<QueryResultDeal> results = values.get(0).groupValues;
            QueryResultDeal result = results.get(0);
            return result.dealProperties.keySet();
        }

    }

}
