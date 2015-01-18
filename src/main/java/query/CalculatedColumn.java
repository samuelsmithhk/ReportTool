package query;

import cache.Cache;
import com.google.common.collect.Maps;
import deal.Deal;
import deal.DealProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by samuelsmith on 18/01/2015.
 */
public class CalculatedColumn {

    Logger logger = LoggerFactory.getLogger(CalculatedColumn.class);

    public final String header;
    private final String firstHalf, secondHalf;
    private final Operator operator;

    public CalculatedColumn(String header, String firstHalf, String operator, String secondHalf) throws Exception {
        logger.info("Constructing calculated column");
        this.header = header;
        this.firstHalf = firstHalf;
        this.secondHalf = secondHalf;
        this.operator = getOperator(operator);
    }

    public DealProperty.Value evaluate(Cache cache, String dealName) throws Exception {
        logger.info("Evaluating calculated column for deal: " + dealName);

        Deal deal = cache.getDeal(dealName);
        return operator.evaluate(deal, firstHalf, secondHalf);
    }

   private Operator getOperator(String operator) throws Exception {
       if (operator.equals("HISTORIC")) return new HistoricOperator();
       else throw new Exception("Unknown operator: " + operator);
   }


    private interface Operator {
        public DealProperty.Value evaluate(Deal deal, String firstHalf, String secondHalf);
    }

    private class HistoricOperator implements Operator {
        @Override
        public DealProperty.Value evaluate(Deal deal, String firstHalf, String secondHalf) {
            DealProperty dealProperty = (deal.dealProperties.containsKey(firstHalf))
                    ? deal.dealProperties.get(firstHalf) : null;

            if (dealProperty == null)  return null;

            int days = Integer.parseInt(secondHalf);

            return dealProperty.getValueMinusXDays(days);
        }
    }

}
