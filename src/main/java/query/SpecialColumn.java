package query;

import cache.Cache;
import deal.DealProperty;

/**
 * Created by samuelsmith on 07/02/2015.
 */
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
