package query;

import cache.Cache;
import com.google.common.collect.Maps;
import deal.Deal;
import deal.DealProperty;
import managers.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalculatedColumn implements SpecialColumn {

    Logger logger = LoggerFactory.getLogger(CalculatedColumn.class);

    private final String header, firstHalf, operatorString, secondHalf;
    private final Operator operator;

    private final Map<String, Operator> operatorMap;

    public CalculatedColumn(String header, String firstHalf, String operator, String secondHalf)
            throws SpecialColumnException {
        logger.info("Constructing calculated column");
        this.header = header;
        this.firstHalf = firstHalf;
        this.operatorString = operator;
        this.secondHalf = secondHalf;

        operatorMap = Maps.newHashMap();
        operatorMap.put("HISTORIC", new HistoricOperator());
        operatorMap.put("ADD", new AddOperator());
        operatorMap.put("SUBTRACT", new SubtractOperator());
        operatorMap.put("MULTIPLY", new MultiplyOperator());
        operatorMap.put("DIVIDE", new DivideOperator());
        operatorMap.put("AGGREGATE", new AggregateOperator());
        operatorMap.put("AVERAGE", new AverageOperator());
        operatorMap.put("CONCAT", new ConcatOperator());

        this.operator = getOperator(operator);
    }

    public String getHeader() {
        return header;
    }

    public DealProperty.Value evaluate(Query query, String dealName) throws
            Exception {

        CacheManager cm = CacheManager.getCacheManager();

        Deal deal = cm.getDeal(dealName);
        return operator.evaluate(query, deal, firstHalf, secondHalf);
    }

    public DealProperty.Value evaluate(Query query, Deal deal) throws
            Cache.CacheException, SpecialColumnException {
        return operator.evaluate(query, deal, firstHalf, secondHalf);
    }

   private Operator getOperator(String operator) throws SpecialColumnException {
       if (operatorMap.containsKey(operator)) return operatorMap.get(operator);
       throw new SpecialColumnException("Unknown operator: " + operator);
   }

    public String getFirstHalf() {
        return firstHalf;
    }

    public String getOperator() {
        return operatorString;
    }

    public String getSecondHalf() {
        return secondHalf;
    }


    private interface Operator {
        public DealProperty.Value evaluate(Query query, Deal deal, String firstHalf, String secondHalf)
                throws Cache.CacheException, SpecialColumnException;
    }

    private class HistoricOperator implements Operator {
        @Override
        public DealProperty.Value evaluate(Query query, Deal deal, String firstHalf,
                                           String secondHalf) throws SpecialColumnException {
            DealProperty dealProperty = (deal.dealProperties.containsKey(firstHalf))
                    ? deal.dealProperties.get(firstHalf) : null;

            if (dealProperty == null)
                throw new SpecialColumnException("DealProperty " + firstHalf + " does not exist");

            int days = Integer.parseInt(secondHalf);

            return dealProperty.getValueMinusXDays(days);
        }
    }

    private abstract class RangeOperator implements Operator {

        @Override
        public DealProperty.Value evaluate(Query query, Deal deal, String firstHalf,
                                           String secondHalf) throws SpecialColumnException {

            DealProperty dp = (deal.dealProperties.containsKey(firstHalf)) ?
                    deal.dealProperties.get(firstHalf) : null;

            if (dp == null) throw new SpecialColumnException("DealProperty " + firstHalf + " does not exist");

            if (secondHalf == null)
                throw new SpecialColumnException("Need instruction for aggregate operator");

            Pattern simplePattern = Pattern.compile("(\\d+)");
            Pattern complexPattern = Pattern.compile("(\\d+)-(\\d+)");

            Matcher simpleMatcher = simplePattern.matcher(secondHalf);
            Matcher complexMatcher = complexPattern.matcher(secondHalf);

            int r1, r2;

            if (secondHalf.equals("*")) {
                r1 = 0;
                r2 = 0;
            } else if (simpleMatcher.find()) {
                r1 = Integer.valueOf(simpleMatcher.group(1));
                r2 = 0;
            } else if (complexMatcher.find()) {
                r1 = Integer.valueOf(complexMatcher.group(1));
                r2 = Integer.valueOf(complexMatcher.group(2));
            } else
                throw new
                        SpecialColumnException("Instruction for aggregate operator invalid: " + secondHalf);

            return calculate(dp, r1, r2);
        }

        public abstract DealProperty.Value calculate(DealProperty d1, int r1, int r2);
    }

    private class AverageOperator extends RangeOperator {

        @Override
        public DealProperty.Value calculate(DealProperty dp, int r1, int r2) {
            double total = 0, count = 0;
            for (DealProperty.Value value : dp.getValuesForDayRange(r1, r2)) {
                if (value.type.equals(DealProperty.Value.ValueType.NUMERIC)) {
                    total += (Double) value.innerValue;
                    count++;
                }
                else logger.warn("Unable to add value " + value.innerValue
                        + " in aggregation operation, as not numeric");
            }

            double average = total / count;

            return new DealProperty.Value(average, DealProperty.Value.ValueType.NUMERIC);
        }
    }

    private class AggregateOperator extends RangeOperator {

        @Override
        public DealProperty.Value calculate(DealProperty dp, int r1, int r2) {
            double total = 0;
            for (DealProperty.Value value : dp.getValuesForDayRange(r1, r2)) {
                if (value.type.equals(DealProperty.Value.ValueType.NUMERIC)) total += (Double) value.innerValue;
                else logger.warn("Unable to add value " + value.innerValue
                        + " in aggregation operation, as not numeric");
            }

            return new DealProperty.Value(total, DealProperty.Value.ValueType.NUMERIC);
        }


    }

    private abstract class MathematicalOperator implements Operator {

        @Override
        public DealProperty.Value evaluate(Query query, Deal deal, String firstHalf, String secondHalf)
                throws Cache.CacheException, SpecialColumnException {

            if (firstHalf == null || firstHalf.trim().equals("") || secondHalf == null || secondHalf.trim().equals(""))
                throw new SpecialColumnException("Missing parameter for mathematical operation");

            double a, b;

            if (firstHalf.startsWith("##")) a = Double.parseDouble(firstHalf.substring(1));
            else if (firstHalf.startsWith("=")) {
                String reference = firstHalf.substring(1);
                if (query.calculatedColumns.containsKey(reference)) {
                    CalculatedColumn cc = query.calculatedColumns.get(reference);
                    DealProperty.Value res = cc.evaluate(query, deal);

                    if (!(res.type.equals(DealProperty.Value.ValueType.NUMERIC)))
                        throw new SpecialColumnException
                                ("Mathematical operator cannot be applied to non-numeric DealProperty");

                    a = (Double) res.innerValue;
                } else throw new SpecialColumnException("Calculated column missing: " + reference);
            }
            else {
                DealProperty dp1 = (deal.dealProperties.containsKey(firstHalf)) ?
                        deal.dealProperties.get(firstHalf) : null;

                if (dp1 == null) throw new
                        SpecialColumnException("DealProperty " + firstHalf + " does not exist");
                if (!(dp1.getLatestValue().type.equals(DealProperty.Value.ValueType.NUMERIC)))
                    throw new SpecialColumnException
                            ("Mathematical operator cannot be applied to non-numeric DealProperty " + firstHalf);

                a = (Double) dp1.getLatestValue().innerValue;
            }

            if (secondHalf.startsWith("##")) b = Double.parseDouble(secondHalf.substring(1));
            else if (secondHalf.startsWith("=")) {
                String reference = secondHalf.substring(1);
                if (query.calculatedColumns.containsKey(reference)) {
                    logger.info("Executing calculated column: " + reference);

                    CalculatedColumn cc = query.calculatedColumns.get(reference);
                    DealProperty.Value res = cc.evaluate(query, deal);

                    if (!(res.type.equals(DealProperty.Value.ValueType.NUMERIC)))
                        throw new SpecialColumnException
                                ("Mathematical operator cannot be applied to non-numeric DealProperty");

                    b = (Double) res.innerValue;
                } else throw new SpecialColumnException("Calculated column missing: " + reference);
            }
            else {
                DealProperty dp2 = (deal.dealProperties.containsKey(secondHalf)) ?
                        deal.dealProperties.get(secondHalf) : null;

                if (dp2 == null) throw new
                        SpecialColumnException("DealProperty " + secondHalf + " does not exist");
                if (!(dp2.getLatestValue().type.equals(DealProperty.Value.ValueType.NUMERIC)))
                    throw new SpecialColumnException
                            ("Mathematical operator cannot be applied to non-numeric DealProperty " + secondHalf);

                b = (Double) dp2.getLatestValue().innerValue;
            }

            return calculate(a, b);
        }


        public abstract DealProperty.Value calculate(double dp1, double dp2);
    }

    private class AddOperator extends MathematicalOperator{
        @Override
        public DealProperty.Value calculate(double a, double b) {
            return new DealProperty.Value((a + b), DealProperty.Value.ValueType.NUMERIC);
        }
    }

    private class SubtractOperator extends MathematicalOperator {
        @Override
        public DealProperty.Value calculate(double a, double b) {
            return new DealProperty.Value((a - b), DealProperty.Value.ValueType.NUMERIC);
        }
    }

    private class MultiplyOperator extends MathematicalOperator {
        @Override
        public DealProperty.Value calculate(double a, double b) {
            return new DealProperty.Value((a * b), DealProperty.Value.ValueType.NUMERIC);
        }
    }

    private class DivideOperator extends MathematicalOperator {
        @Override
        public DealProperty.Value calculate(double a, double b) {
            return new DealProperty.Value((a / b), DealProperty.Value.ValueType.NUMERIC);
        }
    }

    private class ConcatOperator implements Operator {

        @Override
        public DealProperty.Value evaluate(Query query, Deal deal, String firstHalf,
                                           String secondHalf) throws Cache.CacheException,
                SpecialColumnException {
            if (firstHalf == null || secondHalf == null) throw new SpecialColumnException
                    ("Both parameters are required for ConcatOperator");

            String str1, str2;

            if (firstHalf.startsWith("##")) str1 = firstHalf.substring(1);
            else if (firstHalf.startsWith("=")) {
                String reference = firstHalf.substring(1);
                if (query.calculatedColumns.containsKey(reference)) {
                    logger.info("Executing calculated column: " + reference);

                    CalculatedColumn cc = query.calculatedColumns.get(reference);
                    DealProperty.Value res = cc.evaluate(query, deal);
                    str1 = (String) res.innerValue;
                } else throw new SpecialColumnException("Calculated column missing: " + reference);
            }
            else {
                DealProperty dp = (deal.dealProperties.containsKey(firstHalf)) ?
                        deal.dealProperties.get(firstHalf) : null;

                if (dp == null) throw new
                        SpecialColumnException("DealProperty " + firstHalf + " does not exist");

                str1 = (String) dp.getLatestValue().innerValue;
            }

            if (secondHalf.startsWith("##")) str2 = secondHalf.substring(1);
            else if (secondHalf.startsWith("=")) {
                String reference = secondHalf.substring(1);
                if (query.calculatedColumns.containsKey(reference)) {
                    logger.info("Executing calculated column: " + reference);

                    CalculatedColumn cc = query.calculatedColumns.get(reference);
                    DealProperty.Value res = cc.evaluate(query, deal);
                    str2 = (String) res.innerValue;
                } else throw new SpecialColumnException("Calculated column missing: " + reference);
            }
            else {
                DealProperty dp = (deal.dealProperties.containsKey(secondHalf)) ?
                        deal.dealProperties.get(secondHalf) : null;

                if (dp == null) throw new
                        SpecialColumnException("DealProperty " + firstHalf + " does not exist");

                str2 = (String) dp.getLatestValue().innerValue;
            }

            return new DealProperty.Value((str1 + str2), DealProperty.Value.ValueType.STRING);
        }
    }

}
