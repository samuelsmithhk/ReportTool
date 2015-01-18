package query;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by samuelsmith on 09/11/2014.
 */
public class Query {

    private static transient Logger logger = LoggerFactory.getLogger(Query.class);

    public final List<QuerySheet> sheets;
    public final Map<String, CalculatedColumn> calculatedColumns;
    public final String name, templateName;
    public final boolean hasTemplate;

    private Query(QueryBuilder qb) {
        logger.info("Creating query");
        this.sheets = qb.sheets;
        this.calculatedColumns = qb.calculatedColumns;
        this.name = qb.name;
        this.templateName = qb.templateName;
        this.hasTemplate = qb.hasTemplate;
    }

    public static class QueryBuilder {
        List<QuerySheet> sheets;
        Map<String, CalculatedColumn> calculatedColumns;
        String name, templateName;
        boolean hasTemplate;

        public QueryBuilder(String name) {
            this.name = name;
            this.sheets = Lists.newLinkedList();
            this.calculatedColumns = Maps.newHashMap();
            this.templateName = null;
            this.hasTemplate = false;
        }

        public QueryBuilder addSheet(QuerySheet sheet) {
            this.sheets.add((sheet));
            return this;
        }

        public QueryBuilder addCalculatedColumn(String columnName, CalculatedColumn column) {
            this.calculatedColumns.put(columnName, column);
            return this;
        }

        public QueryBuilder setTemplate(String templateName) {
            this.templateName = templateName;
            this.hasTemplate = true;
            return this;
        }

        public Query build() {
            return new Query(this);
        }
    }



    public static class QuerySheet {
        public final List<Header> headers;
        public final String sheetName, groupBy, filterColumn, filterValue, sortBy;

        private QuerySheet(QuerySheetBuilder qsb) {
            logger.info("Creating query sheet");

            this.sheetName = qsb.sheetName;
            this.headers = qsb.headers;
            this.groupBy = qsb.groupBy;
            this.filterColumn = qsb.filterColumn;
            this.filterValue = qsb.filterValue;
            this.sortBy = qsb.sortBy;
        }

        public static class QuerySheetBuilder {
            List<Header> headers;
            String sheetName, groupBy, filterColumn, filterValue, sortBy;

            public QuerySheetBuilder(String sheetName) {
                this.sheetName = sheetName;
                headers = null;
                groupBy = null;
                filterColumn = null;
                filterValue = null;
                sortBy = null;
            }

            public QuerySheetBuilder withColumns(String header, String[] columns) {
                if (this.headers == null) {
                    this.headers = Lists.newLinkedList();
                }

                Header _header = new Header(header, columns);

                this.headers.add(_header);

                return this;
            }

            public QuerySheetBuilder setGroupBy(String groupBy) {
                this.groupBy = groupBy;
                return this;
            }

            public QuerySheetBuilder setSortBy(String sortBy) {
                this.sortBy = sortBy;
                return this;
            }

            public QuerySheetBuilder setFilter(String filterColumn, String filterValue) {
                this.filterColumn = filterColumn;
                this.filterValue = filterValue;
                return this;
            }

            public QuerySheet build() {
                return new QuerySheet(this);
            }
        }

        public static class Header {
            public final String header;
            public final String[] subs;

            public Header(String header, String[] subs) {
                this.header = header;
                this.subs = subs;
            }

            public void overwriteSub(String subToOverwrite, String newValue) {
                for (int i = 0; i < subs.length; i++) {
                    if (subs[i].equals(subToOverwrite)) subs[i] = newValue;
                }
            }
        }
    }

}
