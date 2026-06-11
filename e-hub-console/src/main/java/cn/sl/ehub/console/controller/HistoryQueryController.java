package cn.sl.ehub.console.controller;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.export.ExcelExportService;
import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.console.model.req.*;
import cn.sl.ehub.console.model.resp.*;
import cn.sl.ehub.console.model.vo.*;
import cn.sl.ehub.service.req.AdjustSituationExcelRep;
import cn.sl.ehub.service.req.IndexOverviewTableResp;
import cn.sl.ehub.service.resp.HistoryQueryDeviceMetricResp;
import cn.sl.ehub.service.resp.IndexOverviewResp;
import cn.sl.ehub.console.service.IAggregatorEntService;
import cn.sl.ehub.console.service.IAggregatorInfoService;
import cn.sl.ehub.console.service.IAggregatorResourceTypeService;
import cn.sl.ehub.console.service.IHistoryQueryService;
import cn.sl.ehub.service.vo.AggregatorEnt;
import cn.sl.ehub.service.vo.AggregatorInfo;
import cn.sl.ehub.service.vo.AggregatorResourceType;
import cn.sl.ehub.common.vo.ResultVO;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.util.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Description: 历史查询接口
 * @Author sl
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/historyQuery")
@Api(tags = "历史查询")
public class HistoryQueryController {

    private final IHistoryQueryService historyQueryService;
    private final IAggregatorEntService aggregatorEntService;
    private final IAggregatorResourceTypeService aggregatorResourceTypeService;
    private final IAggregatorInfoService iAggregatorInfoService;


    @ApiOperation(value = "汇总功率曲线")
    @RequestMapping(value = "/getTotalPowerChart", method = RequestMethod.GET)
    public ResultVO<IndexOverviewResp> getTotalPowerChart(HttpServletRequest request,
                                                          @RequestParam("aggregatorId") String aggregatorId,
                                                          @RequestParam("resourceTypeId") String resourceTypeId,
                                                          @RequestParam(value = "startDate", required = false) String startDate,
                                                          @RequestParam(value = "endDate", required = false) String endDate) {
        String simulate = request.getHeader("simulate");
        if (StringUtils.isEmpty(simulate) || "null".equals(simulate) || !"1".equals(simulate)) {
            simulate = "0";
        }
        return ResultVO.success(historyQueryService.getTotalPowerChart(simulate, aggregatorId, resourceTypeId, startDate, endDate));
    }

    @PostMapping("/userAdjustmentGraph")
    @ApiOperation(value = "用户完成调节情况曲线图接口", notes = "用户完成调节情况曲线图接口")
    public ResultVO<HistoryQueryGraphVO> userAdjustmentGraph(HttpServletRequest request, @RequestBody @Valid UserAdjustmentGraphReq userAdjustmentGraphReq, BindingResult results) {
        if (results.hasErrors()) {
            return ResultVO.fail(StatusCode.ERROR.getCode(), results.getFieldError().getDefaultMessage());
        }
        String simulate = request.getHeader("simulate");
        if (StringUtils.isEmpty(simulate) || "null".equals(simulate) || !"1".equals(simulate)) {
            simulate = "0";
        }
        return ResultVO.success(historyQueryService.userAdjustmentGraph(userAdjustmentGraphReq, simulate));
    }


    @PostMapping("/userAdjustmentGraphNew")
    @ApiOperation(value = "用户完成调节情况曲线图接口", notes = "用户完成调节情况曲线图接口")
    public ResultVO<HistoryQueryGraphVO> userAdjustmentGraphNew(HttpServletRequest request, @RequestBody @Valid NewUserAdjustmentGraphReq userAdjustmentGraphReq, BindingResult results) {
        if (results.hasErrors()) {
            return ResultVO.fail(StatusCode.ERROR.getCode(), results.getFieldError().getDefaultMessage());
        }
        String simulate = request.getHeader("simulate");
        if (StringUtils.isEmpty(simulate) || "null".equals(simulate) || !"1".equals(simulate)) {
            simulate = "0";
        }
        return ResultVO.success(historyQueryService.userAdjustmentGraphNew(userAdjustmentGraphReq, simulate));
    }

    @PostMapping("/userAdjustmentTable")
    @ApiOperation(value = "用户完成调节情况图表接口", notes = "用户完成调节情况图表接口")
    public ResultVO<PageResultVO<HistoryQueryTableVO>> userAdjustmentTable(@RequestBody @Valid UserAdjustmentTableReq userAdjustmentTableReq, BindingResult results) {
        if (results.hasErrors()) {
            return ResultVO.fail(StatusCode.ERROR.getCode(), results.getFieldError().getDefaultMessage());
        }
        return ResultVO.success(historyQueryService.userAdjustmentTable(userAdjustmentTableReq));
    }

