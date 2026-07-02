package cn.sl.ehub.common.utils;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Excel 导出工具类，基于 Apache POI
 */
public class ExcelExportUtil {

    private ExcelExportUtil() {}

    /**
     * 导出带表头的列表数据为 Excel（.xlsx）
     *
     * @param headers   表头名称数组，如 ["设备编码", "测点", "时间", "值"]
     * @param columns  对应数据对象的字段名数组，如 ["deviceCode", "pointCode", "dataTime", "value"]
     * @param data     数据列表（对象字段名须与 columns 一一对应）
     * @param fileName 文件名（不含扩展名）
     * @param sheetName Sheet 名称
     * @param response HTTP 响应对象
     */
    public static <T> void export(List<String> headers, List<String> columns,
                                  List<T> data, String fileName,
                                  String sheetName, HttpServletResponse response) throws Exception {
        if (headers.size() != columns.size()) {
            throw new IllegalArgumentException("表头数量与字段数量必须一致");
        }
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);

        for (int i = 0; i < headers.size(); i++) {
            sheet.setColumnWidth(i, 20 * 256);
        }

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (T item : data) {
            Row row = sheet.createRow(rowIdx++);
            for (int i = 0; i < columns.size(); i++) {
                Cell cell = row.createCell(i);
                cell.setCellStyle(dataStyle);
                Object value = ReflectUtils.getFieldValue(item, columns.get(i));
                setCellValue(cell, value);
            }
        }

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + encodedFileName + ".xlsx");

        OutputStream out = response.getOutputStream();
        workbook.write(out);
        out.flush();
        workbook.close();
    }

    private static void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Date) {
            cell.setCellValue(DateFormatUtils.format((Date) value, "yyyy-MM-dd HH:mm:ss"));
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }
}
