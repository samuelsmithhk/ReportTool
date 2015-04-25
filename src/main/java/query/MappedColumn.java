package query;

import deal.Deal;
import deal.DealProperty;
import managers.CacheManager;

public class MappedColumn implements SpecialColumn {

    private final String original, header;

    public MappedColumn(String original, String header) {

        this.original = original;
        this.header = header;
    }

    @Override
    public String getHeader() {
        return header;
    }

    @Override
    public DealProperty.Value evaluate(Query query, String dealName)
            throws Exception {

        CacheManager cm = CacheManager.getCacheManager();

        Deal deal = cm.getDeal(dealName);

        if (deal.dealProperties.containsKey(original)) return deal.dealProperties.get(original).getLatestValue();
        return new DealProperty.Value("", DealProperty.Value.ValueType.BL, "MAPPED");
    }

    public String getOriginal() {
        return original;
    }
}