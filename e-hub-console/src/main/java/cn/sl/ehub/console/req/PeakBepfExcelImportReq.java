package cn.sl.ehub.console.req;

import java.util.Date;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 基础用电电力预测值Excel导入实体
 *
 * @author sl
 * @date 2026-05-28
 */
@Data
@ApiModel("基础用电电力预测值Excel导入实体")
public class PeakBepfExcelImportReq {

    @Excel(name = "日期", fixedIndex = 0, importFormat = "yyyy-MM-dd")
    @ApiModelProperty("日期")
    private Date date;

    @Excel(name = "时间", fixedIndex = 1, importFormat = "HH:mm")
    @ApiModelProperty("时间")
    private Date time;

    @Excel(name = "基础用电电力预测值（MW）")
    @ApiModelProperty("基础用电电力预测值（MW）")
    private java.math.BigDecimal value;
}
