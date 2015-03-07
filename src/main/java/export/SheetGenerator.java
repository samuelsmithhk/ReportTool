package export;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import files.TemplateFileManager;
import managers.TemplateManager;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import query.Group;
import query.Query;
import query.QueryResult;
import query.QueryResultDeal;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SheetGenerator {

    public static Workbook generateSheet(QueryResult results) throws Exception {
        SheetGenerator generator = new SheetGenerator();

        Workbook workbook = results.hasTemplate ?
                TemplateManager.getTemplateManager().getTemplate(results.query.templateName) : new XSSFWorkbook();
        return generator.generateWorkbook(workbook, results);
    }

    private Workbook generateWorkbook(Workbook workbook, QueryResult results) {
        Map<String, CellStyle> styles = GeneratorUtils.createStyles(workbook);

        for (QueryResult.QueryResultSheet resultSheet : results.sheets) {
            workbook = generateOutputSheet(workbook, resultSheet, styles);
            workbook.setSheetHidden(workbook.getSheetIndex(resultSheet.sheetName), resultSheet.isHidden);
        }

        return workbook;
    }

    private Workbook generateOutputSheet(Workbook toWorkOn,
                                         QueryResult.QueryResultSheet deals, Map<String, CellStyle> styles) {

        Sheet sheet = toWorkOn.createSheet(deals.sheetName);
        sheet.setAutobreaks(true);

        Set<Query.QuerySheet.Header> headers = GeneratorUtils.getHeadersFromQueryResultSheet(deals);

        Row headerRow = sheet.createRow(0), subRow = sheet.createRow(1);
        headerRow.setHeightInPoints(2 * sheet.getDefaultRowHeightInPoints());
        int n = 0;

        for (Query.QuerySheet.Header header : headers) {
            Cell headerCell = headerRow.createCell(n);
            headerCell.setCellValue(header.header);
            headerCell.setCellStyle(styles.get("header"));

            int hs = n;

            for (String sub : header.subs) {
                Cell subCell = subRow.createCell(n);
                subCell.setCellValue(sub);
                subCell.setCellStyle(styles.get("header"));

                if (n != hs) {
                    Cell aboveCell = headerRow.createCell(n);
                    aboveCell.setCellStyle(styles.get("header"));
                }

                sheet.setColumnWidth(n, GeneratorUtils.calculateColumnWidth(sheet, n, sub));
                n++;
            }

            sheet.addMergedRegion(new CellRangeAddress(0, 0, hs, n - 1));
        }

        int r = 2;
        for (Group g : deals.valuesGrouped) {

            Row groupHeaderRow = null;
            int ghRow = 0;

            if (!g.groupKey.equals("no-group")) {
                groupHeaderRow = sheet.createRow(r);
                Cell groupCell = groupHeaderRow.createCell(0);
                groupCell.setCellValue(g.groupKey);
                groupCell.setCellStyle(styles.get("groupHeader"));
                ghRow = r;
                r++;
            }

            for (QueryResultDeal d : g.groupValues) {
                Row currentRow = sheet.createRow(r);
                List<String> values = GeneratorUtils.getValuesInHeaderOrder(headers, d);

                n = 0;
                for (String value : values) {
                    Cell currentCell = currentRow.createCell(n);
                    currentCell.setCellValue(value);
                    currentCell.setCellStyle(styles.get("valueCell"));

                    if ((groupHeaderRow != null) && (n != 0)) {
                        Cell aboveCell = groupHeaderRow.createCell(n);
                        aboveCell.setCellStyle(styles.get("groupHeader"));
                    }

                    sheet.setColumnWidth(n, GeneratorUtils.calculateColumnWidth(sheet, n, value));

                    n++;
                }
                r++;
            }

            if (groupHeaderRow != null) sheet.addMergedRegion(new CellRangeAddress(ghRow, ghRow, 0, n - 1));

        }

        return toWorkOn;
    }

    public static class GeneratorUtils {

        static Set<Query.QuerySheet.Header> getHeadersFromQueryResultSheet(QueryResult.QueryResultSheet deals) {
            List<Group> values = deals.valuesGrouped;
            List<QueryResultDeal> results = values.get(0).groupValues;
            QueryResultDeal result = results.get(0);

            Set<Query.QuerySheet.Header> retSet = Sets.newLinkedHashSet();
            List<Query.QuerySheet.Header> allowedHeaders = deals.headers;

            for (QueryResultDeal.Header h : result.dealProperties.keySet()) {
                String primaryH = h.header;

                for (Query.QuerySheet.Header a : allowedHeaders) {
                    if (a.header.equals(primaryH)) {
                        retSet.add(a);
                        break;
                    }
                }
            }

            return retSet;
        }

        static List<String> getValuesInHeaderOrder(Set<Query.QuerySheet.Header> headers, QueryResultDeal d){
            List<String> retList = Lists.newLinkedList();

            for (Query.QuerySheet.Header h : headers) {
                for (String sub: h.subs) {
                    if (d.hasDealProperty(sub)) retList.add(d.getDPValue(sub));
                    else retList.add("");
                }
            }

            return retList;
        }

        static int calculateColumnWidth(Sheet sheet, int column, String value) {
            int current = sheet.getColumnWidth(column);
            int potential = (value.length() * 256) + 512;

            if (current >= potential) return current;
            else if (potential >= 40000) return 40000;
            else return potential;
        }

        static Map<String, CellStyle> createStyles(Workbook wb) {
            Map<String, CellStyle> styles = Maps.newHashMap();

            XSSFCellStyle hs, gs, vs;

            Font headerFont = wb.createFont();
            headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
            headerFont.setFontHeightInPoints((short) 12);

            hs = (XSSFCellStyle) createBorderedStyle(wb);
            hs.setAlignment(CellStyle.ALIGN_CENTER);
            hs.setFont(headerFont);
            hs.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
            hs.setFillForegroundColor(new XSSFColor(Color.ORANGE));
            hs.setWrapText(true);

            gs = (XSSFCellStyle) createBorderedStyle(wb);
            gs.setFont(headerFont);
            gs.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
            gs.setFillForegroundColor(new XSSFColor(Color.LIGHT_GRAY));

            vs = (XSSFCellStyle) wb.createCellStyle();
            vs.setAlignment(CellStyle.ALIGN_CENTER);
            vs.setWrapText(true);


            styles.put("header", hs);
            styles.put("groupHeader", gs);
            styles.put("valueCell", vs);

            return styles;
        }

        private static CellStyle createBorderedStyle(Workbook wb) {
            CellStyle style = wb.createCellStyle();
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setRightBorderColor(IndexedColors.BLACK.getIndex());
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
            style.setBorderLeft(CellStyle.BORDER_THIN);
            style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
            style.setBorderTop(CellStyle.BORDER_THIN);
            style.setTopBorderColor(IndexedColors.BLACK.getIndex());
            return style;
        }

    }

}
