package cn.sl.ehub.console.model.vo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.sl.ehub.service.resp.AggregatorDateProfitResp;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class ProfitExcelData {
    @Excel(name = "日期", width = 12)
    private String date;
    @Excel(name = "调度下发总金额(元)")
    private String issueProfit;
    @Excel(name = "负荷聚合商收益(元)")
    private String aggregatorProfit;
    @Excel(name ="用户收益(元)")
    private String entProfit;

    public static List<ProfitExcelData> transform(List<AggregatorDateProfitResp> list) {
        if (list != null && !list.isEmpty()) {
            return list.stream().map(e -> {
                ProfitExcelData profitExcelData = new ProfitExcelData();
                profitExcelData.setDate(e.getDate());
                profitExcelData.setIssueProfit(String.format("%.2f", e.getIssueProfit()));
                profitExcelData.setAggregatorProfit(String.format("%.2f", e.getAggregatorProfit()));
                profitExcelData.setEntProfit(String.format("%.2f", e.getEntProfit()));
                return profitExcelData;
            }).collect(Collectors.toList());
        }
        return null;
    }
}
