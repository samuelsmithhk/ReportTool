package cache;

import com.google.common.collect.Maps;
import deal.Deal;
import deal.DealProperty;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by samuelsmith on 02/11/2014.
 */
public class CacheTest {

    @Test
    public void shouldParseCacheToJson() {
        Map<String, Deal> toBeParsed = Maps.newHashMap();

        DateTime time = new DateTime(2014, 10, 10, 10, 10);
        DealProperty.Value
                value1 = new DealProperty.Value("Deal Code - Project PE - AA1", DealProperty.Value.ValueType.STRING),
                value2 = new DealProperty.Value("Deal Code - Project PE - AA2", DealProperty.Value.ValueType.STRING);

        DealProperty.DealPropertyBuilder dpb1 = new DealProperty.DealPropertyBuilder(),
                dpb2 = new DealProperty.DealPropertyBuilder();
        DealProperty dp1 = dpb1.withValue(time, value1).build();
        DealProperty dp2 = dpb2.withValue(time, value2).build();

        Map<String, DealProperty> map1 = Maps.newHashMap(), map2 = Maps.newHashMap();

        map1.put("Deal Code Name", dp1);
        map2.put("Deal Code Name", dp2);

        Deal deal1 = new Deal(map1), deal2 = new Deal(map2);

        toBeParsed.put("Project PE - AA1", deal1);
        toBeParsed.put("Project PE - AA2", deal2);

        String expected =
                "{\"Project PE - AA2\":{\"dealProperties\":{\"Deal Code Name\":{\"values\":{\"2014-10-10T10:10:00.000+08:00\":{\"innerValue\":\"Deal Code - Project PE - AA2\",\"type\":\"STRING\"}}}}},\"Project PE - AA1\":{\"dealProperties\":{\"Deal Code Name\":{\"values\":{\"2014-10-10T10:10:00.000+08:00\":{\"innerValue\":\"Deal Code - Project PE - AA1\",\"type\":\"STRING\"}}}}}}";
        String actual = Cache.serializeCache(toBeParsed);

        Assert.assertTrue(actual.equals(expected));
    }

    @Test
    public void shouldParseJsonToCache() {
        String json =
                "{\"Project PE - AA2\":{\"dealProperties\":{\"Deal Code Name\":{\"values\":{\"2014-10-10T10:10:00.000+08:00\":{\"innerValue\":\"Deal Code - Project PE - AA2\",\"type\":\"STRING\"}}}}},\"Project PE - AA1\":{\"dealProperties\":{\"Deal Code Name\":{\"values\":{\"2014-10-10T10:10:00.000+08:00\":{\"innerValue\":\"Deal Code - Project PE - AA1\",\"type\":\"STRING\"}}}}}}";

        String actual = String.valueOf(Cache.deserializeCache(json));
        String expected =
                "{Project PE - AA2= Deal properties : {Deal Code Name={2014-10-10T10:10:00.000+08:00=Deal Code - Project PE - AA2 (type: STRING)}}, Project PE - AA1= Deal properties : {Deal Code Name={2014-10-10T10:10:00.000+08:00=Deal Code - Project PE - AA1 (type: STRING)}}}";

        Assert.assertTrue(actual.equals(expected));


    }
}
