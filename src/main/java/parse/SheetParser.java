package parse;

import deal.Deal;

import java.util.Map;

/**
 * Created by samuelsmith on 28/10/2014.
 */
public interface SheetParser {
    public Map<String, Deal> parse() throws AbstractParser.ParserException;
}
