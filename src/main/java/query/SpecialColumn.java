package query;

import deal.DealProperty;

public interface SpecialColumn {

    DealProperty.Value evaluate(Query query, String dealName)
            throws Exception;

    String getHeader();

    class SpecialColumnException extends Exception {
        public SpecialColumnException(String e) {
            super(e);
        }
    }

}
