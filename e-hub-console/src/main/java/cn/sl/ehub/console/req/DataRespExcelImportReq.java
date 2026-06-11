package cn.sl.ehub.console.req;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 数据返回实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("数据返回实体")
public class DataRespExcelImportReq {

    @Excel(name = "时间", importFormat = "HH:mm:ss")
    private String time;

    @Excel(name = "值")
    private Double value;
}
