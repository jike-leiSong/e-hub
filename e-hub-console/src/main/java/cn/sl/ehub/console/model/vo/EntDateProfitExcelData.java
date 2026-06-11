package cn.sl.ehub.console.model.vo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

@Data
public class EntDateProfitExcelData implements java.io.Serializable {

    private String entId;
    private String date;
    @Excel(name = "企业名称", width = 15)
    private String entName;
    @Excel(name = "企业收益(元)")
    private Double entProfit;

}
