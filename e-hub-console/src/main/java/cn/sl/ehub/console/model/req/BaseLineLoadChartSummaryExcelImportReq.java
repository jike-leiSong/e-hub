package cn.sl.ehub.console.model.req;
import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 
 * @author sl
 * @date 2026-05-28
 */
@Data
@ApiModel("数据返回实体")
public class BaseLineLoadChartSummaryExcelImportReq {

    @Excel(name = "时间", importFormat = "HH:mm:ss")
    private String time;

    @Excel(name = "", fixedIndex = 1)
    private Double value1;

    @Excel(name = "", fixedIndex = 2)
    private Double value2;

    @Excel(name = "", fixedIndex = 3)
    private Double value3;

    @Excel(name = "", fixedIndex = 4)
    private Double value4;
}
