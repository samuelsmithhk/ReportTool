package parse;

import com.google.common.collect.Lists;
import deal.Deal;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;


/**
 * Created by samuelsmith on 28/10/2014.
 */
public class DCParserTest {

    DCParser parser;

    @Before
    public void init() throws IOException, InvalidFormatException {

        InputStream input = this.getClass().getResourceAsStream("/testFiles/dcTestSheet.xlsx");
        Workbook wb = new XSSFWorkbook(input);
        parser = new DCParser(wb);
    }

    @Test
    public void shouldReturnCorrectHeaders() {
       String h1 = "Deal Code Name", h2 = "Company";
       List<String> actual = parser.getHeaders(parser.sheet.getRow(4));

       assertThat(actual, containsInAnyOrder(h1, h2));
    }

    //parsing issue, may be because sheet was saved using pages not excel, need to research
     @Test
     public void shouldGenerateActualDeals() {

         Deal e1 = new Deal("Project PE - AA1", null);
         Deal e2 = new Deal("Project PE - AA2", null);

         List<Deal> expected = Lists.newArrayListWithExpectedSize(2);

         expected.add(e1);
         expected.add(e2);

         List<Deal> actual = parser.parse();

         boolean firstEqual = e1.isEqual(actual.get(0));
         boolean secondEqual = e2.isEqual(actual.get(1));

         //TODO: improve this test so that it works in any order, (probably when we have cache implemented)
         //TODO: improve so that it also tests dealProperties

         assert(firstEqual == true && secondEqual == true);
     }
}
