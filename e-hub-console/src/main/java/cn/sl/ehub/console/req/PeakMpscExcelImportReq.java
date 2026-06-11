package cn.sl.ehub.console.req;

import java.util.Date;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 最大调峰能力Excel导入实体
 *
 * @author sl
 * @date 2026-05-28
 */
@Data
@ApiModel("最大调峰能力Excel导入实体")
public class PeakMpscExcelImportReq {

    @Excel(name = "日期", fixedIndex = 0, importFormat = "yyyy-MM-dd")
    @ApiModelProperty("日期")
    private Date date;

    @Excel(name = "时间", fixedIndex = 1, importFormat = "HH:mm")
    @ApiModelProperty("时间")
    private Date time;

    @Excel(name = "最大调峰能力(MW)")
    @ApiModelProperty("最大调峰能力(MW)")
    private java.math.BigDecimal value;
}
