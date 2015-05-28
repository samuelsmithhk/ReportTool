package deal;

import org.joda.time.DateTime;

import java.util.Iterator;
import java.util.Map;

public class Deal {

    public final Map<String, DealProperty> dealProperties; //key is column

    public Deal(Map<String, DealProperty> dealProperties) {
        this.dealProperties = dealProperties;
    }

    public void updateDeal(DateTime timestamp, Deal deal) {

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


    /**
     *
     * @return true if still dealProperties
     */
    public boolean purgeOldData() {

        for (Map.Entry<String, DealProperty> entry : dealProperties.entrySet())
            if (!entry.getValue().purgeOldData()) dealProperties.remove(entry.getKey());

        for (Iterator<Map.Entry<String, DealProperty>> dp = dealProperties.entrySet().iterator(); dp.hasNext();) {
            Map.Entry<String, DealProperty> element = dp.next();
            if (!element.getValue().purgeOldData())
                dp.remove();
        }


        return dealProperties.size() != 0;
    }
}
