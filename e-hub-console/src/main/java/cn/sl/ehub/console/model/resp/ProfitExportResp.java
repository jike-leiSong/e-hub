package cn.sl.ehub.console.model.resp;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * 华北电网调峰服务收益统计表导出
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@ApiModel("华北电网调峰服务收益统计表导出")
@Data
public class ProfitExportResp {

    @Excel(name = "客户名称")
    private String customerName;
    @Excel(name = "项目名称")
    private String projectName;
    @Excel(name = "起始日期")
    private String startDate;
    @Excel(name = "截止日期")
    private String endDate;
    @Excel(name = "累计收益（元）")
    private Double profit;
    @Excel(name = "企业名称")
    private String entName;
    @Excel(name = "日期")
    private List<String> dateList;
    @Excel(name = "时段")
    private List<String> timeList;
    @Excel(name = "出清价格（元/kWh)")
    private List<Double> offerList;
    @Excel(name = "调节电量（kWh)")
    private List<Double> electricQuantityList;
    @Excel(name = "时段收益（元）")
    private List<Double> profitList;
    @Excel(name = "日收益（元）")
    private List<Double> dayProfitList;
}
