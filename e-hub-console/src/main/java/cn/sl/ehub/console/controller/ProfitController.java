package cn.sl.ehub.console.controller;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.sl.ehub.console.enums.AggregatorProfitTypeEnum;
import cn.sl.ehub.console.enums.OrderTypeEnum;
import cn.sl.ehub.common.exception.ParamException;
import cn.sl.ehub.console.model.vo.DateProfitWithEntDetailExcelData;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.console.model.vo.ProfitExcelData;
import cn.sl.ehub.service.resp.AggregatorDateProfitResp;
import cn.sl.ehub.service.resp.AggregatorEntDateProfitResp;
import cn.sl.ehub.console.service.IAggregatorEntService;
import cn.sl.ehub.console.service.ProfitService;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.service.vo.*;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "收益统计")
@RequiredArgsConstructor
@RequestMapping("/profit")
@RestController
public class ProfitController {

    private final ProfitService profitService;
    private final IAggregatorEntService aggregatorEntService;

    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");

    @ApiOperation(value = "首页本月收益")
    @GetMapping("/week")
    public ResultVO<List<AggregatorDateProfit>> getAggregatorProfit(@ApiParam(value = "聚合商ID") @RequestParam String aggregatorId) {
        return ResultVO.success(profitService.getAggregatorProfit(aggregatorId, DateUtils.getMouthOneDay(), DateUtils.getDay()));
    }

    @ApiOperation(value = "筛选内容列表")
    @GetMapping("/getContentList")
    public ResultVO<List<AggregatorEnt>> getAggregatorEntList(@ApiParam(value = "聚合商ID") @RequestParam String aggregatorId) {
        return ResultVO.success(profitService.getAggregatorEntList(aggregatorId));
    }

    @ApiOperation(value = "首页 收益统计导出")
    @GetMapping("/month/download")
    public void downloadMonthProfit(@ApiParam(value = "聚合商ID") @RequestParam String aggregatorId,
                                    @ApiParam(value = "日期类型 1:当前月 2:上个月")
                                    @RequestParam(required = false, defaultValue = "1") Integer dateType,
                                    HttpServletResponse response) throws IOException {
        try {
            List<ProfitExcelData> profitExcelData = profitService.getProfitExcelData(aggregatorId, dateType);
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("收益统计", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xls");
            Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams("收益统计", "sheet1"),
                    ProfitExcelData.class, profitExcelData);
            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().println(JSON.toJSONString(ResultVO.fail(400, "下载文件失败")));
        }
    }

