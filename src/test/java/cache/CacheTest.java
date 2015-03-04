package cache;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import deal.Deal;
import deal.DealProperty;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by samuelsmith on 02/11/2014.
 */
public class CacheTest {

    private Cache createEmptyCache() {
         return Cache.createEmptyCache();
    }

    private Cache createLoadedCache() {
        return createLoadedCache(null);
    }

    private Cache createLoadedCache(DateTime now) {
        if (now == null) now = new DateTime();
        String cacheContents = "{\"deals\":{\"Project PE - AA2\":{\"dealProperties\":{\"Deal Code Name\":{\"values\":{\"2014-10-10T10:10:00.000+08:00\":{\"innerValue\":\"Deal Code - Project PE - AA2\",\"type\":\"STRING\"}}}}},\"Project PE - AA1\":{\"dealProperties\":{\"Deal Code Name\":{\"values\":{\"2014-10-10T10:10:00.000+08:00\":{\"innerValue\":\"Deal Code - Project PE - AA1\",\"type\":\"STRING\"}}}}}}, \"columnIndex\": [\"column1\", \"column2\", \"column3\"]}";
        return Cache.createLoadedCache(cacheContents, now);
    }

    @Test
    public void shouldCreateEmptyCache() {
        Cache emptyCache = createEmptyCache();
        boolean condition1 = emptyCache.getDeals().size() == 0;
        boolean condition2 = emptyCache.getCols().size() == 0;
        boolean condition3 = emptyCache.getLastUpdated() == null;

        Assert.assertTrue(condition1 && condition2 && condition3);
    }

    @Test
    public void shouldCreateLoadedCache() {
        DateTime now = new DateTime();
        Cache loadedCache = createLoadedCache(now);

        boolean condition1 = loadedCache.getDeals().size() != 0; //weak test
        boolean condition2 = (loadedCache.getCols().contains("column1") && (loadedCache.getCols().contains("column2"))
                && (loadedCache.getCols().contains("column3")));
        boolean condition3 = loadedCache.getLastUpdated().equals(now);

        Assert.assertTrue(condition1 && condition2 && condition3);
    }

    @Test(expected = Cache.CacheException.class)
    public void shouldThrowCacheErrorGettingDealFromEmptyCache() throws Cache.CacheException {
        Cache emptyCache = createEmptyCache();
        Deal deal = emptyCache.getDeal("empty");
    }

    @Test(expected = Cache.CacheException.class)
    public void shouldThrowCacheErrorGettingInvalidDealFromLoadedCache() throws Cache.CacheException {
        Cache loadedCache = createLoadedCache();
        Deal deal = loadedCache.getDeal("wontHaveThis");
    }

    //testing static methods
    @Test
    public void shouldReturnBlankInnerValue() {
        String expected = "";
        String actual = (String) Cache.parseInnerValue(DealProperty.Value.ValueType.BLANK, null);

        Assert.assertTrue(expected.equals(actual));
    }

    @Test
    public void shouldReturnBooleanInnerValue() {
        JsonElement mockedElement = mock(JsonElement.class);
        when(mockedElement.getAsBoolean()).thenReturn(true);

        boolean actual = (Boolean) Cache.parseInnerValue(DealProperty.Value.ValueType.BOOLEAN, mockedElement);

        Assert.assertEquals(true, actual);
    }

    @Test
    public void shouldReturnNumericInnerValue() {
        JsonElement mockedElement = mock(JsonElement.class);
        when (mockedElement.getAsDouble()).thenReturn(23.1);

        double actual = (Double) Cache.parseInnerValue(DealProperty.Value.ValueType.NUMERIC, mockedElement);

        Assert.assertTrue(23.1 == actual);
    }

    @Test
    public void shouldReturnStringInnerValue() {
        JsonElement mockedElement = mock(JsonElement.class);
        when(mockedElement.getAsString()).thenReturn("test");

        String actual = (String) Cache.parseInnerValue(DealProperty.Value.ValueType.STRING, mockedElement);

        Assert.assertTrue(actual.equals("test"));
    }

    @Test
    public void shouldReturnBlankType() {
        Assert.assertTrue(Cache.parseType("BLANK").compareTo(DealProperty.Value.ValueType.BLANK) == 0);
    }

    @Test
    public void shouldReturnBooleanType() {
        Assert.assertTrue(Cache.parseType("BOOLEAN").compareTo(DealProperty.Value.ValueType.BOOLEAN) == 0);

    }

    @Test
    public void shouldReturnNumericType() {
        Assert.assertTrue(Cache.parseType("NUMERIC").compareTo(DealProperty.Value.ValueType.NUMERIC) == 0);

    }

    @Test
    public void shouldReturnStringType() {
        Assert.assertTrue(Cache.parseType("STRING").compareTo(DealProperty.Value.ValueType.STRING) == 0);

    }

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
                "{\"deals\":{\"Project PE - AA2\":{\"dealProperties\":{\"Deal Code Name\":{\"values\":{\"2014-10-10T10:10:00.000+08:00\":{\"innerValue\":\"Deal Code - Project PE - AA2\",\"type\":\"STRING\"}}}}},\"Project PE - AA1\":{\"dealProperties\":{\"Deal Code Name\":{\"values\":{\"2014-10-10T10:10:00.000+08:00\":{\"innerValue\":\"Deal Code - Project PE - AA1\",\"type\":\"STRING\"}}}}}},\"columnIndex\":null}";
        String actual = Cache.serializeCache(toBeParsed, null);

        System.out.println(actual);

        Assert.assertTrue(actual.equals(expected));
    }

    @Test
    public void shouldParseJsonToCache() {
        String json =
                "{\"deals\":{\"Project PE - AA2\":{\"dealProperties\":{\"Deal Code Name\":{\"values\":{\"2014-10-10T10:10:00.000+08:00\":{\"innerValue\":\"Deal Code - Project PE - AA2\",\"type\":\"STRING\"}}}}},\"Project PE - AA1\":{\"dealProperties\":{\"Deal Code Name\":{\"values\":{\"2014-10-10T10:10:00.000+08:00\":{\"innerValue\":\"Deal Code - Project PE - AA1\",\"type\":\"STRING\"}}}}}}}";

        String actual = String.valueOf(Cache.deserializeCacheContents(json));
        String expected =
                "{Project PE - AA2= Deal properties : {Deal Code Name={2014-10-10T10:10:00.000+08:00=Deal Code - Project PE - AA2 (type: STRING)}}, Project PE - AA1= Deal properties : {Deal Code Name={2014-10-10T10:10:00.000+08:00=Deal Code - Project PE - AA1 (type: STRING)}}}";

        Assert.assertTrue(actual.equals(expected));


    }

    @Test
    public void shouldDeserializeCacheColumns() {
        String json = "{\"columnIndex\": [\"column1\", \"column2\", \"column3\"]}";
        Set<String> actual = Cache.deserializeCacheColumns(json);
        Assert.assertThat(actual, containsInAnyOrder("column1", "column2", "column3"));
    }

}
