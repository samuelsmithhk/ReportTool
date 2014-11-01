package parse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import deal.Deal;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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

         Map<String, String> e1dp = Maps.newHashMap(), e2dp = Maps.newHashMap();

         e1dp.put("Deal Code Name", "Deal Code - Project PE - AA1");
         e2dp.put("Deal Code Name", "Deal Code - Project PE - AA2");

         Deal e1 = new Deal("Project PE - AA1", e1dp), e2 = new Deal("Project PE - AA2", e2dp);

         List<Deal> expected = Lists.newArrayList();
         expected.add(e1);
         expected.add(e2);

         List<Deal> actual = parser.parse();

         assert(dealsEqualInAnyOrder(expected, actual));
     }

    public boolean dealsEqualInAnyOrder(List<Deal> a, List<Deal> b) {

        if (a.size() != b.size()) return false;

        Map<Integer, Integer> comparMap = Maps.newHashMap();

        for (int i = 0; i < a.size(); i++) {
            Deal da = a.get(i), db = b.get(i);
            int ua = da.almostUniqueCode(), ub = db.almostUniqueCode();

            if (!(comparMap.containsKey(ua))) comparMap.put(ua, 1);
            else comparMap.put(ua, comparMap.get(ua) + 1);

            if (!(comparMap.containsKey(ub))) comparMap.put(ub, -1);
            else comparMap.put(ub, comparMap.get(ub) - 1);
        }

        Set<Integer> res = new HashSet<Integer>(comparMap.values());

        if (res.size() != 1) return false;
        if (!(res.contains(0))) return false;

        return true;
    }
}
