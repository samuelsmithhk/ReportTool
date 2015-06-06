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

    private Cache createCache() {
        return new Cache();
    }



    @Test
    public void shouldCreateEmptyCache() {
        Cache emptyCache = createCache();
        boolean condition1 = emptyCache.getDeals().size() == 0;
        boolean condition2 = emptyCache.getCols().size() == 0;
        boolean condition3 = emptyCache.getDirectoriesLastUpdated().size() == 0;

        Assert.assertTrue(condition1 && condition2 && condition3);
    }

    @Test(expected = Cache.CacheException.class)
    public void shouldThrowCacheErrorGettingDealFromEmptyCache() throws Cache.CacheException {
        Cache emptyCache = createCache();
        @SuppressWarnings("UnusedDeclaration") Deal deal = emptyCache.getDeal("empty");
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
        when(mockedElement.getAsDouble()).thenReturn(23.1);

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
        Assert.assertTrue(Cache.parseType("BL").compareTo(DealProperty.Value.ValueType.BL) == 0);
    }

    @Test
    public void shouldReturnBooleanType() {
        Assert.assertTrue(Cache.parseType("BO").compareTo(DealProperty.Value.ValueType.BO) == 0);

    }

    @Test
    public void shouldReturnNumericType() {
        Assert.assertTrue(Cache.parseType("NU").compareTo(DealProperty.Value.ValueType.NU) == 0);

    }

    @Test
    public void shouldReturnStringType() {
        Assert.assertTrue(Cache.parseType("ST").compareTo(DealProperty.Value.ValueType.ST) == 0);

    }
}
