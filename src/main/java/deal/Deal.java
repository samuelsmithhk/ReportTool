package deal;

import java.util.Map;

/**
 * Created by samuelsmith on 28/10/2014.
 */
public class Deal {

    public final String opportunity;
    public final Map<String, String> dealProperties;

    public Deal (String opportunity, Map<String, String> dealProperties) {
        this.opportunity = opportunity;
        this.dealProperties = dealProperties;
    }

    @Override
    public String toString() {
        return "Opportunity: " + opportunity + " deal properties (values): " + dealProperties.values();
    }

    public int almostUniqueCode() {
        int retInt = 0;

        char[] oppArr = opportunity.toCharArray();
        for (char c : oppArr) {
            retInt += c;
        }

        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> e : dealProperties.entrySet()) {
            sb.append(e.getKey() + e.getValue());
        }

        String dp = sb.toString();
        char[] dpArr = dp.toCharArray();

        for (char c : dpArr) {
            retInt += c;
        }

        return retInt;
    }


}
