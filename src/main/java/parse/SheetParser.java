package parse;

import deal.Deal;

import java.util.Map;

public interface SheetParser {
    public Map<String, Deal> parse() throws AbstractParser.ParserException;
}
