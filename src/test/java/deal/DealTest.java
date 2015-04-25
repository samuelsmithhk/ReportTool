package deal;

import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class DealTest {

    @Test
    public void shouldUpdateDeal() {
        Deal deal = new Deal(new HashMap<String, DealProperty>());
        DateTime time = new DateTime(1,1,1,1,1);

        DealProperty.DealPropertyBuilder dpb = new DealProperty.DealPropertyBuilder();
        dpb.withValue(time, new DealProperty.Value("Hello", DealProperty.Value.ValueType.ST, "TEST"));

        Map<String, DealProperty> dealProperties = Maps.newHashMap();
        dealProperties.put("Test", dpb.build());

        Deal update = new Deal(dealProperties);

        deal.updateDeal(time, update);

        String expected = " Deal properties : {Test={0001-01-01T01:01:00.000+07:36:42=Hello (type: STRING)}}";
        String actual = deal.toString();

        Assert.assertTrue(expected.equals(actual));
    }
}
