package cn.sl.ehub.console.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 多 Sheet 报告及异步导出任务。 */
@Service
public class GridDeliveryReportService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FILE_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final JdbcTemplate jdbcTemplate;
    private final GridDeliveryQualityService qualityService;
    private final GridDeliveryQualityManagementService managementService;
    private final ThreadPoolTaskExecutor executor;

    @Value("${grid.delivery.export.path:./exports/grid-delivery}")
    private String exportPath;

    @Value("${grid.delivery.export.expire-hours:72}")
    private int expireHours;

    public GridDeliveryReportService(JdbcTemplate jdbcTemplate,
                                     GridDeliveryQualityService qualityService,
                                     GridDeliveryQualityManagementService managementService,
                                     @Qualifier("threadPoolTaskExecutor") ThreadPoolTaskExecutor executor) {
        this.jdbcTemplate = jdbcTemplate;
        this.qualityService = qualityService;
        this.managementService = managementService;
        this.executor = executor;
    }

    public byte[] report(String aggregatorId, String startDate, String endDate, String channelNo) throws IOException {
        return report(aggregatorId, startDate, endDate, channelNo, null);
    }

    public byte[] report(String aggregatorId, String startDate, String endDate, String channelNo,
                         Long periodId) throws IOException {
        return report(aggregatorId, startDate, endDate, channelNo, periodId, null);
    }

    public byte[] report(String aggregatorId, String startDate, String endDate, String channelNo,
                         Long periodId, String resourceTypeId) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writeReport(aggregatorId, parseRange(startDate, endDate, 31), channelNo, periodId, resourceTypeId, output);
        return output.toByteArray();
    }

    public Map<String, Object> createTask(String aggregatorId, String startDate, String endDate,
                                          String channelNo, Long periodId, String createdBy) {
        return createTask(aggregatorId, startDate, endDate, channelNo, periodId, null, createdBy);
    }

    public Map<String, Object> createTask(String aggregatorId, String startDate, String endDate,
                                          String channelNo, Long periodId, String resourceTypeId, String createdBy) {
        DateRange range = parseRange(startDate, endDate, 366);
        String taskNo = "GD-" + LocalDateTime.now().format(FILE_DATE_TIME) + "-"
                + UUID.randomUUID().toString().substring(0, 8);
        jdbcTemplate.update("INSERT INTO la_grid_export_task (task_no, aggregator_id, period_id, resource_type_id, report_type, start_date, end_date, "
                        + "grid_channel_no, task_status, progress, created_by, expire_time) "
                        + "VALUES (?, ?, ?, ?, 'FULL', ?, ?, ?, 'PENDING', 0, ?, ?)",
                taskNo, aggregatorId, periodId, resourceTypeId, java.sql.Date.valueOf(range.start), java.sql.Date.valueOf(range.end),
                StringUtils.defaultString(channelNo), createdBy,
                Timestamp.valueOf(LocalDateTime.now().plusHours(Math.max(1, expireHours))));
        executor.execute(() -> generateTask(taskNo));
        return task(taskNo, aggregatorId);
    }

    public List<Map<String, Object>> tasks(String aggregatorId) {
        return tasks(aggregatorId, null);
    }

    public List<Map<String, Object>> tasks(String aggregatorId, Long periodId) {
        return tasks(aggregatorId, periodId, null);
    }

    public List<Map<String, Object>> tasks(String aggregatorId, Long periodId, String resourceTypeId) {
        String periodFilter = periodId == null ? "" : " AND period_id = ?";
        String resourceFilter = StringUtils.isBlank(resourceTypeId) ? "" : " AND resource_type_id = ?";
        List<Object> args = new ArrayList<>();
        args.add(aggregatorId);
        if (periodId != null) {
            args.add(periodId);
        }
        if (StringUtils.isNotBlank(resourceTypeId)) {
            args.add(resourceTypeId);
        }
        return jdbcTemplate.queryForList("SELECT task_no taskNo, period_id periodId, resource_type_id resourceTypeId, report_type reportType, start_date startDate, "
                + "end_date endDate, grid_channel_no channelNo, task_status status, progress, file_name fileName, "
                + "error_message errorMessage, finish_time finishTime, expire_time expireTime, create_time createTime "
                + "FROM la_grid_export_task WHERE aggregator_id = ?" + periodFilter + resourceFilter
                + " ORDER BY id DESC LIMIT 50", args.toArray());
    }

    public Map<String, Object> task(String taskNo, String aggregatorId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT task_no taskNo, period_id periodId, resource_type_id resourceTypeId, report_type reportType, "
                + "start_date startDate, end_date endDate, grid_channel_no channelNo, task_status status, progress, "
                + "file_name fileName, file_path filePath, error_message errorMessage, finish_time finishTime, "
                + "expire_time expireTime, create_time createTime FROM la_grid_export_task "
                + "WHERE task_no = ? AND aggregator_id = ?", taskNo, aggregatorId);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("导出任务不存在或不属于当前聚合商");
        }
        return rows.get(0);
    }

    public Path taskFile(String taskNo, String aggregatorId) {
        Map<String, Object> task = task(taskNo, aggregatorId);
        if (!"SUCCESS".equals(task.get("status"))) {
            throw new IllegalStateException("导出任务尚未完成");
        }
        Object expire = task.get("expireTime");
        if (expire != null && toDateTime(expire).isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("导出文件已过期，请重新生成");
        }
        Path root = Paths.get(exportPath).toAbsolutePath().normalize();
        Path file = Paths.get(String.valueOf(task.get("filePath"))).toAbsolutePath().normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file)) {
            throw new IllegalStateException("导出文件不存在");
        }
        return file;
    }

    private void generateTask(String taskNo) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT aggregator_id aggregatorId, period_id periodId, resource_type_id resourceTypeId, start_date startDate, "
                    + "end_date endDate, grid_channel_no channelNo FROM la_grid_export_task WHERE task_no = ?", taskNo);
            if (rows.isEmpty()) {
                return;
            }
            Map<String, Object> task = rows.get(0);
            jdbcTemplate.update("UPDATE la_grid_export_task SET task_status = 'RUNNING', progress = 10 WHERE task_no = ?", taskNo);
            DateRange range = new DateRange(toDate(task.get("startDate")), toDate(task.get("endDate")));
            Path directory = Paths.get(exportPath).toAbsolutePath().normalize();
            Files.createDirectories(directory);
            String fileName = "电网上送核查-" + range.start.format(DATE) + "-" + range.end.format(DATE)
                    + "-" + taskNo + ".xlsx";
            Path file = directory.resolve(taskNo + ".xlsx").normalize();
            try (OutputStream output = Files.newOutputStream(file)) {
                Object periodId = task.get("periodId");
                writeReport(String.valueOf(task.get("aggregatorId")), range,
                        task.get("channelNo") == null ? "" : String.valueOf(task.get("channelNo")),
                        periodId == null ? null : Long.valueOf(String.valueOf(periodId)),
                        task.get("resourceTypeId") == null ? null : String.valueOf(task.get("resourceTypeId")), output);
            }
            jdbcTemplate.update("UPDATE la_grid_export_task SET task_status = 'SUCCESS', progress = 100, file_name = ?, "
                            + "file_path = ?, finish_time = NOW() WHERE task_no = ?",
                    fileName, file.toString(), taskNo);
        } catch (Exception ex) {
            jdbcTemplate.update("UPDATE la_grid_export_task SET task_status = 'FAILED', progress = 100, "
                            + "error_message = ?, finish_time = NOW() WHERE task_no = ?",
                    StringUtils.abbreviate(ex.getMessage(), 1000), taskNo);
        }
    }

    private void writeReport(String aggregatorId, DateRange range, String channelNo, Long periodId,
                             String resourceTypeId,
                             OutputStream output) throws IOException {
        SXSSFWorkbook workbook = new SXSSFWorkbook(200);
        workbook.setCompressTempFiles(true);
        try {
            CellStyle header = headerStyle(workbook);
            Sheet overview = sheet(workbook, "每日上送概览",
                    Arrays.asList("日期", "参与单体", "不参与单体", "总加实际/应有", "总加完整率", "单体实际/应有", "单体完整率",
                            "对账一致", "精度差异", "异常不一致", "对账完整率"), header);
            Sheet total = sheet(workbook, "总加实际数据",
                    Arrays.asList("时间", "值", "状态", "数据来源", "通道"), header);
            List<String> singleHeaders = new ArrayList<>(Arrays.asList("业务日", "参与状态", "单体编码", "单体名称"));
            for (int minute = 15; minute < 24 * 60; minute += 15) {
                singleHeaders.add(String.format("%02d:%02d", minute / 60, minute % 60));
            }
            Sheet single = sheet(workbook, "单体量测95点", singleHeaders, header);
            Sheet reconciliation = sheet(workbook, "总加单体对账",
                    Arrays.asList("时间", "总加原值(MW)", "总加折算(kW)", "参与单体加和(kW)", "绝对偏差(kW)", "缺失单体", "精度容差(kW)", "结果"), header);
            Sheet issues = sheet(workbook, "异常处理记录",
                    Arrays.asList("日期", "问题时间", "问题类型", "单体编码", "原因", "状态", "处理人", "备注"), header);
            Sheet participation = sheet(workbook, "参与及不参与单体",
                    Arrays.asList("业务日", "参与状态", "快照ID", "单体编码", "单体名称", "企业", "设备编码"), header);

            int overviewRow = 1;
            int totalRow = 1;
            int singleRow = 1;
            int reconciliationRow = 1;
            int participationRow = 1;
            LocalDate cursor = range.start;
            while (!cursor.isAfter(range.end)) {
                String day = cursor.format(DATE);
                Map<String, Object> summary = qualityService.dailyOverview(aggregatorId, day, channelNo, resourceTypeId);
                row(overview, overviewRow++, day, summary.get("participantCount"),
                        summary.get("nonParticipantCount"),
                        fraction(summary.get("totalActual"), summary.get("totalExpected")), summary.get("totalRate"),
                        fraction(summary.get("singleActual"), summary.get("singleExpected")), summary.get("singleRate"),
                        summary.get("reconcileMatched"), summary.get("reconcilePrecision"),
                        summary.get("reconcileMismatch"), summary.get("reconcileRate"));
                for (Map<String, Object> item : qualityService.daily(aggregatorId, day, "TOTAL", channelNo, resourceTypeId)) {
                    row(total, totalRow++, item.get("time"), item.get("value"), item.get("status"), item.get("source"), item.get("channelNo"));
                }
                Map<String, Map<String, Object>> singleByCode = new LinkedHashMap<>();
                for (Map<String, Object> item : qualityService.dailyAllSingles(aggregatorId, day, channelNo, resourceTypeId)) {
                    String code = String.valueOf(item.get("singleCode"));
                    Map<String, Object> values = singleByCode.computeIfAbsent(code, ignored -> new LinkedHashMap<>());
                    values.put("singleCode", code);
                    values.put("singleName", item.get("singleName"));
                    values.put("participating", item.get("participating"));
                    String time = String.valueOf(item.get("time"));
                    values.put(time.substring(11, 16), item.get("value"));
                }
                for (Map<String, Object> values : singleByCode.values()) {
                    List<Object> cells = new ArrayList<>();
                    cells.add(day);
                    cells.add(Boolean.TRUE.equals(values.get("participating")) ? "参与" : "不参与");
                    cells.add(values.get("singleCode"));
                    cells.add(values.get("singleName"));
                    for (int minute = 15; minute < 24 * 60; minute += 15) {
                        cells.add(values.get(String.format("%02d:%02d", minute / 60, minute % 60)));
                    }
                    row(single, singleRow++, cells.toArray());
                }
                for (Map<String, Object> item : qualityService.reconciliation(aggregatorId, day, channelNo, resourceTypeId)) {
                    row(reconciliation, reconciliationRow++, item.get("time"), item.get("totalValue"), item.get("totalValueKw"),
                            item.get("singleSum"), item.get("difference"), item.get("missingSingleCount"),
                            item.get("tolerance"), item.get("status"));
                }
                for (Map<String, Object> item : qualityService.participationScope(aggregatorId, day, resourceTypeId)) {
                    row(participation, participationRow++, day,
                            Boolean.TRUE.equals(item.get("participating")) ? "参与" : "不参与",
                            item.get("snapshotId"), item.get("singleCode"),
                            item.get("singleName"), item.get("entId"), item.get("deviceCode"));
                }
                cursor = cursor.plusDays(1);
            }
            int issueRow = 1;
            List<Map<String, Object>> issueRows = jdbcTemplate.queryForList("SELECT data_date dataDate, issue_time issueTime, "
                            + "issue_type issueType, single_code singleCode, reason, status, handler_name handlerName, remark "
                            + "FROM la_grid_quality_issue WHERE aggregator_id = ? AND data_date BETWEEN ? AND ? "
                            + "AND IFNULL(grid_channel_no, '') = ?"
                            + (periodId == null ? "" : " AND period_id = ?")
                            + (StringUtils.isBlank(resourceTypeId) ? "" : " AND resource_type_id = ?")
                            + " ORDER BY issue_time, id",
                    reportIssueArgs(aggregatorId, range, channelNo, periodId, resourceTypeId));
            for (Map<String, Object> item : issueRows) {
                row(issues, issueRow++, item.get("dataDate"), item.get("issueTime"), item.get("issueType"),
                        item.get("singleCode"), item.get("reason"), item.get("status"), item.get("handlerName"), item.get("remark"));
            }
            workbook.write(output);
        } finally {
            workbook.dispose();
            workbook.close();
        }
    }

    private Sheet sheet(SXSSFWorkbook workbook, String name, List<String> headers, CellStyle style) {
        Sheet sheet = workbook.createSheet(name);
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(style);
            sheet.setColumnWidth(i, Math.min(50, Math.max(14, headers.get(i).length() * 3)) * 256);
        }
        sheet.createFreezePane(0, 1);
        return sheet;
    }

    private CellStyle headerStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private void row(Sheet sheet, int index, Object... values) {
        Row row = sheet.createRow(index);
        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            Object value = values[i];
            if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else if (value != null) {
                cell.setCellValue(String.valueOf(value));
            }
        }
    }

    private int issueCount(String aggregatorId, LocalDate date, String channelNo) {
        return issueCount(aggregatorId, date, channelNo, null, null);
    }

    private int issueCount(String aggregatorId, LocalDate date, String channelNo, Long periodId,
                           String resourceTypeId) {
        String sql = "SELECT COUNT(1) FROM la_grid_quality_issue WHERE aggregator_id = ? "
                + "AND data_date = ? AND IFNULL(grid_channel_no, '') = ? AND status NOT IN ('RESOLVED','IGNORED')"
                + (periodId == null ? "" : " AND period_id = ?")
                + (StringUtils.isBlank(resourceTypeId) ? "" : " AND resource_type_id = ?");
        List<Object> args = new ArrayList<>();
        args.add(aggregatorId);
        args.add(java.sql.Date.valueOf(date));
        args.add(StringUtils.defaultString(channelNo));
        if (periodId != null) {
            args.add(periodId);
        }
        if (StringUtils.isNotBlank(resourceTypeId)) {
            args.add(resourceTypeId);
        }
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, args.toArray());
        return count == null ? 0 : count;
    }

    private Object[] reportIssueArgs(String aggregatorId, DateRange range, String channelNo, Long periodId,
                                     String resourceTypeId) {
        List<Object> args = new ArrayList<>();
        args.add(aggregatorId);
        args.add(java.sql.Date.valueOf(range.start));
        args.add(java.sql.Date.valueOf(range.end));
        args.add(StringUtils.defaultString(channelNo));
        if (periodId != null) {
            args.add(periodId);
        }
        if (StringUtils.isNotBlank(resourceTypeId)) {
            args.add(resourceTypeId);
        }
        return args.toArray();
    }

    private String qualified(Map<String, Object> summary) {
        return qualified(summary, BigDecimal.valueOf(99));
    }

    private String qualified(Map<String, Object> summary, BigDecimal standard) {
        return decimal(summary.get("totalRate")).compareTo(standard) >= 0
                && decimal(summary.get("singleRate")).compareTo(standard) >= 0
                && decimal(summary.get("reconcileRate")).compareTo(standard) >= 0 ? "是" : "否";
    }

    private String fraction(Object actual, Object expected) {
        return String.valueOf(actual) + "/" + String.valueOf(expected);
    }

    private BigDecimal decimal(Object value) {
        return value == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(value));
    }

    private DateRange parseRange(String startDate, String endDate, int maximumDays) {
        LocalDate start = LocalDate.parse(startDate, DATE);
        LocalDate end = LocalDate.parse(StringUtils.defaultIfBlank(endDate, startDate), DATE);
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        if (days < 1 || days > maximumDays) {
            throw new IllegalArgumentException("导出日期范围必须为 1 至 " + maximumDays + " 天");
        }
        return new DateRange(start, end);
    }

    private LocalDate toDate(Object value) {
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate();
        }
        return LocalDate.parse(String.valueOf(value).substring(0, 10), DATE);
    }

    private LocalDateTime toDateTime(Object value) {
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime();
        }
        return LocalDateTime.parse(String.valueOf(value).replace('T', ' '),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static final class DateRange {
        private final LocalDate start;
        private final LocalDate end;

        private DateRange(LocalDate start, LocalDate end) {
            this.start = start;
            this.end = end;
        }
    }
}
