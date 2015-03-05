package query;

import cache.Cache;
import deal.DealProperty;

public interface SpecialColumn {

    public DealProperty.Value evaluate(Query query, Cache cache, String dealName)
            throws SpecialColumnException, Cache.CacheException;

    public String getHeader();

    class SpecialColumnException extends Exception {
        public SpecialColumnException(String e) {
            super(e);
        }
    }

}
