package cache;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import deal.Deal;
import deal.DealProperty;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CacheTest {

    private Cache createEmptyCache() {
         return Cache.createEmptyCache();
    }

    private Cache createLoadedCache() {
        String cacheContents = "{\"deals\":{\"Project PE - AA2\":{\"dealProperties\":{\"Deal Code Name\":{\"values\":{\"2014-10-10T10:10:00.000+08:00\":{\"innerValue\":\"Deal Code - Project PE - AA2\",\"type\":\"STRING\"}}}}},\"Project PE - AA1\":{\"dealProperties\":{\"Deal Code Name\":{\"values\":{\"2014-10-10T10:10:00.000+08:00\":{\"innerValue\":\"Deal Code - Project PE - AA1\",\"type\":\"STRING\"}}}}}}, \"columnIndex\": [\"column1\", \"column2\", \"column3\"],\"lastUpdated\":[{\"d1\":\"2014-10-10T10:10:00.000+08:00\"}]}";
        return Cache.createLoadedCache(cacheContents);
    }

    @Test
    public void shouldCreateEmptyCache() {
        Cache emptyCache = createEmptyCache();
        boolean condition1 = emptyCache.getDeals().size() == 0;
        boolean condition2 = emptyCache.getCols().size() == 0;
        boolean condition3 = emptyCache.getDirectoriesLastUpdated() == null;

        Assert.assertTrue(condition1 && condition2 && condition3);
    }

    @Test(expected = Cache.CacheException.class)
    public void shouldThrowCacheErrorGettingDealFromEmptyCache() throws Cache.CacheException {
        Cache emptyCache = createEmptyCache();
        @SuppressWarnings("UnusedDeclaration") Deal deal = emptyCache.getDeal("empty");
    }

    @Test(expected = Cache.CacheException.class)
    public void shouldThrowCacheErrorGettingInvalidDealFromLoadedCache() throws Cache.CacheException {
        Cache loadedCache = createLoadedCache();
        @SuppressWarnings("UnusedDeclaration") Deal deal = loadedCache.getDeal("wontHaveThis");
    }

    //testing static methods
    @Test
    public void shouldReturnBlankInnerValue() {
        String expected = "";
        String actual = (String) Cache.parseInnerValue(DealProperty.Value.ValueType.BL, null);

        Assert.assertTrue(expected.equals(actual));
    }

    @Test
    public void shouldReturnBooleanInnerValue() {
        JsonElement mockedElement = mock(JsonElement.class);
        when(mockedElement.getAsBoolean()).thenReturn(true);

        boolean actual = (Boolean) Cache.parseInnerValue(DealProperty.Value.ValueType.BO, mockedElement);

        Assert.assertEquals(true, actual);
    }

    @Test
    public void shouldReturnNumericInnerValue() {
        JsonElement mockedElement = mock(JsonElement.class);
        when (mockedElement.getAsDouble()).thenReturn(23.1);

        double actual = (Double) Cache.parseInnerValue(DealProperty.Value.ValueType.NU, mockedElement);

        Assert.assertTrue(23.1 == actual);
    }

    @Test
    public void shouldReturnStringInnerValue() {
        JsonElement mockedElement = mock(JsonElement.class);
        when(mockedElement.getAsString()).thenReturn("test");

        String actual = (String) Cache.parseInnerValue(DealProperty.Value.ValueType.ST, mockedElement);

        Assert.assertTrue(actual.equals("test"));
    }

    @Test
    public void shouldReturnBlankType() {
        Assert.assertTrue(Cache.parseType("BLANK").compareTo(DealProperty.Value.ValueType.BL) == 0);
    }

    @Test
    public void shouldReturnBooleanType() {
        Assert.assertTrue(Cache.parseType("BOOLEAN").compareTo(DealProperty.Value.ValueType.BO) == 0);

    }

    @Test
    public void shouldReturnNumericType() {
        Assert.assertTrue(Cache.parseType("NUMERIC").compareTo(DealProperty.Value.ValueType.NU) == 0);

    }

    @Test
    public void shouldReturnStringType() {
        Assert.assertTrue(Cache.parseType("STRING").compareTo(DealProperty.Value.ValueType.ST) == 0);

    }

    @Test
    public void shouldParseJsonToCache() {
        String json =
                "{\"deals\":{\"Project PE - AA2\":{\"dealProperties\":{\"Deal Code Name\":{\"values\":{\"2014-10-10T10:10:00.000+08:00\":{\"innerValue\":\"Deal Code - Project PE - AA2\",\"type\":\"STRING\"}}}}},\"Project PE - AA1\":{\"dealProperties\":{\"Deal Code Name\":{\"values\":{\"2014-10-10T10:10:00.000+08:00\":{\"innerValue\":\"Deal Code - Project PE - AA1\",\"type\":\"STRING\"}}}}}}}";

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        JsonObject test = element.getAsJsonObject();

        String actual = String.valueOf(Cache.deserializeCacheContents(test));
        String expected =
                "{Project PE - AA2= Deal properties : {Deal Code Name={2014-10-10T10:10:00.000+08:00=Deal Code - Project PE - AA2 (type: STRING)}}, Project PE - AA1= Deal properties : {Deal Code Name={2014-10-10T10:10:00.000+08:00=Deal Code - Project PE - AA1 (type: STRING)}}}";

        Assert.assertTrue(actual.equals(expected));


    }

    @Test
    public void shouldDeserializeCacheColumns() {
        String json = "{\"columnIndex\": [\"column1\", \"column2\", \"column3\"]}";

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        JsonArray test = element.getAsJsonArray();

        Set<String> actual = Cache.deserializeCacheColumns(test);
        Assert.assertThat(actual, containsInAnyOrder("column1", "column2", "column3"));
    }

}
