package cn.sl.ehub.console.model.vo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelCollection;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.util.Date;
import java.util.List;

@ExcelTarget("dateProfitWithEntDetailExcelData")
@Data
public class DateProfitWithEntDetailExcelData implements java.io.Serializable {

    private String aggregatorId;
    @Excel(name = "日期", width = 12, needMerge = true)
    private String date;
    @Excel(name = "调度下发总金额(元)", needMerge = true)
    private Double issueProfit;
    @Excel(name = "负荷聚合商收益(元)", needMerge = true)
    private Double aggregatorProfit;
    @Excel(name = "企业收益(元)", needMerge = true)
    private Double entProfit;
    @ExcelCollection(name = "企业收益详情")
    List<EntDateProfitExcelData> entDateProfitList;
}
