package query;

import cache.Cache;
import deal.Deal;
import deal.DealProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by samuelsmith on 31/01/2015.
 */
public class MappedColumn implements SpecialColumn {
    Logger logger = LoggerFactory.getLogger(MappedColumn.class);

    private final String original, header;

    public MappedColumn(String original, String header) {
        logger.info("Constructing mapped column");

        this.original = original;
        this.header = header;
    }

    @Override
    public String getHeader() {
        return header;
    }

    public String getOriginal() {
        return original;
    }

    @Override
    public DealProperty.Value evaluate(Query query, Cache cache, String dealName)
            throws Cache.CacheException,SpecialColumnException {
        Deal deal = cache.getDeal(dealName);

        if (deal.dealProperties.containsKey(original)) return deal.dealProperties.get(original).getLatestValue();
        return new DealProperty.Value("", DealProperty.Value.ValueType.BLANK);
    }
}