    @PostMapping("/deviceRunStatusChart")
    @ApiOperation(value = "设备运行情况曲线图接口", notes = "设备运行情况曲线图接口")
    public ResultVO<List<LineDataGraphResp>> deviceRunStatusChart(HttpServletRequest request, @RequestBody DeviceRunStatusReq deviceRunStatusReq) {
        String simulate = request.getHeader("simulate");
        if (StringUtils.isEmpty(simulate) || "null".equals(simulate) || !"1".equals(simulate)) {
            simulate = "0";
        }
        return ResultVO.success(historyQueryService.deviceRunStatusChart(deviceRunStatusReq, simulate));
    }

    @PostMapping("/getMetricList")
    @ApiOperation(value = "设备运行情况测点列表接口", notes = "设备运行情况测点列表接口")
    public ResultVO<List<HistoryQueryDeviceMetricResp>> getMetricList() {
        return ResultVO.success(historyQueryService.getMetricList());
    }

    @PostMapping("/profitStatistics")
    @ApiOperation(value = "收益统计接口", notes = "收益统计接口")
    public ResultVO<ProfitStatisticsVO> profitStatistics(@RequestBody ProfitStatisticsReq profitStatisticsReq) {
        return ResultVO.success(historyQueryService.profitStatistics(profitStatisticsReq));
    }

    @PostMapping("/userProfitStatistics")
    @ApiOperation(value = "用户收益统计接口", notes = "用户收益统计接口")
    public ResultVO<UserProfitStatisticsVO> userProfitStatistics(@RequestBody ProfitStatisticsReq profitStatisticsReq) {
        return ResultVO.success(historyQueryService.userProfitStatistics(profitStatisticsReq));
    }

    @PostMapping("/getPrice")
    @ApiOperation(value = "出清价格接口", notes = "出清价格接口")
    public ResultVO<IndexOverviewResp> getPrice(@RequestBody ProfitStatisticsReq profitStatisticsReq) {
        return ResultVO.success(historyQueryService.getPrice(profitStatisticsReq));
    }

    @PostMapping("/getPriceTable")
    @ApiOperation(value = "出清价格表格接口", notes = "出清价格表格接口")
    public ResultVO<IndexOverviewTableResp> getPriceTable(@RequestBody ProfitStatisticsReq profitStatisticsReq) {
        return ResultVO.success(historyQueryService.getPriceTable(profitStatisticsReq));
    }

