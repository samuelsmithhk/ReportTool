package query;

import deal.DealProperty;

public interface SpecialColumn {

    public DealProperty.Value evaluate(Query query, String dealName)
            throws Exception;

    public String getHeader();

    class SpecialColumnException extends Exception {
        public SpecialColumnException(String e) {
            super(e);
        }
    }

}
