package parse;

import com.google.common.collect.Maps;
import deal.Deal;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

/**
 * Created by samuelsmith on 28/10/2014.
 */
public class DCParserTest {

    DCParser parser;

    @Before
    public void init() throws IOException, InvalidFormatException {

        InputStream input = this.getClass().getResourceAsStream("/testFiles/dcTestSheet.xlsx");
        Workbook wb = new XSSFWorkbook(input);
        Sheet sheet = wb.getSheetAt(0);
        parser = new DCParser(sheet);
    }

    @Test
    public void shouldReturnCorrectHeaders() {
       String h1 = "Deal Code Name", h2 = "Company";
       List<String> actual = parser.getHeaders(parser.sheet.getRow(4));

       assertThat(actual, containsInAnyOrder(h1, h2));
    }

    //parsing issue, may be because sheet was saved using pages not excel, need to research
 //   @Test
 //   public void shouldGenerateActualDeals() {
 //       List<Deal> actual = parser.parse();

 //       assertThat(actual, containsInAnyOrder(hasProperty("opportunity", equalTo("Project PE - AA1")),
 //               hasProperty("opportunity", equalTo("Project PE - AA2"))));
 //   }
}