    @ApiOperation(value = "出清价格导出", notes = "出清价格导出")
    @GetMapping("/getPriceExcel")
    public void getPriceExcel(
            @RequestParam("aggregatorId") String aggregatorId,
            @RequestParam("resourceTypeId") String resourceTypeId,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            HttpServletResponse response) throws IOException {
        try {
            AggregatorResourceType aggregatorResourceType = aggregatorResourceTypeService.getTypeById(resourceTypeId);
            String sourceTypeName=null;
            if(null != aggregatorResourceType){
                sourceTypeName =  aggregatorResourceType.getName();
            }


            String excelFileName = startTime + "~" + endTime + sourceTypeName+"出清情况";
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode(excelFileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xls");
            ProfitStatisticsReq profitStatisticsReq = new ProfitStatisticsReq();
            profitStatisticsReq.setAggregatorId(aggregatorId);
            profitStatisticsReq.setResourceTypeId(resourceTypeId);
            profitStatisticsReq.setStartTime(startTime);
            profitStatisticsReq.setEndTime(endTime);
            List<PriceExcelDateResp> priceExcelDateRespList = historyQueryService.getPriceExcel(profitStatisticsReq);
            ExcelExportService service = new ExcelExportService();

            Workbook workbook = new HSSFWorkbook();
            priceExcelDateRespList.forEach(priceExcelDateResp -> {
                ExportParams exportParams = new ExportParams(null, priceExcelDateResp.getDate());
                List<PriceExcelResp> priceExcelRespList = priceExcelDateResp.getPriceExcelRespList();
                service.createSheet(workbook, exportParams, PriceExcelResp.class, priceExcelRespList);
            });
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

    @ApiOperation(value = "查询收益计算")
    @GetMapping("/getProfitCalculation")
    public ResultVO<List<HistoryProfitCalculationExcelResp>> getProfitCalculation(
            @RequestParam("entId") String entId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        return ResultVO.success(historyQueryService.getProfitCalculation(entId, startDate, endDate));
    }

    @ApiOperation(value = "收益计算导出")
    @GetMapping("/getProfitCalculationExcel")
    public void getProfitCalculationExcel(
            @RequestParam("entId") String entId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            HttpServletResponse response) throws IOException {
        try {
            //查询企业信息
            AggregatorEnt aggregatorEnt = aggregatorEntService.getAggregatorEnt(entId);
            LocalDate localStDate = LocalDate.parse(startDate);
            LocalDate localEdDate = LocalDate.parse(endDate);
            String stDate = localStDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
            String edDate = localEdDate.format(DateTimeFormatter.ofPattern("MM月dd日"));
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode(aggregatorEnt.getEntName()+" "+stDate+"∼"+edDate, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xls");

            ExcelExportService service = new ExcelExportService();
            Workbook workbook = new HSSFWorkbook();
            ExportParams exportParamsTotal = new ExportParams(null, "汇总");
            List<HistoryProfitCalculationExcelResp> historyProfitCalculationExcelRespList = historyQueryService.getProfitCalculation(entId, startDate, endDate);
            service.createSheet(workbook, exportParamsTotal, HistoryProfitCalculationExcelResp.class, historyProfitCalculationExcelRespList);

            LinkedHashMap<String, List<HistoryProfitCalculationTimeExcelResp>> profitCalculationMap = historyQueryService.getProfitCalculationMap(entId, startDate, endDate);
            profitCalculationMap.entrySet().forEach(profitMap -> {
                ExportParams exportParams = new ExportParams(null, profitMap.getKey());
                service.createSheet(workbook, exportParams, HistoryProfitCalculationTimeExcelResp.class, profitMap.getValue());
            });

            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().println(JSON.toJSONString(ResultVO.fail(400, "下载文件失败")));
        }
    }



    @ApiOperation(value = "导出调节情况excel")
    @GetMapping("/exportAdjust")
    public void exportAdjust(HttpServletResponse response,
                             String aggregatorId, String entId, String sourceId, String startDate,String endDate)throws IOException{

        try {
            AdjustSituationExcelRep req = new AdjustSituationExcelRep();
            req.setAggregatorId(aggregatorId);
            req.setEntId(entId);
            req.setSourceId(sourceId);
            req.setStartDate(startDate);
            req.setEndDate(endDate);

            HistoryAdjustExcelResp historyAdjustExcelResp = historyQueryService.exportAdjustSituationExcel(req);
            AggregatorResourceType aggregatorResourceType = aggregatorResourceTypeService.getTypeById(sourceId);
            String sourceTypeName = null;
            String excelFileName = null;
            if (null != aggregatorResourceType) {
                sourceTypeName = aggregatorResourceType.getName();
            }
            if (StringUtil.isNotEmpty(entId)) {
                AggregatorEnt aggregatorEnt = aggregatorEntService.getAggregatorEnt(entId);
                excelFileName = startDate + "~" + endDate + aggregatorEnt.getEntName() + sourceTypeName + "调节效果统计";
            } else {
                excelFileName = startDate + "~" + endDate + sourceTypeName + "调节效果统计";
            }

            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode(excelFileName, "UTF-8").replaceAll("\\+", "%20");

            ExportParams exportParams = new ExportParams(null, "sheet1");
            Workbook workbook = ExcelExportUtil.exportExcel(exportParams, historyAdjustExcelResp.getEntityList(), historyAdjustExcelResp.getAllExcelDataList());

            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xls");
            response.setCharacterEncoding("UTF-8");
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().println(JSON.toJSONString(ResultVO.fail(400, "下载文件失败")));
        }
    }

    @ApiOperation(value = "导出补招上送数据excel")
    @GetMapping("/exportBuZhaoUploadData")
    public void exportBuZhaoUploadData(HttpServletResponse response,
                                       String aggregatorId, String sourceId, String startDate,String endDate)throws IOException{

        try {
            // 获取excel数据
            AdjustSituationExcelRep req = new AdjustSituationExcelRep();
            req.setAggregatorId(aggregatorId);
            req.setSourceId(sourceId);
            req.setStartDate(startDate);
            req.setEndDate(endDate);
            HistoryAdjustExcelResp historyAdjustExcelResp = historyQueryService.exportBuZhaoUploadData(req);

            String excelFileName = "";
            if(StringUtil.isNotEmpty(aggregatorId)){
                AggregatorInfo aggregatorInfo = iAggregatorInfoService.getAggregatorInfo(aggregatorId);
                excelFileName = "【" + aggregatorInfo.getAggregatorName() + "】总加数据补招【" + startDate + "~" + endDate + "】";
            }else {
                excelFileName = "总加数据补招【" + startDate + "~" + endDate + "】";
            }

            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode(excelFileName, "UTF-8").replaceAll("\\+", "%20");

            ExportParams exportParams = new ExportParams(null, "sheet1");
            Workbook workbook = ExcelExportUtil.exportExcel(exportParams, historyAdjustExcelResp.getEntityList(), historyAdjustExcelResp.getAllExcelDataList());

            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xls");
            response.setCharacterEncoding("UTF-8");
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().println(JSON.toJSONString(ResultVO.fail(400, "下载文件失败")));
        }
    }

}