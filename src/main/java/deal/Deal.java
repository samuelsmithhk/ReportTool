package deal;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by samuelsmith on 28/10/2014.
 */
public class Deal {

    private final Logger logger = LoggerFactory.getLogger(Deal.class);

    public final Map<String, DealProperty> dealProperties;

    public Deal (Map<String, DealProperty> dealProperties) {
        this.dealProperties = dealProperties;
    }

    public void updateDeal(DateTime timestamp, Deal deal) {
        logger.info("Updating deal " + this);

        Map<String, DealProperty> updated = deal.dealProperties;

        for (Map.Entry<String, DealProperty> dp : updated.entrySet()) {
            if (dealProperties.containsKey(dp.getKey()))
                dealProperties.get(dp.getKey()).addValue(timestamp, dp.getValue().getLatestValue());
            else {
                DealProperty.DealPropertyBuilder dpb = new DealProperty.DealPropertyBuilder();
                DealProperty newDP = dpb.withValue(timestamp, dp.getValue().getLatestValue()).build();
                dealProperties.put(dp.getKey(), newDP);
            }
        }
    }

    @Override
    public String toString() {
        return " Deal properties : " + dealProperties;
    }

    public int almostUniqueCode() {
        logger.info("Generating almost unique code for" + this);

        int retInt = 0;

        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, DealProperty> e : dealProperties.entrySet()) {
            sb.append(e.getKey() + e.getValue().getLatestValue().innerValue);
        }

        String dp = sb.toString();
        char[] dpArr = dp.toCharArray();

        for (char c : dpArr) {
            retInt += c;
        }

        return retInt;
    }
}