    @ApiOperation(value = "收益统计 日收益列表")
    @GetMapping("/list")
    public ResultVO<List<AggregatorDateProfitResp>> getProfitList(@ApiParam(value = "聚合商ID")
                                                                  @RequestParam String aggregatorId,
                                                                  @RequestParam(required = false) String startDate,
                                                                  @RequestParam(required = false) String endDate,
                                                                  @ApiParam(value = "当前页", defaultValue = "1")
                                                                  @RequestParam(required = false, defaultValue = "1") Integer pageIndex,
                                                                  @ApiParam(value = "页大小", defaultValue = "10")
                                                                  @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                                                  @RequestParam(required = false) List<String> entIds) {
        Date queryStartDate = null;
        Date queryEndDate = null;
        try {
            if (StringUtils.isNotBlank(startDate)) {
                queryStartDate = DATE_FORMATTER.parseDateTime(startDate).toDateTime().toDate();
            }

            if (StringUtils.isNotBlank(endDate)) {
                queryEndDate = DATE_FORMATTER.parseDateTime(endDate).toDate();
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ParamException("时间参数有误");
        }
        PageResultVO<AggregatorDateProfitResp> profitPageResult = profitService.getProfitPageResult(aggregatorId,
                queryStartDate, queryEndDate, pageIndex, pageSize, OrderTypeEnum.DESC, entIds, true);
        return ResultWithPageVO.success4Page(profitPageResult.getList(), profitPageResult.getPageIndex(),
                profitPageResult.getPageSize(), profitPageResult.getTotal());
    }

    //@ApiOperation(value = "收益统计 企业日收益列表")
    //@GetMapping("/ent/list")
    public ResultVO<List<AggregatorEntDateProfitResp>> getProfitEntList(@ApiParam(value = "聚合商ID")
                                                                        @RequestParam String aggregatorId,
                                                                        @RequestParam String date,
                                                                        @RequestParam(required = false) List<String> entIds) {
        PageResultVO<AggregatorEntDateProfitResp> profitPageResult = profitService.getEntProfitPageResult(
                aggregatorId, date, null, null, entIds);
        return ResultWithPageVO.success4Page(profitPageResult.getList(), profitPageResult.getPageIndex(),
                profitPageResult.getPageSize(), profitPageResult.getTotal());
    }

    @ApiOperation(value = "收益统计 日收益列表导出")
    @GetMapping("/list/download")
    public void downloadProfitList(@ApiParam(value = "聚合商ID")
                                   @RequestParam String aggregatorId,
                                   @RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate,
                                   @RequestParam(required = false) List<String> entIds,
                                   HttpServletResponse response) throws IOException {
        try {
            Date queryStartDate = null;
            Date queryEndDate = null;
            try {
                if (StringUtils.isNotBlank(startDate)) {
                    queryStartDate = DATE_FORMATTER.parseDateTime(startDate).toDateTime().toDate();
                }
                if (StringUtils.isNotBlank(endDate)) {
                    queryEndDate = DATE_FORMATTER.parseDateTime(endDate).toDate();
                }
            } catch (IllegalArgumentException illegalArgumentException) {
                throw new ParamException("时间参数有误");
            }
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("收益统计", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xls");
            List<DateProfitWithEntDetailExcelData> dateProfitExcelDataList
                    = profitService.getDateProfitWithEntDetailExcelData(aggregatorId, queryStartDate, queryEndDate, entIds);
            Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams("收益统计", "sheet1"),
                    DateProfitWithEntDetailExcelData.class, dateProfitExcelDataList);
            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().println(JSON.toJSONString(ResultVO.fail(400, "下载文件失败")));
        }
    }

    @ApiOperation(value = "日收益列表(有筛选条件)")
    @GetMapping("/listByEntIdList")
    public ResultVO<List<List<String>>> listByEntIdList(@ApiParam(value = "聚合商ID") @RequestParam String aggregatorId,
                                                        @ApiParam(value = "开始时间") @RequestParam String startDate,
                                                        @ApiParam(value = "结束时间") @RequestParam String endDate,
                                                        @ApiParam(value = "当前页", defaultValue = "1") @RequestParam Integer pageIndex,
                                                        @ApiParam(value = "页大小", defaultValue = "10") @RequestParam Integer pageSize,
                                                        @ApiParam(value = "筛选内容") @RequestParam(required = false) List<String> entIds) {
        PageResultVO<List<String>> profitPageResult = profitService.getProfitPageResult(aggregatorId, startDate, endDate, pageIndex, pageSize, entIds);
        return ResultWithPageVO.success4Page(profitPageResult.getList(), profitPageResult.getPageIndex(), profitPageResult.getPageSize(), profitPageResult.getTotal());
    }

    @ApiOperation(value = "日收益列表(有筛选条件)--导出")
    @GetMapping("/listByEntIdListExcel")
    public void listByEntIdListExcel(@ApiParam(value = "聚合商ID") @RequestParam String aggregatorId,
                                                  @ApiParam(value = "开始时间") @RequestParam String startDate,
                                                  @ApiParam(value = "结束时间") @RequestParam String endDate,
                                                  @ApiParam(value = "筛选内容") @RequestParam(required = false) List<String> entIds,
                                                  HttpServletResponse response) throws IOException {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("收益统计", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xls");
            Map<String, String> entIdNameMap = new HashMap<>();
            entIdNameMap.put(AggregatorProfitTypeEnum.ISSUE.getCode(), AggregatorProfitTypeEnum.ISSUE.getDesc());
            entIdNameMap.put(AggregatorProfitTypeEnum.AGGREGATOR.getCode(), AggregatorProfitTypeEnum.AGGREGATOR.getDesc());
            entIdNameMap.put(AggregatorProfitTypeEnum.ENT.getCode(), AggregatorProfitTypeEnum.ENT.getDesc());
            List<AggregatorEnt> aggregatorEntList = aggregatorEntService.getAggregatorEntList(entIds);
            if (null != aggregatorEntList && aggregatorEntList.size() > 0) {
                entIdNameMap.putAll(aggregatorEntList.stream().collect(Collectors.toMap(AggregatorEnt::getEntId, AggregatorEnt::getEntName, (v1, v2) -> v1)));
            }
            List<ExcelExportEntity> excelExportEntityList = Lists.newArrayList();
            excelExportEntityList.add(new ExcelExportEntity("日期", "date"));
            entIds.forEach(entId -> excelExportEntityList.add(new ExcelExportEntity(StringUtils.isEmpty(entIdNameMap.get(entId)) ? "" : entIdNameMap.get(entId), entId)));
            List<Map<String, Object>> dataList = profitService.listByEntIdListExcel(aggregatorId, startDate, endDate, entIds);
            Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams("收益统计", "sheet1"), excelExportEntityList, dataList);
            workbook.write(response.getOutputStream());
//            File file = new File("D:\\abc.xls");
//            FileOutputStream fileOutputStream = new FileOutputStream(file);
//            workbook.write(fileOutputStream);
//            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().println(JSON.toJSONString(ResultVO.fail(400, "下载文件失败")));
        }
    }
}
