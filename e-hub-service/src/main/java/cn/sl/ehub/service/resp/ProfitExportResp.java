package cn.sl.ehub.service.resp;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProfitExportResp {
    private String date;
    private String entName;
    private BigDecimal profit;
    private BigDecimal power;
}
