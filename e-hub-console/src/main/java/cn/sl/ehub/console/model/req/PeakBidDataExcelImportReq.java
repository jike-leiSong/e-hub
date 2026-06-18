package cn.sl.ehub.console.model.req;

import java.util.Date;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 日运行指标Excel导入实体
 *
 * @author sl
 * @date 2026-05-28
 */
@Data
@ApiModel("日运行指标Excel导入实体")
public class PeakBidDataExcelImportReq {

    @Excel(name = "日期", fixedIndex = 0, importFormat = "yyyy-MM-dd")
    @ApiModelProperty("日期")
    private Date date;

    @Excel(name = "调峰报价（元/MW）")
    @ApiModelProperty("调峰报价（元/MW）")
    private java.math.BigDecimal bidPrice;

    @Excel(name = "最大充电电量")
    @ApiModelProperty("最大充电电量")
    private java.math.BigDecimal maxInPower;

    @Excel(name = "最大放电电量")
    @ApiModelProperty("最大放电电量")
    private java.math.BigDecimal maxOutPower;

    @Excel(name = "日最大充电次数")
    @ApiModelProperty("日最大充电次数")
    private java.math.BigDecimal maxInTimes;

    @Excel(name = "日最大放电次数")
    @ApiModelProperty("日最大放电次数")
    private java.math.BigDecimal maxOutTimes;

    @Excel(name = "充电速率（WM/min）")
    @ApiModelProperty("充电速率（WM/min）")
    private java.math.BigDecimal inRate;

    @Excel(name = "放电速率")
    @ApiModelProperty("放电速率")
    private java.math.BigDecimal outRate;

    @Excel(name = "充电起始SOC")
    @ApiModelProperty("充电起始SOC")
    private java.math.BigDecimal soc;

    @Excel(name = "备用2")
    @ApiModelProperty("备用2")
    private java.math.BigDecimal value2;

    @Excel(name = "备用3")
    @ApiModelProperty("备用3")
    private java.math.BigDecimal value3;

    @Excel(name = "备用4")
    @ApiModelProperty("备用4")
    private java.math.BigDecimal value4;
}
