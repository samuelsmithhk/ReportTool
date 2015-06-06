package deal;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

public class DealPropertyTest {

    @Test //also tests getValueAtTimestamp()
    public void shouldBuildDealPropertyWithThreeCorrectValues() {
        DateTime time1 = new DateTime(2014, 10, 10, 10, 10), time2 = new DateTime(2014, 11, 11, 11, 11),
                time3 = new DateTime(2014, 12, 12, 12, 12);

        DealProperty.Value expected1 = new DealProperty.Value(23, DealProperty.Value.ValueType.NU, "TEST"),
                expected2 = new DealProperty.Value(24, DealProperty.Value.ValueType.NU, "TEST"),
                expected3 = new DealProperty.Value(25, DealProperty.Value.ValueType.NU, "TEST");

        DealProperty.DealPropertyBuilder dpb = new DealProperty.DealPropertyBuilder();
        DealProperty dp = dpb
                .withValue(time1, expected1)
                .withValue(time2, expected2)
                .withValue(time3, expected3)
                .build();

        DealProperty.Value actual1 = dp.getValueAtTimestamp(time1), actual2 = dp.getValueAtTimestamp(time2),
                actual3 = dp.getValueAtTimestamp(time3);

        Assert.assertTrue((expected1 == actual1) && (expected2 == actual2) && (expected3 == actual3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldReturnErrorForDuplicateTimestampAndStillReturnOtherwiseValidDealProperty() {
        DateTime time1 = new DateTime(2014, 10, 10, 10, 10), time2 = new DateTime(2014, 11, 11, 11, 11);

        DealProperty.Value expected1 = new DealProperty.Value(23, DealProperty.Value.ValueType.NU, "TEST"),
                expected2 = new DealProperty.Value(24, DealProperty.Value.ValueType.NU, "TEST"),
                expected3 = new DealProperty.Value(25, DealProperty.Value.ValueType.NU, "TEST");

        DealProperty.DealPropertyBuilder dpb = new DealProperty.DealPropertyBuilder();
        DealProperty dp = dpb
                .withValue(time1, expected1)
                .withValue(time2, expected2)
                .withValue(time2, expected3)
                .build();

        DealProperty.Value actual1 = dp.getValueAtTimestamp(time1), actual2 = dp.getValueAtTimestamp(time2);

        Assert.assertTrue((expected1 == actual1) && (expected2 == actual2));
    }

    @Test //also tests getLatestValue()
    public void shouldSuccessfullyAddValueToDealProperty() {
        DateTime time1 = new DateTime(2014, 10, 10, 10, 10), time2 = new DateTime(2014, 11, 11, 11, 11);

        DealProperty.Value expected1 = new DealProperty.Value(23, DealProperty.Value.ValueType.NU, "TEST"),
                expected2 = new DealProperty.Value(24, DealProperty.Value.ValueType.NU, "TEST");

        DealProperty.DealPropertyBuilder dpb = new DealProperty.DealPropertyBuilder();
        DealProperty dp = dpb
                .withValue(time1, expected1)
                .build();

        DealProperty.Value actual1 = dp.getLatestValue();

        dp.addValue(time2, expected2);

        DealProperty.Value actual2 = dp.getLatestValue();

        Assert.assertTrue((expected1 == actual1) && (expected2 == actual2));
    }
}
